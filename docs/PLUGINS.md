# Plugin System Architecture

This document describes the plugin system of Tokonga (Art of Illusion) — how plugins work, how to build them, and what already exists.

---

## 1. Overview

All features beyond the minimal core are plugins. The core (`ArtOfIllusion/`) contains the scene graph, windows, base texture/material/mapping types, and primitive Object3D classes (`Cube`, `Sphere`, `Tube`, `Curve`, etc.). Everything else — renderers, modeling tools, import/export formats, procedural modules, image filters — lives in separate modules that compile as plugin JARs into `Plugins/`.

The plugin system is **not** an add-on mechanism; it is the primary architectural pattern.

---

## 2. Plugin Lifecycle

Every plugin implements the `artofillusion.Plugin` interface, which provides lifecycle hooks via `processMessage(int, Object...)` or individual default methods:

| Message | Constant | When | Arguments |
|---------|----------|------|-----------|
| `APPLICATION_STARTING` | 0 | After init, before first window | none |
| `APPLICATION_STOPPING` | 1 | Shutdown (via shutdown hook) | none |
| `SCENE_WINDOW_CREATED` | 2 | LayoutWindow created, before display | `LayoutWindow` |
| `SCENE_WINDOW_CLOSING` | 3 | LayoutWindow closing | `LayoutWindow` |
| `SCENE_SAVED` | 4 | Scene written to disk | `File`, `LayoutWindow` |
| `OBJECT_WINDOW_CREATED` | 5 | Object editor opened | `ObjectEditorWindow` |
| `OBJECT_WINDOW_CLOSING` | 6 | Object editor closing | `ObjectEditorWindow` |

Implement `Plugin` directly, or override specific `on*()` methods:

```java
public class MyPlugin implements Plugin {
    @Override
    public void onApplicationStarting() {
        // one-time initialization
    }

    @Override
    public void onSceneWindowCreated(LayoutWindow window) {
        // add menus, toolbars, etc. to each new window
    }
}
```

---

## 3. Extension Points (Categories)

A **category** is a class or interface that defines an extension point. After `PluginRegistry.addCategory(SomeInterface.class)`, any registered plugin implementing that interface is automatically added to its category list.

**Core categories** — registered in `ArtOfIllusion.main()` via `PluginRegistry.addCategory()`:

| Category Interface | What It Adds | Examples |
|--------------------|--------------|----------|
| `Plugin` | Lifecycle hooks | PreferencesPlugin, PolyMeshPlugin |
| `Renderer` | Render engines | Raster, RaytracerRenderer |
| `Translator` | File import/export | OBJTranslator, PLYTranslator |
| `ModellingTool` | Menu commands | ExtrudeTool, CSGTool |
| `Texture` | Texture types | UniformTexture, ImageMapTexture |
| `Material` | Material types | UniformMaterial, ProceduralMaterial3D |
| `TextureMapping` | Texture mapping algorithms | UVMapping, SphericalMapping |
| `MaterialMapping` | Material mapping algorithms | LinearMaterialMapping |
| `ImageFilter` | Post-render image filters | BlurFilter, GlowFilter |
| `procedural.Module` | Procedural texture/material nodes | NoiseModule, RGBModule |
| `PreferencesEditor` | Preferences window pages | PreferencesPlugin |

**Plugin-registered categories** — declared via `<category class="..."/>` in a plugin's `extensions.xml`:

| Category Interface | Registered By | What It Adds |
|--------------------|---------------|--------------|
| `PrimitiveFactory` | PrimitiveProviders | Object creation menu entries (CubeFactory, SphereFactory, etc.) |
| `RTObjectFactory` | Renderers | Raytracer object factories |
| `PhotonSourceFactory` | Renderers | Photon source factories |

Retrieve plugins of any category:

```java
List<Renderer> renderers = PluginRegistry.getPlugins(Renderer.class);
```

---

## 4. Plugin Descriptor: extensions.xml

Every plugin JAR **must** contain `extensions.xml` at the resource root (`src/main/resources/extensions.xml`). It is parsed by XStream.

### Complete reference

```xml
<extension name="My Plugin" version="1.0">
    <author>Your Name</author>
    <date>2026</date>
    <description>What this plugin does.</description>

    <!-- Declare dependency on another plugin (by extension name) -->
    <import name="Translators"/>

    <!-- Register a new extension point (rare) -->
    <category class="mypackage.MyInterface"/>

    <!-- Plugin classes to instantiate -->
    <plugin class="mypackage.MyPlugin">
        <!-- Export a method for cross-plugin invocation -->
        <export method="doSomething" id="mypackage.MyPlugin.doSomething"/>
    </plugin>
    <plugin class="mypackage.MyTool"/>

    <!-- Resources: translation bundles, themes, preferences -->
    <resource type="TranslateBundle" id="myPlugin" name="Bundle"/>
    <resource type="UITheme" id="myPlugin" name="myPlugin/theme.xml"/>

    <!-- Version history (optional) -->
    <history>
        <log version="1.0" date="2026" author="Your Name">
            Initial release.
        </log>
    </history>
</extension>
```

### Legacy format

For backward compatibility, JARs can also contain a plain-text `plugins` file listing one class name per line. No `<import>`, `<export>`, or `<resource>` support in this format.

---

## 5. Plugin Discovery and Loading

At startup, `PluginRegistry.scanPlugins()` (called from `ArtOfIllusion.main()`) performs these steps:

1. **Enumerate**: scan `Plugins/` directory for all `.jar` files
2. **Blacklist**: if `Plugins/blacklist` exists, skip any JAR listed by filename
3. **Parse descriptors**: read `extensions.xml` (or `plugins`) from each JAR → build `JarInfo` objects
4. **Resolve dependencies**: process JARs in dependency order. If JAR A imports extension name B, A is deferred until B is loaded. Unresolvable imports cause a load error for that JAR.
5. **Create classloaders**:
   - No imports: plain `URLClassLoader`
   - Has imports: `SearchlistClassLoader` that chains the JAR's own loader with its imports' loaders
6. **Instantiate**: load each `<plugin class="...">`, reflectively invoke no-arg constructor, call `registerPlugin()`
7. **Categorize**: check each plugin instance against all registered categories (`instanceof` check)
8. **Register exports and resources**: make exported methods and localized resources available
9. **Notify**: call `processMessage(APPLICATION_STARTING)` on all `Plugin` instances

### Classloader isolation

Each plugin JAR gets its own classloader. Plugins **cannot** see each other's classes unless explicitly linked via `<import>`. This means:

- If plugin A needs a class from plugin B, A must `<import name="B"/>` in its `extensions.xml`
- Core classes are visible to all plugins via the parent classloader
- Load classes by name via `ArtOfIllusion.getClass(name)` (searches all plugin classloaders), not `Class.forName()` (searches only the caller's classloader)

---

## 6. Building a Plugin

### Gradle convention

Apply `aoi.plugin-conventions` (defined in `buildSrc/aoi.plugin-conventions.gradle`):

```groovy
plugins {
    id 'aoi.plugin-conventions'
}
```

This convention:
- Applies `aoi.java-conventions` (Java 17 toolchain, checkstyle, test config)
- Adds `implementation project(':ArtOfIllusion')` as a dependency
- Sets JAR output directory to `$rootDir/Plugins/`

No additional configuration is required.

### Cross-plugin compile dependencies

Most plugins depend only on the core. The one exception is **Polymesh**, which depends on **Translators** at compile time:

```groovy
// Polymesh/build.gradle
dependencies {
    implementation project(':Translators')
}
```

This is a **compile-time** dependency (Gradle `dependencies`). It is independent of the **runtime** `<import>` mechanism in `extensions.xml`.

### Runtime vs compile-time dependencies

| Aspect | Compile-time (Gradle) | Runtime (extensions.xml) |
|--------|----------------------|--------------------------|
| Defined in | `build.gradle` | `extensions.xml` |
| Purpose | Class visibility during compilation | Classloader linking at startup |
| Failure mode | Compilation error | Plugin skipped with error log |
| Example | `implementation project(':Translators')` | `<import name="Translators"/>` |

A plugin can have a runtime `<import>` without a compile dependency (if it only calls imported classes reflectively) or vice versa (if it needs the classes to compile but handles absence gracefully at runtime).

### Registration in settings.gradle

New plugin modules must be added to the root `settings.gradle`:

```groovy
include ':ArtOfIllusion', ':Filters', ':Tools', ':Renderers', ...
```

Without this, the module is not part of the build.

---

## 7. Cross-Plugin Communication

### Exported methods

A plugin can expose methods for other plugins to call without direct class imports:

**Declare in extensions.xml:**
```xml
<plugin class="mypackage.MyPlugin">
    <export method="download" id="artofillusion.SPManager.download"/>
</plugin>
```

**Invoke from any code:**
```java
Object result = PluginRegistry.invokeExportedMethod(
    "artofillusion.SPManager.download", frame, url
);
```

**List available exports:**
```java
List<String> ids = PluginRegistry.getExportedMethodIds();
```

### Manual registration

Plugins can register other plugins or resources programmatically:

```java
// Register a plugin instance (used for built-in plugins in ArtOfIllusion.main())
PluginRegistry.registerPlugin(new MyTool());

// Register a localized resource
PluginRegistry.registerResource("TranslateBundle", "myPlugin",
    myClassLoader, "myPlugin.Bundle", null);
```

---

## 8. Existing Plugins

### Active plugins (included in build)

| Module | Extension Name | Classes | Categories | Runtime Imports |
|--------|---------------|---------|------------|-----------------|
| **Renderers** | `Renderers` | RaytracerRenderer, Raster | Renderer | — |
| **Tools** | `Tools` | ExtrudeTool, LatheTool, SkinTool, CSGTool, TubeTool, TextTool, ArrayTool | ModellingTool | — |
| **Translators** | `Translators` | OBJTranslator, POVTranslator, PLYTranslator, VRMLTranslator | Translator | — |
| **Filters** | `Image Filters` | BlurFilter, BrightnessFilter, DepthOfFieldFilter, ExposureFilter, GlowFilter, NoiseReductionFilter, OutlineFilter, SaturationFilter, TintFilter | ImageFilter | — |
| **StandardModules** | `Standard Procedural Modules` | ~49 procedural modules (Noise, Cells, RGB, HSV, Compare, Blend, Turbulence, etc.) | procedural.Module | — |
| **PrimitiveProviders** | `Primitive Providers` | CubeFactory, SphereFactory, CylinderFactory, ConeFactory, TorusMeshProvider, ScriptedObjectProvider, CameraFactory, 5 light factories, ReferenceImageFactory, NullFactory | PrimitiveFactory | — |
| **Polymesh** | `PolyMesh` | PolyMeshPlugin, PMOBJTranslator, PolymeshSettingsPage, PMAssetsWatcher | Plugin, Translator, PreferencesEditor | **Translators, PreferencesPlugin** |
| **Preferences** | `PreferencesPlugin` | PreferencesPlugin (exports 16 API methods) | Plugin, PreferencesEditor | — |
| **StandardTheme** | `Standard Theme` | (resources only — UITheme) | — | — |
| **OSSpecific** | `OSSpecific` | MacOSPlugin | Plugin | — |

### Disabled plugins (commented out in settings.gradle)

| Module | Extension Name | Notes |
|--------|---------------|-------|
| **SPManager** | `SPManager` | Plugin/script manager. Imports: StandardTheme, PreferencesPlugin. **Disabled: no active remote plugin repository.** |
| **PostInstall** | `PostInstall` | Post-install cleanup. Distributed with SPManager. **Disabled with SPManager.** |

### Built-in registrations (in core, not plugins)

These are registered directly in `ArtOfIllusion.main()` before `scanPlugins()`:

- **Textures**: UniformTexture, ImageMapTexture, ProceduralTexture2D, ProceduralTexture3D
- **Materials**: UniformMaterial, ProceduralMaterial3D
- **Mappings**: UniformMapping, ProjectionMapping, CylindricalMapping, SphericalMapping, UVMapping, LinearMapping3D, LinearMaterialMapping
- **Internal**: AssetsFolderWatcher, LoggingListener

---

## 9. Minimal Functional Set

Without these plugins, the application cannot start or is severely degraded:

| Plugin | Why Critical |
|--------|-------------|
| **StandardTheme** | **Mandatory.** Application hangs at splash screen without it. |
| **Renderers** | No preview or render capability |
| **Tools** | No modeling operations (extrude, lathe, skin, CSG) |
| **PrimitiveProviders** | No way to create objects from menus |
| **StandardModules** | Procedural texture/material editors are empty |

### Dependency graph

```
Polymesh ──compile──→ Translators
         ──runtime──→ Translators
         ──runtime──→ PreferencesPlugin

SPManager ──runtime──→ StandardTheme
          ──runtime──→ PreferencesPlugin
```

All other plugins are independent.

---

## 10. How to Create a New Plugin

### Step 1: Create the module directory

```
MyPlugin/
├── build.gradle
└── src/
    ├── main/
    │   ├── java/
    │   │   └── mypackage/
    │   │       └── MyPlugin.java
    │   └── resources/
    │       └── extensions.xml
    └── test/
        └── java/
            └── mypackage/
                └── MyPluginTest.java
```

### Step 2: Write build.gradle

```groovy
plugins {
    id 'aoi.plugin-conventions'
}
```

### Step 3: Register in settings.gradle

Add `':MyPlugin'` to the `include` list in the root `settings.gradle`.

### Step 4: Write extensions.xml

```xml
<?xml version="1.0" standalone="yes"?>
<extension name="My Plugin" version="1.0">
    <author>Your Name</author>
    <date>2026</date>
    <description>Description of what this plugin does.</description>
    <plugin class="mypackage.MyPlugin"/>
</extension>
```

### Step 5: Implement the plugin class

```java
package mypackage;

import artofillusion.Plugin;
import artofillusion.LayoutWindow;

public class MyPlugin implements Plugin {

    @Override
    public void onApplicationStarting() {
        // Initialize once at startup
    }

    @Override
    public void onSceneWindowCreated(LayoutWindow window) {
        // Add UI elements to each new scene window
    }
}
```

### Step 6: Build and test

```bash
./gradlew :MyPlugin:assemble    # compile and produce JAR in Plugins/
./gradlew :MyPlugin:test        # run tests
./gradlew run                   # launch application with plugin loaded
```

---

## 11. Testing and Plugins

Tests **do not** call `PluginRegistry.scanPlugins()`. They depend on plugins only through Gradle compile-time dependencies:

- `:ArtOfIllusion:test` has `testImplementation project(':StandardModules')` for procedural module tests
- `:Polymesh:test` depends on `:Translators` via `implementation` (compile-time)
- UI tests register resources directly: `PluginRegistry.registerResource("UITheme", "default", ...)` — no JAR loading needed

When testing a plugin module, Gradle resolves the module's `dependencies` block. Runtime `<import>` chains in `extensions.xml` are irrelevant for tests.

---

## 12. Plugin Blacklist

The file `Plugins/blacklist` (if present) lists JAR filenames to skip during loading, one per line. This allows disabling specific plugins without removing the JAR files.

---

## 13. Key Source Files

| File | Purpose |
|------|---------|
| `ArtOfIllusion.java:109-139` | Category registration and built-in plugin registration |
| `PluginRegistry.java` (617 lines) | All loading logic: scanning, classloaders, dependencies, resources, exports |
| `Plugin.java` (105 lines) | Lifecycle interface with default processMessage dispatch |
| `buildSrc/aoi.plugin-conventions.gradle` | Convention: dependency on core + JAR output to Plugins/ |
| `buildSrc/aoi.java-conventions.gradle` | Base Java conventions (toolchain, checkstyle, tests) |
| `xml-data/src/main/java/artofillusion/plugin/*.java` | XML descriptor classes (Extension, PluginDef, Export, ImportDef, Resource, etc.) |
| `settings.gradle` | Module inclusion |
