# Tokonga (Art of Illusion) — Agent Guide

> **Purpose**: This file helps AI coding agents contribute effectively to the Tokonga repository.
> **Maintainer**: Maksim Khramov (`makiam` on GitHub)

---

## 1. Quick Start (Build & Run)

```bash
./gradlew assemble      # compile all modules, produce plugins JARs
./gradlew run           # launch the application (adds -Dsun.java2d.noddraw=true)
./gradlew test          # run tests (JUnit 5 + Mockito + Jemmy)
./gradlew aggregateJavadoc
```

- **Java**: toolchain 17 (CI uses 19 Temurin)
- **Gradle**: 9.2.1 (wrapper included)
- **Main class**: `artofillusion.ArtOfIllusion`
- **CI**: GitHub Actions on `macOS-latest` — builds DMG, does NOT run tests

---

## 2. Architecture at a Glance

### Module Structure

```
tokonga/
├── ArtOfIllusion/     ★ CORE (~397 Java files, 18 packages)
├── Tools/             Plugin: Extrude, Lathe, Skin, CSG, Tube, Text, Array
├── Renderers/         Plugin: RaytracerRenderer
├── Filters/           Plugin: Image filters (Blur, etc.)
├── Translators/       Plugin: File import/export (PLY, etc.)
├── Polymesh/          Plugin: Polygonal modeling (PolyMesh)
├── StandardModules/   Plugin: Procedural modules (RGB→HSV, Compare, Power)
├── Preferences/       Plugin: Preferences window
├── PrimitiveProviders/ Plugin: Primitive providers
├── OSSpecific/        Plugin: OS-specific features
├── default-theme-lib/ Library: Default theme
├── StandardTheme/     Plugin: Standard theme
├── xml-data/          Common XML resources (mappings.properties)
├── buildSrc/          Convention plugins (aoi.java-conventions, aoi.plugin-conventions)
├── Plugins/           Runtime plugin directory + blacklist
├── Scripts/           User scripts (Groovy)
└── InstallerSrc/      Installers (IZPack, Launch4j, DMG)
```

### Core Packages (`ArtOfIllusion/src/main/java/artofillusion/`)

| Package | Purpose |
|---|---|
| `(root)` | Scene, LayoutWindow, ArtOfIllusion, PluginRegistry, UndoRecord, SceneIO |
| `object/` | Object3D hierarchy, ObjectInfo — scene geometry |
| `animation/` | Track, Keyframe, Score, Skeleton, Pose |
| `math/` | Vec3, Mat4, CoordinateSystem, BoundingBox |
| `texture/` | Texture hierarchy, TextureMapping |
| `material/` | Material hierarchy, MaterialMapping |
| `ui/` | EditingTool, Translate (i18n), ThemeManager |
| `view/` | ViewerCanvas, SceneViewer — 3D viewport rendering |
| `script/` | ScriptRunner, Groovy/BeanShell engines |
| `procedural/` | Module system for procedural textures |
| `image/` | ImageMap, ImageFilter |
| `tools/` | PrimitivesMenu (built-in primitive creation) |
| `keystroke/` | Keyboard shortcut management |
| `unwrap/` | UV unwrapping |
| `util/` | SearchlistClassLoader, UndoableEdit, ThreadManager |

### Domain Model Chain

```
Scene
 └── List<ObjectInfo>
      ├── Object3D object        ← geometry (shared across duplicates!)
      ├── CoordinateSystem coords ← position + orientation
      ├── ObjectInfo parent      ← hierarchy
      ├── List<ObjectInfo> children
      ├── List<Track> tracks     ← animation
      ├── String name
      ├── boolean selected, visible
      └── Texture/Material mappings (inherited from Object3D)

 Object3D hierarchy:
 ┌── Cube, Sphere, Cylinder           (primitives)
 ├── TriangleMesh, SplineMesh, Curve  (meshes)
 ├── CSGObject                         (boolean operations)
 ├── Light → DirectionalLight, PointLight, SpotLight
 ├── SceneCamera, NullObject, ReferenceImage
 ├── ImplicitObject → ImplicitSphere, CompoundImplicitObject
 ├── ObjectCollection → ScriptedObject
 └── ObjectWrapper → Actor, ExternalObject
```

### Plugin System (`PluginRegistry.java`, 617 lines)

```
Startup: scanPlugins() → scan Plugins/*.jar → read extensions.xml
         → instantiate classes → registerPlugin() → check instanceof categories
         → notify APPLICATION_STARTING
```

**Plugin categories** (registered in `ArtOfIllusion.main()`):
`Plugin`, `Renderer`, `Translator`, `ModellingTool`, `Texture`, `Material`, `TextureMapping`, `MaterialMapping`, `ImageFilter`, `Module`, `PreferencesEditor`

**extensions.xml format** (parsed by XStream):
```xml
<extension name="Tools" version="3.0">
    <author>Peter Eastman</author>
    <plugin class="artofillusion.tools.ExtrudeTool">
        <export method="extrudeCurve" id="artofillusion.tools.ExtrudeTool.extrudeCurve"/>
    </plugin>
    <plugin class="artofillusion.tools.LatheTool"/>
</extension>
```

### Plugin Convention (buildSrc)

Any module using `aoi.plugin-conventions` auto-produces a JAR in `Plugins/`:
```groovy
plugins { id 'aoi.java-conventions' }
dependencies { implementation project(':ArtOfIllusion') }
tasks.withType(Jar) {
    destinationDirectory = file("${rootProject.projectDir}/Plugins")
}
```

---

## 3. Key Files for an Agent to Know

| File | Lines | Purpose |
|---|---|---|
| `ArtOfIllusion.java` | 703 | Entry point, init sequence, plugin registration |
| `PluginRegistry.java` | 617 | Plugin discovery, classloading, lifecycle |
| `Scene.java` | 1331 | Scene container — objects, textures, materials, images, selection |
| `ObjectInfo.java` | 531 | Object instance wrapper (geometry + transform + tracks + hierarchy) |
| `Object3D.java` | 589 | Abstract geometry base class |
| `UndoRecord.java` | 453 | Command-pattern undo/redo with disk caching |
| `LayoutWindow.java` | 2621 | Main window — menus, tool palette, 4 viewports, score |
| `SceneViewer.java` | ~800 | 3D viewport — mouse handling, hit testing, tool dispatch |
| `EditingTool.java` | ~300 | Base class for viewport tools |
| `ModellingTool.java` | 23 | Interface for menu commands |
| `Plugin.java` | 105 | Lifecycle interface (APPLICATION_STARTING, SCENE_WINDOW_CREATED, etc.) |
| `SceneIO.java` | 212 | Serialization helpers for .aoi format |
| `CoordinateSystem.java` | ~400 | Position + orientation (dual representation: vectors + Euler angles) |
| `Track.java` | ~200 | Abstract animation track |
| `Score.java` | 1202 | Animation timeline widget |
| `mappings.properties` | — | Class name translations for backward .aoi compatibility |

---

## 4. Common Tasks — Step by Step

### Add a New 3D Primitive

1. Create class in `artofillusion.object`:
```java
public class Torus extends Object3D
{
  // --- Required abstract methods ---
  public Object3D duplicate()
  {
    return new Torus(this);
  }

  public void copyObject(Object3D obj)
  {
    Torus t = (Torus) obj;
    // copy fields...
  }

  public BoundingBox getBounds()
  {
    return new BoundingBox(-outerR, outerR, -outerR, outerR, -innerR, innerR);
  }

  public void setSize(double x, double y, double z)
  {
    // resize based on bounding box dimensions
  }

  public WireframeMesh getWireframeMesh()
  {
    // return wireframe representation
  }

  // --- Serialization (BINARY, not XML!) ---
  public void writeToStream(DataOutputStream out, Scene scene) throws IOException
  {
    super.writeToStream(out, scene);  // writes texture/material
    out.writeDouble(outerR);
    out.writeDouble(innerR);
    // write all fields...
  }

  public Torus(DataInputStream in, Scene scene) throws IOException
  {
    super(in, scene);  // reads texture/material
    outerR = in.readDouble();
    innerR = in.readDouble();
    // read all fields...
  }

  // --- Optional ---
  public boolean isEditable() { return true; }

  public void edit(EditingWindow window, Runnable callback)
  {
    // show editing dialog
  }
}
```

2. Register in `PrimitivesMenu` or create a `ModellingTool`.
3. Add to `mappings.properties` if renaming/moving an existing class.

### Add a Menu Command (ModellingTool)

```java
public class MyCommand implements ModellingTool
{
  public String getName()
  {
    return "My Command";  // or Translate.text("menu.myCommand")
  }

  public void commandSelected(LayoutWindow window)
  {
    Scene scene = window.getScene();
    // ... show dialog, compute result ...

    // MUST wrap in UndoRecord:
    UndoRecord undo = new UndoRecord(window, false);
    ObjectInfo info = scene.getObject(selectedIndex);
    undo.addCommand(UndoRecord.SET_OBJECT, info, info.getObject().duplicate());

    // Apply change:
    info.setObject(newObject);

    // Commit undo:
    window.setUndoRecord(undo);
    window.updateImage();
    window.setModified(true);
  }
}
```

Register in `extensions.xml`:
```xml
<plugin class="artofillusion.tools.MyCommand"/>
```

### Add a Viewport Tool (EditingTool)

```java
@ButtonImage("myTool")          // icon: myTool.png in resources
@Tooltip("myTool.tipText")      // i18n key
@ActivatedToolText("myTool.help") // status bar text
public class MyViewportTool extends EditingTool
{
  public MyViewportTool(EditingWindow fr)
  {
    super(fr);
  }

  public int whichClicks()
  {
    return OBJECT_CLICKS;  // or ALL_CLICKS, HANDLE_CLICKS
  }

  public void mousePressed(WidgetMouseEvent e, ViewerCanvas view)
  {
    // handle click
  }

  public void mouseDragged(WidgetMouseEvent e, ViewerCanvas view)
  {
    // handle drag
  }

  public void mouseReleased(WidgetMouseEvent e, ViewerCanvas view)
  {
    // handle release — commit changes with UndoRecord
  }

  public void drawOverlay(ViewerCanvas view)
  {
    // optional: custom rendering on viewport
  }
}
```

Register in editor window constructor:
```java
tools.addTool(new MyViewportTool(this));
```

### Add a File Translator

1. Implement the `Translator` interface (see `Translators/` module for examples).
2. Register via `extensions.xml`.

### Add a Renderer

1. Implement `Renderer` interface:
```java
public class MyRenderer implements Renderer
{
  public void renderScene(Scene scene, Camera camera,
                          RenderListener listener, SceneCamera sceneCamera)
  {
    // spawn render thread, call listener when done
  }

  public void cancelRendering(Scene scene) { /* cancel */ }
  public BPanel getConfigPanel() { /* settings UI */ }
  // ...
}
```

2. Register via `extensions.xml`.

### Fix a UI Bug

1. Find the relevant class — search menus/dialogs first (NOT `LayoutWindow` directly).
2. Buoy event handling: `widget.addEventLink(MousePressedEvent.class, handler)`.
3. **Always** use `UndoRecord` when altering Scene/ObjectInfo/Object3D data.
4. Call `window.setModified(true)` after changes.
5. For repaint: `view.repaint()` or `window.updateImage()`.

---

## 5. Undo/Redo — The Critical Pattern

**Every persistent change to the scene MUST go through `UndoRecord`.**

```java
// Basic pattern:
UndoRecord undo = new UndoRecord(window, false);
undo.addCommand(UndoRecord.SET_OBJECT, objectInfo, objectInfo.getObject().duplicate());
// ... apply change ...
objectInfo.setObject(newObject);
window.setUndoRecord(undo);

// Adding an object:
UndoRecord undo = new UndoRecord(window, false);
undo.addCommand(UndoRecord.ADD_OBJECT, newInfo, scene.getNumObjects());
scene.addObject(newInfo);
window.setUndoRecord(undo);

// Modifying vertices:
UndoRecord undo = new UndoRecord(window, false);
undo.addCommand(UndoRecord.COPY_VERTEX_POSITIONS, mesh, mesh.getVertexPositions().clone());
mesh.setVertexPositions(newPositions);
window.setUndoRecord(undo);

// Custom undo action:
UndoRecord undo = new UndoRecord(window, false, new UndoableEdit() {
  public void undo() { /* reverse the change */ }
  public void redo() { /* apply the change */ }
  public String getName() { return "My Action"; }
});
window.setUndoRecord(undo);
```

**UndoRecord command types:** `COPY_OBJECT(0)`, `COPY_COORDS(1)`, `COPY_OBJECT_INFO(2)`, `SET_OBJECT(3)`, `ADD_OBJECT(4)`, `DELETE_OBJECT(5)`, `RENAME_OBJECT(6)`, `ADD_TO_GROUP(7)`, `REMOVE_FROM_GROUP(8)`, `SET_GROUP_CONTENTS(9)`, `SET_TRACK(10)`, `SET_TRACK_LIST(11)`, `COPY_TRACK(12)`, `COPY_VERTEX_POSITIONS(13)`, `COPY_SKELETON(14)`, `SET_MESH_SELECTION(15)`, `SET_SCENE_SELECTION(16)`, `USER_DEFINED_ACTION(1000)`

---

## 6. Dependencies

| Library | Version | Role |
|---|---|---|
| **Buoy** | 1.1.5 | Swing-based UI framework (custom, `com.github.makiam:Buoy`) |
| **JOGL** | 2.6.0 | OpenGL rendering in viewports |
| **Groovy** | 5.0.7 | Scripting (replaced BeanShell) |
| **RSyntaxTextArea** | 3.6.3 | Code editor widget |
| **XStream** | 1.4.21 | XML parsing (extensions.xml only) |
| **EventBus** | 3.3.1 | Publish-subscribe between components |
| **Logback + SLF4J** | 1.5.37 / 2.0.18 | Logging |
| **Lombok** | 1.18.46 | `@Getter`, `@Setter`, `@Slf4j`, `@NoArgsConstructor` |
| **JAMA** | 1.0.3 | Linear algebra (matrices) |
| **svgSalamander** | 1.1.3 | SVG icon support |

---

## 7. Code Style (from `docs/Contributing.md`)

```java
// Allman braces — opening brace on NEW line
public class Example
{
  public void method(boolean param)
  {
    if (condition)
      System.out.println("Indent body 2 spaces, no braces for single statements");

    for (Object obj : collection)
      obj.process();
  }

  // Long parameter lists stack vertically
  public void longMethod(boolean first,
                         String second,
                         int third,
                         String fourth)
  {
    // max line length: 72 characters
  }
}
```

- **Lombok** is used extensively: fields annotated `@Getter @Setter` skip explicit accessor methods.
- **Logging**: use `@Slf4j` annotation, then `log.info(...)`, `log.atError().setCause(ex).log(...)`.
- **i18n**: use `Translate.text("key")` for user-visible strings, `Translate.menu("key")` for menus.

---

## 8. Pitfalls & Gotchas

| Pitfall | Details |
|---|---|
| **`LayoutWindow` god class** | 2621 lines. Most UI changes touch it. Search for specific menu/dialog classes first. |
| **Low test coverage** | ~89 test files for 212K LOC. CI does NOT run tests. Manual testing is essential. |
| **Binary `.aoi` format** | Changing domain classes? Update `mappings.properties` (backward compat) and consider `@ImplementationVersion`. |
| **Threading** | UI = Swing EDT. Rendering = separate threads (`ThreadManager`). EventBus for cross-component. No cross-thread mutations without sync. |
| **Classloader complexity** | Plugins use `SearchlistClassLoader`. Load classes by name via `ArtOfIllusion.getClass(name)`, not `Class.forName()`. |
| **Java version** | Toolchain = 17, CI = 19. Don't use features unavailable in 17. |
| **Active refactoring** | ~25 feature branches in progress. Coordinate with maintainer before large changes. |
| **Object3D shared instances** | Multiple `ObjectInfo` can reference the SAME `Object3D`. Modifying geometry affects all instances. Use `objectModified()` to invalidate caches. |
| **UndoRecord is mandatory** | Any scene change without `UndoRecord` = user can't undo = broken UX. |
| **Plugin blacklist** | `Plugins/blacklist` file blocks plugins by JAR name. Check it if a plugin isn't loading. |
| **Buoy is undocumented** | No external docs. Study existing UI code or Buoy source at `com.github.makiam:Buoy`. |

---

## 9. File Format (.aoi)

- **Format**: GZipped binary stream (not XML!)
- **Prefix**: `{'A', 'o', 'I', 'S', 'c', 'e', 'n', 'e'}` (8 bytes)
- **Version**: `short` — current = 6, minimum = 2
- **Structure**: images → materials → textures → objects (with tracks) → hierarchy → environment → metadata
- **Serialization**: `DataOutputStream`/`DataInputStream` — every `Object3D` subclass must implement `writeToStream()` and a constructor from `DataInputStream`
- **Deduplication**: shared `Object3D` instances written once, referenced by index
- **Class resolution**: `ArtOfIllusion.getClass(className)` + `classTranslations` map for moved/renamed classes
- **Buffered writes**: data written to `ByteArrayOutputStream` first, then length-prefixed — enables skip on read errors

---

## 10. Testing

```bash
./gradlew test                        # run all tests
./gradlew :ArtOfIllusion:test         # run core module tests only
./gradlew :StandardModules:test       # run procedural module tests
```

- **Framework**: JUnit 5 (`jupiter`) + Mockito + Jemmy (GUI testing)
- **Best covered**: `StandardModules` (procedural), `Filters`, `Translators`
- **CI**: builds on macOS, does NOT run tests — always test locally
- **Test location**: each module's `src/test/java/`

---

## 11. When to Update This File

- New extension points added (plugin categories, new interfaces)
- Build commands or mandatory dependencies change
- Java version requirement changes
- Major architectural changes (e.g., LayoutWindow split)