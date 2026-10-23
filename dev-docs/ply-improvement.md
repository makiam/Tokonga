# PLY import — improving `PLYReader` with ideas from the `Import PLY.bsh` script

Status: reader work done — binary/ASCII/polygon fixes, header hardening, checkstyle cleanup and the PR #355 review items landed; 23 tests / 13 fixtures all green. Only the scale / center / ground dialog (§4.2–4.3) is deliberately deferred; see §9 for the review outcome
Scope target: `Translators` module (`PLYReader`, `PLYImporter`, `PLYTranslator`, `PLYReaderTest`)

## 1. Goal / non-goals

**Goal**: bring the Stanford PLY **importer** in the `Translators` module to the practical level of
the user script `Scripts/Tools/Import PLY.bsh` (working binary parsing, tolerance to polygonal
faces, and later the scale / center / ground options), while keeping the Java reader's superior
header model and typed decoding.

**Done so far**: binary PLY in both byte orders, side-by-side x/y/z lookup by property name, fan
triangulation of n-gons, header-typed list widths, 64-bit scalars, unused-element skipping and
checkstyle-clean files. **Still open**: the import options dialog and its bounding box scan
(§4.2–4.3), deliberately deferred.

**Non-goals**:
- PLY **export** (`PLYTranslator.canExport()` stays `false`).
- Arbitrary vertex-color / normal / UV attribute import (kept for later; only x/y/z + faces matter now).
- Rewriting `PLYImporter`'s "new scene + new window" flow into "import into current window"
  (a separate UX decision, see §8).

## 2. Current state (facts, verified in this tree)

| Area | Location | Note |
|---|---|---|
| Reader | `Translators/src/main/java/artofillusion/translators/PLYReader.java` | header model (`Property`/`Element`/`Header`) is complete, incl. list properties; all reading paths share one fan-triangulating `buildMesh()` |
| Binary path | `PLYReader.readBinary` | **fixed**: offset table for the sub-triangle case, sequential walk when the vertex element holds a list property, header-typed face lists, unused elements skipped |
| ASCII path | `PLYReader.readAscii` | **fixed**: x/y/z picked by header property index (other scalars and lists are consumed in order); non-triangular faces fan triangulated |
| Wrapper | `Translators/src/main/java/artofillusion/translators/PLYImporter.java` | JFileChooser (title key `importPLY`) + new `Scene` + camera/light + one `ObjectInfo` named after the file (extension stripped, §4.5) → `ArtOfIllusion.newWindow`; no import options yet |
| Translator registration | `Translators/src/main/java/artofillusion/translators/PLYTranslator.java` | implements `Translator`, `canImport() == true`, delegates to `PLYImporter` |
| Tests | `Translators/src/test/java/artofillusion/translators/PLYReaderTest.java` | **23 tests / 13 fixtures, all green** — see §7 |
| Reference script | `Scripts/Tools/Import PLY.bsh` | BeanShell script (Italian UI); binary-only, offset-based fast path, bbox scan, scale/center dialog, polygon-tolerant |

Reproduce the test status with `./gradlew :Translators:test --tests "*PLYReaderTest"`. In this
environment Gradle must run on JDK 24 (`JAVA_HOME=~/.gradle/jdks/eclipse_adoptium-24-*`); the
default JDK 27 breaks `buildSrc:compileGroovyPlugins` with `Unsupported class file major version 71`.

## 3. Side-by-side comparison

| Capability | `Import PLY.bsh` | `PLYReader` + `PLYImporter` |
|---|---|---|
| ASCII format | rejected with a warning | ✅ reads it |
| Binary little-endian | ✅ works | ✅ works |
| Binary big-endian | ✅ works | ✅ works |
| Header model | flat scalar scan only | ✅ full model incl. list properties |
| x/y/z located by | byte offset inside the vertex record | by property *name*, any position ✅ |
| Non-x/y/z vertex props | skipped via `bytesPerVertex` | ✅ skipped (offset table, or a sequential walk when the element has a list) |
| Position value type | hardcoded `getFloat()` (float32 only — wrong for `double` vertices) | ✅ `readScalarValue()` handles all types incl. 64-bit |
| Face count / index types | hardcoded uchar count + `int` indices | ✅ `countType`/`elemType` widths are honoured |
| Polygons (count ≠ 3) | tolerated (takes first 3, skips the rest) | ✅ fan triangulated (N−2 triangles, no geometry lost) |
| Unused elements (edge, material, …) | implicitly skipped | ✅ skipped in both paths |
| Bounding box scan | ✅ computed during vertex read | ❌ absent (deferred with the dialog) |
| Import options (scale / center / ground-Y=0) | ✅ dialog + offset math | ❌ none (deferred, §4.2–4.3) |
| Geometry summary before import | ✅ vertex/face count + detected size | ❌ none (deferred with the dialog) |
| Adds object to | current scene window | new scene + new window |
| Undo support | ❌ missing | n/a (new scene) |
| Tests, plugin integration, i18n | ❌ none | ✅ 13 fixtures |

Summary: parsing is now at parity with the script (binary in both byte orders, polygon tolerance,
header-typed decoding) and ahead of it on ASCII support, type fidelity and error reporting. What the
script still has and this reader does not is the import UX: the bounding box scan and the
scale / center / ground dialog, deliberately left out of the current round of work.

## 4. What to port from the script into `PLYReader`

### 4.1 The offset-based fast binary path ✅ implemented

The script's key trick (§2–3 of the script): precompute each property's byte size and the x/y/z
offsets inside the vertex record, then read vertices with **absolute buffer positions** and jump
straight to the face block. This both fixes the old bug (x/y/z read sequentially, other properties
never skipped) and is faster than decoding every property on large meshes.

Landed as `sizeOf(String)` (returns −1 for unknown types so the header parser can reject them) plus
`readScalarAt(buffer, index, type)`, which reads without moving the buffer:

```java
private static int sizeOf(String type) {
    return switch (type) {
        case "char", "uchar", "int8", "uint8" -> 1;
        case "short", "ushort", "int16", "uint16" -> 2;
        case "int", "uint", "int32", "uint32", "float", "float32" -> 4;
        case "double", "float64", "long", "int64", "ulong", "uint64" -> 8;
        default -> -1;
    };
}
```

- **Fast path** (`readBinaryVertices`) — when every vertex property is scalar: build an offset
  table plus `stride`, read x/y/z at `base + i*stride + offset`, then `buffer.position(base + count*stride)`.
  Before the loop it verifies the remaining bytes can hold `count × stride` and raises a readable
  `IOException` instead of letting a `BufferUnderflowException` escape.
- **Fallback** (`readBinaryVertex`) — if the vertex element contains a list property: walk all
  properties in header order, skipping lists with `skipBinaryList()`, storing only x/y/z.
- **Faces** (`readBinaryFaces`) — every property is consumed in header order: the declared
  `countType` gives the list length, `elemType` gives the index width, other lists are skipped and
  other scalars are stepped over, so trailing per-face properties (colours) are tolerated.
- **Byte order** — `read()` passes `LITTLE_ENDIAN` / `BIG_ENDIAN` to the same code.
- **Other elements** (`skipBinaryElement`) — an `edge`/`material`/… element declared between
  `vertex` and `face` is skipped with one arithmetic jump when it is all scalars, record by record
  otherwise, so the buffer stays aligned. Elements are processed in header order, so a `face`
  element declared before `vertex` also works.
- **Bounds safety** — all skips go through `advance()`, which validates against `buffer.limit()`, and
  `readBinary` converts any residual `IndexOutOfBoundsException`/`BufferUnderflowException` into an
  `IOException` about a truncated file.
- **Coverage** — `quad_binary_extras` pins the offset math over `double` x/y/z with a trailing
  `float nx`, a skipped `edge` element and a `uint32` list count; `tet_binary_mixed` pins the
  sequential fallback (list property inside `vertex`), 64-bit positions and `ushort` indices.

### 4.2 Bounding box scan — deferred

Track min/max per axis while reading vertices (one comparison per coordinate, essentially free).
The result is needed by the importer dialog (§4.3). Options: return it together with the mesh via
a small result record, or apply offsets inside `PLYReader` via an overloaded
`read(path, scale, alignToGround)`. Either way, keep the reader free of UI code.

**Not implemented on purpose**: the only consumer is the dialog below, so adding the API now would
mean shipping an untested, unused return channel. `TriangleMesh.getBounds()` already computes the
box lazily (`ArtOfIllusion/.../TriangleMesh.java:489`), so when the dialog is scheduled the bounds
can be taken from the mesh — the extra record may turn out to be unnecessary. Do not add it before
the consumer exists.

### 4.3 Import options dialog (scale / center / ground) — deferred

The script's §4–5 is a genuinely useful UX feature the importer lacks entirely. Per architecture,
the dialog belongs in `PLYImporter`, not in the reader.

**Deliberately out of the current round of work** (with §4.2). Everything below is the prepared
design, to be picked up when the dialog is scheduled.

```java
Widget[] widgets = new Widget[4];
String[] labels  = new String[4];
widgets[0] = new BLabel(vertexCount + " " + Translate.text("Translators:vertices")
    + ", " + faceCount + " " + Translate.text("Translators:faces"));
widgets[1] = new BLabel(String.format("%.3f x %.3f x %.3f", sizeX, sizeY, sizeZ));
widgets[2] = new ValueField(1.0, ValueField.POSITIVE);   // scale factor
widgets[3] = new BCheckBox(Translate.text("Translators:centerAndGround"), true);
new ComponentsDialog(parent, title, widgets, labels).clickedOk()
```

With the script's offset math:

```java
double scale  = scaleField.getValue();
boolean align = centerBox.getState();
double offX = align ? -(minX + maxX) * 0.5 * scale : 0.0;
double offY = align ? -minY * scale : 0.0;
double offZ = align ? -(minZ + maxZ) * 0.5 * scale : 0.0;
```

> Adaptation note: the script hardcodes Italian strings and plain JFileChooser/Native dialogs.
> Port the *feature*, spring the strings via `Translate.text("Translators:…")` bundle keys, and
> stay consistent with `PLYImporter`'s existing Swing-based file chooser approach. `ComponentsDialog`
> usage to mirror is `OBJExporter:64` / `VRMLExporter:67`; `ValueField.POSITIVE` is the established
> flag for a scale field (`OBJExporter:43`).
>
> Bundle status: `fileFilter.ply` already exists and `importPLY` was added (§4.5) — the chooser no
> longer says "Import Wavefront .obj File". Still to add for this dialog: `vertices`, `faces`,
> `centerAndGround`, and a title key (e.g. `importPLYOptions`). Only the default
> `Bundle.properties` needs them; that is the established pattern (`fileFilter.ply` and `importPLY`
> exist only there).

### 4.4 Tolerance to non-triangular faces ✅ implemented

The script never aborts on `count != 3`; the old Java ASCII path threw. Done **better than both**
with fan triangulation: a face with N vertices becomes N−2 triangles `(v0, vi, vi+1)` instead of the
script's keep-first-three-and-discard (which loses geometry). Concretely:

- both paths collect raw polygons into `List<int[]>` and hand them to a single `buildMesh()`, which
  fan-triangulates them — so the ASCII and binary paths cannot drift apart;
- `buildMesh()` first validates every index against the vertex count and reports the offending face
  number, turning what would have been an `ArrayIndexOutOfBoundsException` inside `TriangleMesh`
  into an actionable `IOException`;
- degenerate counts (`< 3`) are skipped with a debug log, not an error; triangles whose fan produces a
  repeated index (e.g. `3 0 0 1`) are skipped the same way, so a malformed face cannot hand
  `TriangleMesh` a self-referencing edge;
- **holes**: PLY has no hole encoding — an n-gon loop is all the parser sees, so the fan will fill
  any hole (`polygon_WithHole` pins exactly this, §7.1). Hole-preserving triangulation is an
  explicit non-goal.

### 4.5 Small extras worth taking

- **Object name cleanup** — ✅ **done** (`PLYImporter.java:73`): the object name is now
  `f.getName().replaceAll("(?i)\\.ply$", "")`, matching the script. Decision recorded: the
  **object** gets the bare basename, while `scene.setName(...)` keeps the full file name with
  extension. Only edge case left is a file named exactly `.ply`, which yields an empty object
  name — `MeshEditor`-style fallback ("Mesh 1") would fix it if it ever matters.
- **Sanity check on zero-count elements** — still open: the reader covers a missing x/y/z property,
  but a header declaring `element vertex 0` yields an empty mesh instead of an error. Low value
  (nothing to import either way); add only if a real file shows up.
- **Optional success/feedback message** after a long import (the script's final `INFORMATION`
  dialog) — low priority, arguably noise.

> **Housekeeping status** (both PLY files were rewritten, so this is now settled):
> - **GPL header**: both files carry `Copyright (C) 2026 by Maksim Khramov` (`PLYImporter:1`,
>   `PLYReader:1`). ⚠️ Per AGENTS.md §7 a `Changes copyright (C) 2026 by …` line is due on the
>   substantially rewritten `PLYReader` — it needs a real name, so it is left for the maintainer to
>   fill in rather than being invented here.
> - **Formatting**: do **not** reformat to Allman/2-space. `AGENTS.md` §7 is out of date on this
>   point; the core module is uniformly 4-space K&R (431 of 431 class declarations found, e.g.
>   `Scene.java`), and both PLY files match it. `PLYReader` uses arrow `switch` expressions
>   (Java 17-safe) with `NeedBraces`-satisfying block bodies.
> - **Checkstyle is advisory, not a gate**: `ignoreFailures = true`, `maxWarnings = MAX_VALUE`,
>   `severity = warning`, and its rules disagree with AGENTS.md — `LineLength` max is **160**
>   (not 72) and there is no indentation rule. ✅ **`PLYReader` and `PLYImporter` are now free of
>   checkstyle violations** (`:Translators:checkstyleMain`): `NeedBraces`, `LineLength` and
>   `LeftCurly` are all cleared. The only remaining hits on those files are the
>   `NewlineAtEndOfFile`/CRLF reports on line 1, which fire on untouched files too
>   (`OBJImporter`, `OBJExporter`, `PLYTranslator`) because of `core.autocrlf=input` — ignore them.

## 5. What NOT to copy from the script

| Script behavior | Reason |
|---|---|
| Hardcoded `getFloat()` for x/y/z | breaks `double`-precision vertices; `PLYReader`'s typed `readScalarValue()` is superior — keep it |
| Hardcoded 1-byte face count / 4-byte indices | the header already carries `countType`/`elemType`; use it |
| `currentOffset` accumulation bug | it never resets between elements, so a `vertex` element placed *after* another element gets wrong offsets; the Java header model avoids this by construction |
| Direct scene mutation without `UndoRecord` | acceptable nowhere; if "import into current window" is ever added, it must wrap in an `UndoRecord` per `AGENTS.md` §5 |

## 6. Order of work

1. ✅ **Fix `readBinary`** with the offset technique (§4.1) — binary PLY in both byte orders is
   importable; `airplane_binary` and `cube_binary_big_endian` went green on this step alone.
2. ✅ **Fix ASCII** to map x/y/z by header property *order index* instead of "first three tokens"
   (`cube_color_ascii` pinned this: `red green blue` precede `x y z`).
3. ✅ **Polygon fan triangulation** in both paths (§4.4) — `platonic_cube`, `platonic_cube_binary`,
   `platonic_cube_color_prepend`, `platonic_dodecahedron` and `polygon_WithHole` all green.
4. ⏸ **Scale / center / ground dialog** in `PLYImporter` (§4.2–4.3), exposing bounds out of
   `PLYReader` — **deferred by decision**, together with the bounds scan it needs. Nothing in the
   reader has to change first: when scheduled, prefer `TriangleMesh.getBounds()` over a new return
   type (§4.2).
5. ✅ **Header hardening**: `int64`/`long`/`uint64` are now recognised (`long`/`int64` as scalar
   values, `ulong`/`uint64` read through `getLong`); unknown types are rejected at header-parse time
   with the property name, instead of failing later inside a read. Checkstyle is clean on both PLY
   files. ⚠️ The `Changes copyright (C) 2026 by …` line is still pending a name (§4.5).

Verified state after 1–3 + 5 (and after the PR #355 review follow-ups, §9):
`./gradlew :Translators:test :Translators:checkstyleMain :Translators:checkstyleTest` →
**23 tests, 0 failures**; no checkstyle violations in `PLYReader`/`PLYImporter`/`PLYReaderTest`.

## 7. Tests

### 7.1 In the tree

Location: `Translators/src/test/resources/artofillusion/translators/`, driven by
`PLYReaderTest`. Status verified by running the suite: **23 tests, 0 failures** — 13 fixture-driven
tests plus 10 tests that generate small malformed files in a JUnit `@TempDir` (§7.3).

| Test / fixture | Shape | What it pins | Status |
|---|---|---|---|
| `airplane` / `airplane.ply` | ASCII, x/y/z only, 1335 v / 2452 triangles | baseline ASCII | ✅ passes |
| `airplane_binary` / `airplane_binary.ply` | binary LE, float32 x/y/z, 1335 v / 2452 triangles, `property list uchar uint vertex_indices` | binary fast path; parity with the ASCII original (same 1335/2452) | ✅ passes |
| `platonic_cube` / `cube_ascii.ply` | ASCII, 8 v, 6 quads | fan triangulation → 12 triangles | ✅ passes |
| `platonic_cube_binary` / `cube_binary.ply` | binary LE, float32 x/y/z, 8 v, 6 quads, `property list uchar uint vertex_indices` | binary fast path **+** fan → 12 triangles | ✅ passes |
| `platonic_cube_color_prepend` / `cube_color_ascii.ply` | ASCII, `uint8 red/green/blue` **before** x/y/z, 8 v, 6 quads | x/y/z by property order + fan → 12 triangles | ✅ passes |
| `platonic_dodecahedron` / `dodecahedron_ascii.ply` | ASCII, 20 v, 12 pentagons | fan → 36 triangles, 20 vertices | ✅ passes |
| `polygon_WithHole` / `face-with-hole.ply` | ASCII, 8 v, one 8-index loop (outer + inner ring, coplanar) | fan → 6 triangles, 8 vertices | ✅ passes |
| `icosahedron` / `icosahedron.ply` | ASCII, `double` x/y/z **plus** `nx/ny/nz`, 12 v, 20 triangles | `double`-precision text values + trailing per-vertex properties → 20 triangles, 12 vertices | ✅ passes |
| `cube_binary_big_endian` / `cube_binary_big_endian.ply` | binary **BE**, `float32` x/y/z (0/1 cube), 8 v, 12 triangles, `property list uint8 uint32 vertex_indices` | big-endian dispatch for vertices **and** indices, plus the `float32`/`uint8`/`uint32` aliases → 12 triangles, 8 vertices | ✅ passes |
| `colored_faces` / `tet.ply` | ASCII, 4 v, 4 triangles, face element carries `uchar red/green/blue` **after** `vertex_indices`; a `comment` sits inside the vertex element block | trailing face properties and comments inside an element → 4 triangles, 4 vertices | ✅ passes |
| `plane_with_uint32_list_declaration` / `airplane_2.ply` | ASCII, 1335 v / 2452 triangles, `property list uint32 uint32 vertex_indices` (count type 4 bytes wide) | wide **list count** declaration + `uint32` aliases accepted in text mode → 2452 triangles, 1335 vertices | ✅ passes |
| `quad_binary_extras` / `quad_binary_extras.ply` | binary LE, **`double`** x/y/z + trailing `float nx`, 4 v, an unused **`edge`** element between vertex and face, one quad, `property list uint32 uint32 vertex_indices` | fast-path offset math over an 28-byte record, unused-element skipping, wide binary list count, fan of a quad → 2 triangles, 4 vertices; y of vertex 2 asserted at `1e-12` so a float truncation of the `double` input fails | ✅ passes |
| `tet_binary_mixed` / `tet_binary_mixed.ply` | binary LE, **`int64`** x/y/z, a `property list uchar int extra` **inside** the vertex element (before x/y/z), 4 v, 4 triangles, `property list uchar ushort vertex_indices` | sequential fallback, 64-bit scalar decoding, 1-byte count + 2-byte index widths, property-order mapping → 4 triangles, 4 vertices; one vertex's coordinates asserted exactly | ✅ passes |

Fixture details to respect when changing the reader or the fixtures:

- `cube_binary_big_endian.ply` is a cleanly-sized file (469 bytes = header 217 + 8×12 vertices +
  12×13 triangle records, all indices in range), and it is **triangles only**, so it isolates the
  byte-order dispatch from polygon handling — it passed on §4.1 alone, before §4.4 landed.
- `airplane_2.ply` is `airplane.ply` with one header change only — `property list uint32 uint32
  vertex_indices` instead of `uint8 int32` (+2 bytes, exactly the header text delta; the 1335 vertex
  lines and 2452 triangle lines are otherwise identical, all indices in range). It exercises the
  count width in **text** mode only, where the declared width is irrelevant; the binary counterpart
  is `quad_binary_extras.ply`.
- `cube_color_ascii.ply` declares its colour channels as `uint8` (they were `byte`, which made the
  `255` value out of range for a signed `int8`), so every value in the fixture is in range and the
  `uint8` alias must stay handled.
- `airplane_binary.ply` and `cube_binary.ply` both use `uint` (not `int`) as the index element type,
  so the binary face path must go through the declared `elemType`, not assume `int`;
  `cube_binary_big_endian.ply` uses the `uint32` alias and `tet_binary_mixed.ply` uses `ushort`, so
  three widths and the alias table are covered.
- `cube_binary.ply` is the only fixture that combines binary data **and** quads, so it is the joint
  acceptance test for §4.1 + §4.4.
- `tet.ply` puts per-face colours (`uchar red/green/blue`) **after** `vertex_indices` in each face
  record, and a `comment` line inside the `element vertex` block. Both paths tolerate it by
  iterating all face properties rather than assuming the index list is the only one.
- `quad_binary_extras.ply` (484 bytes = header 336 + 4×28 vertices + 2×8 edges + 4+16 face) proves
  the `double` branch: if x/y/z were decoded as `float`, the `0.1` assertion fails by ~1.5e-9. It
  also contains an `edge` element that the reader never uses — if that skip is wrong, the face
  indices are garbage and the mesh construction fails.
- `tet_binary_mixed.ply` (445 bytes = header 301 + 4×29 vertices + 4×7 faces) is the counter-case to
  the fast path: the vertex element holds a list property, so the offset table would be wrong and the
  sequential walk is the only correct reading. If the list skip is off by one byte, the following
  `int64` x/y/z values are garbage.
- `face-with-hole.ply` is **not** a ring: PLY has no hole encoding, so it is a single 8-index loop
  over 4 outer + 4 inner vertices. Fan triangulation produces 6 triangles that *fill* the hole and
  even connect outer to inner vertices — the test pins that mechanical result (6 triangles), not
  geometry that preserves the hole. Correct hole handling would need outer/inner loop splitting
  and is explicitly out of scope.

### 7.2 Resolved test expectations

All assertions match the fan-triangulation semantics:

- `platonic_dodecahedron` — corrected from the copy-pasted 2452/1335 to the fixture's real shape,
  **36 triangles / 20 vertices**.
- `faceWithHole` → renamed `polygon_WithHole`, assertion corrected from the raw face count to
  **6 triangles** (fan of the single 8-index loop) / 8 vertices.
- `plane_with_uin32_list_declaration` → renamed `plane_with_uint32_list_declaration`, and its
  failure message now names `airplane_2.ply` instead of `airplane.ply`.
- `cube_binary_big_endian` failure message no longer says `cow.ply`, and the fixture file name is
  lowercase (`icosahedron.ply`) like the rest.

One naming residue to settle some day: the case is still called `polygon_WithHole` and
`face-with-hole.ply` still says "Single face with a hole (outer + inner loop)". Either rename both
to something neutral (`polygon8` / `octagon.ply`) or keep the name as documentation of the
limitation — but do not let the name imply that holes survive import (§7.1).

Non-blocking, now tidied: the local variables were renamed from the copy-pasted `airplane`/`icos`
to the shape under test (`cube`, `dodecahedron`, `polygonWithHole`, `icosahedron`, `tet`, `quad`,
`mesh`), and the `not found in classpath` messages name their own fixture.

### 7.3 Negative paths (asserted)

The review of PR #355 asked for negative tests rather than relying on the messages by inspection.
Ten cases now live in `PLYReaderTest` and build their input in a `@TempDir` (no repo fixture, so the
intent is readable next to the assertion):

| Test | Input | Rejected by |
|---|---|---|
| `rejectsOversizedDeclaredCount` | ASCII header `element vertex 2000000000`, no payload | `validateDeclaredCounts` — before any `new Vec3[count]` |
| `rejectsTruncatedBinaryPayload` | binary header declares 2 vertices, payload empty | same guard, binary minimum-record sizing |
| `rejectsNonIntegerListType` | `property list float int vertex_indices` | `requireIntegerType` at header-parse time |
| `rejectsUnknownPropertyType` | `property float16 x` | `requireSupportedType` |
| `rejectsDuplicateElement` | two `element vertex` blocks | duplicate-element guard in `parseHeader` |
| `rejectsMissingFaceElement` | vertex element only | `read()` precondition |
| `rejectsOutOfRangeFaceIndex` | face `3 0 1 5`, 3 vertices | `buildMesh()` index range check |
| `rejectsTruncatedAsciiRecord` | vertex line `0 0`, then a face line | ASCII token lookup |
| `rejectsNegativeListLength` | binary `property list char int`, count byte `0xFF` | `readListLength` (`count < 0`) |
| `acceptsVertexIndexAlias` | `property list uchar int vertex_index` | *(positive)* singular spelling is accepted |

### 7.4 Still missing

Every code path now has at least one fixture and every deliberate rejection has an assertion; what
follows is genuinely uncovered behaviour, worth a test only when it matters:

- **unsigned 64-bit positions** — `tet_binary_mixed` covers `int64`; `uint64` is decoded through
  `getLong()` and therefore wraps for values above 2⁶³ (harmless as a coordinate, but untested);
- **64-bit list counts or indices** — now rejected at header-parse time by `requireIntegerType`
  (an `IOException` naming the property), so the `IllegalArgumentException` fallback in
  `readIntValue` is unreachable; the rejection itself is asserted by `rejectsNonIntegerListType`;
- a face element whose **index list is not the first list property** (e.g. a list before
  `vertex_indices`) — handled by iterating face properties in order, but every fixture puts the
  indices first;
- an **ASCII** vertex element with a list property (the binary counterpart is `tet_binary_mixed`);
- a `face` element declared **before** `vertex` in the header (supported by iterating elements in
  header order, never exercised);
- degenerate triangles (`3 0 0 1`) — skipped with a debug log, not asserted.

## 8. Risks / open questions

| Risk | Mitigation |
|---|---|
| Binary fix changes face reading from hardcoded to header-driven → silent regressions for well-formed files | `airplane_binary` is already the parity baseline (same 1335/2452 as the ASCII original), and `cube_binary` / `cube_binary_big_endian` pin the LE/BE pair on a known mesh |
| Result-record / overload API choice (§4.2) grows into awkward public API | decide before implementing; the reader is internal to the `Translators` module, so churn is cheap |
| Dialog strings clashing with existing `Translators` bundle keys | `fileFilter.ply` and the new `importPLY` already exist; add the dialog-specific keys (`vertices`, `faces`, `centerAndGround`, title) to the default bundle only, following the same pattern |
| "Import into current window" (script behavior) vs "new scene + new window" (`Translator` contract) | out of scope here (see §1); if ever adopted, it requires `UndoRecord` and belongs to `PLYImporter` |
| `PLYReaderTest` covers decoding but not failure reporting | **Resolved**: ten negative tests assert every deliberate rejection (§7.3), including the header-driven allocation guard and the non-integer list type |
| Binary skip math assumes a 1-byte list count → misaligns on files like `airplane_2.ply` | **Resolved**: every skip is sized from `countType`/`elemType` widths via `advance()`, pinned by `quad_binary_extras` (4-byte count) and `tet_binary_mixed` (1-byte count + 2-byte indices) |
| A staged fixture is corrupt, so its test can never pass | **Resolved**: `cow.ply` (3 bytes short of its declared face count) was dropped and replaced by `cube_binary_big_endian.ply`, which is size-consistent and byte-validated. Keep validating new binary fixtures this way (size = header + Σ element sizes) before treating them as spec |
| Fan triangulation changes topology for quads/pentagons without any warning | matches the script's intent and the fixtures pin the counts (12, 36, 6); a concave or self-intersecting polygon now triangulates too, and one `log.debug` line reports how many polygons were fan-filled — nothing is shown to the user, so document it in the UI if users complain |
| The GPL `Changes copyright` line on `PLYReader` is still missing | ⚠️ open: needs a real name, so the maintainer must add it before this work is committed (AGENTS.md §7) |
| **`*.ply -text`** (added to `.gitattributes` for PR #355) means fixtures are stored byte-exact | ASCII fixtures are normalised to LF in the index and the working tree; generate new fixtures with LF endings (`\n`, not `WriteAllText` defaults) or the diff shows every line as changed |

## 9. PR #355 review outcome

Review of PR #355 (`simba87`, 2026-10-23) asked for two behavioural fixes plus negative tests, and
listed smaller follow-ups. State in this tree:

**Fixed**

1. **Header-driven allocation before payload validation** (the OOM/LoadError issue):
   `validateDeclaredCounts()` runs right after the header — and, for binary, right after
   `dataLength` is known but *before* `new Vec3[count]` / `new ArrayList<>(count)` — comparing the
   declared record counts against the bytes actually available (minimum record size: 1 byte in
   ASCII, the declared widths in binary, 64-bit sums). A crafted `element vertex 2000000000` now
   raises `IOException`, never `OutOfMemoryError`.
2. **Non-integer list types**: `listProperty()` calls `requireIntegerType()`, so `property list
   float int …` (or any 64-bit/unknown count or element type) fails at header-parse time with a
   message naming the property, instead of an `IllegalArgumentException` mid-read.
3. **Negative tests** (§7.3): 10 new tests, suite 13 → 23.
4. **Big-endian assertions**: `cube_binary_big_endian` now pins decoded coordinates
   (`(0,0,0)`, `(1,1,0)`, `(1,1,1)`), not just counts — a wrong byte order can keep the counts.
5. **Nits**: `readListLength` compares `count * size` against `remaining()`; `vertex_index` is an
   accepted alias after `vertex_indices`/`indices`; duplicate element declarations are rejected;
   `PLYReader` has a private constructor; `readIntValue` documents the 2³¹ ceiling;
   `PLYImporter.objFilter` → `plyFilter`, `importFile` throws `IOException`;
   `*.ply -text` in `.gitattributes` (fixtures normalised to LF); misleading test locals renamed;
   one `log.debug` line per import reports fan-filled polygons.

**Deferred (deliberate)**

- **Whole-file buffering** (`readBinary` reads the payload into one `byte[]`): fine up to a few
  hundred MB; a memory-mapped or streaming pass is a redesign, not a review fix.
- **Vertex attributes** (colours, normals, UVs) — still non-goal for this PR (§1); `tet.ply` shows
  per-face colours are parsed and dropped.
- **Point clouds / vertex-only files** — the reader still requires a `face` element (§7.3 item
  "missing face element" is asserted as an error); supporting them means producing a mesh-less
  `ObjectInfo`, a product decision.
- **`importPLY` in the 14 translated bundles** — the key exists in the default bundle only, so
  other locales now show the correct English title instead of the previously (wrong) translated
  `importOBJ` string. Needs real translations, not machine output.
- **`polygon_WithHole` / `face-with-hole.ply` naming** — kept as documentation of the fan-fill
  limitation (§7.1).
