### Release #71 (21.04.2026)
* Camera filter bug fix ported from upstream AOI: https://github.com/makiam/Tokonga/pull/242
* Remove deprecated code from LayeredMapping class: https://github.com/makiam/Tokonga/pull/243
* Rework Polymesh dialogs: https://github.com/makiam/Tokonga/pull/244
* Minor readability Update in Layout: https://github.com/makiam/Tokonga/pull/246
* Update Polymesh editor tools: https://github.com/makiam/Tokonga/pull/247

### Release #70 (07.04.2026)
* Updated Polymesh Controlled Smoothing Dialog: https://github.com/makiam/Tokonga/pull/238
* Updated Polymesh Edge Range Dialog: https://github.com/makiam/Tokonga/pull/238

### Release #69 (30.03.2026)
* Clean unused code in plugins classloader: https://github.com/makiam/Tokonga/pull/236
* Extract some utility code for Polymesh to separate class: https://github.com/makiam/Tokonga/pull/234
* Groovy libraries updated from 5.0.4 to 5.0.5: https://github.com/makiam/Tokonga/pull/235

### Release #68 (23.03.2026)
Fixed Script Editor window layout: https://github.com/makiam/Tokonga/pull/228
Fixed Script Output window layout: https://github.com/makiam/Tokonga/pull/231
Extracted some Polymesh code to helper class: https://github.com/makiam/Tokonga/pull/232

### Release #67 (03.03.2026)
* Migrate old-style JDK5 for loops to modern for-each where possible: https://github.com/makiam/Tokonga/pull/217
* Syntax highligting library updated from 3.6.1 to 3.6.2: https://github.com/makiam/Tokonga/pull/218
* Logging library updated from 1.5.28 to 1.5.32: https://github.com/makiam/Tokonga/pull/215

### Release #66 (03.03.2026)
* Migrate old-style JDK5 for loops to modern for-each where possible: https://github.com/makiam/Tokonga/pull/217
* Syntax highligting library updated from 3.6.1 to 3.6.2: https://github.com/makiam/Tokonga/pull/218
* Logging library updated from 1.5.28 to 1.5.32: https://github.com/makiam/Tokonga/pull/215

### Release #66 (09.02.2026)
* Groovy version updated from 5.0.3 to 5.0.4: https://github.com/makiam/Tokonga/pull/203
* Syntax highligting library updated from 3.6.0 to 3.6.1: https://github.com/makiam/Tokonga/pull/200
* Logging library updated from 1.5.23 to 1.5.28
* 
### Release #65 (22.12.2025)

* Minimal Java version set to JDK 17: https://github.com/makiam/Tokonga/pull/193
* Update Groovy version: https://github.com/makiam/Tokonga/pull/194
* Updated code of Alias Wavefront importer

### Release #64
* Fixed issue with Keyboard Shortcuts editor cannot save/update shortcuts: https://github.com/makiam/Tokonga/pull/174
* Minor Groovy version update from 5.0.0 to 5.0.1: https://github.com/makiam/Tokonga/pull/176

### Release #63
* Implemented Undo action for Convert To Polymesh command: https://github.com/makiam/Tokonga/pull/171
* Groovy library updated from 4.0,28 to 5.0.0: https://github.com/makiam/Tokonga/pull/169
* JOGL libraries updated to 2.6.0: https://github.com/makiam/Tokonga/pull/170

### Release #62
* Issue fixed: https://github.com/makiam/Tokonga/issues/164

### Release #61 (06.08.2025)
**NB!** Current release drops compatibility.  Release introduces new fault tolerant read/write for scene object tracks. This increased scene verison, so new scene will not open in previous versions
* Buffered write/restore for scene's tracks: https://github.com/makiam/Tokonga/pull/161

### Release #60

* Update Checkbox Menu Items processing: https://github.com/makiam/Tokonga/pull/156
* Simplify code for toggle Smooth mode for Polymesh editor
* Simplify code for copy data to clipboard in Polymesh editor
* Fix menu response on Undo/Redo commands for toggle Smooth mode in Polymesh editor

### Release #59

* Minor updates in Polymesh editor window: https://github.com/makiam/Tokonga/pull/153

### Release #58

* Minor update for Procedure Editor related to shortcut and menu processing: https://github.com/makiam/Tokonga/pull/150
* Update to clean some SonarLint code warnings: https://github.com/makiam/Tokonga/pull/149

### Release #57 (10.06.2025)

**NB!** Current release drops compatibility with Procedural Textures version 0. Version 1 introduced in 2007 so older scenes with Procedural texture will not open (As workaround open and save scene in previous build)

* Drop Procedural Texture version 0 compatibility: https://github.com/makiam/Tokonga/pull/148
* Now Script can be run from Script Editor window using F5 shortcut

### Release #56

* Fix possible null prevents Scene to be saved in TextureTrack: https://github.com/makiam/Tokonga/pull/145

* Update Scene write/save code: https://github.com/makiam/Tokonga/pull/146

### Release #55 (13.05.2025)

**NB!** Backward Compatibility breaking change!!! Current release introduces new fault-tolerant SceneCamera read/write and increase SceneCamera version to 4. For old files when Camera ImageFilter missed by some reason AOI fails to open scene. With new release missed or broken Image Filter bypassed.

* SceneCamera rework: https://github.com/makiam/Tokonga/pull/134 && https://github.com/makiam/Tokonga/pull/136

### Release #54

* Move i18n data for Translators from core app to plugin; https://github.com/makiam/Tokonga/pull/135

### Release #53

* Minor change in MaterialSpec class code: https://github.com/makiam/Tokonga/pull/133
* Blur image filter serialization issue fix and update in SceneCamera code and tests: https://github.com/makiam/Tokonga/pull/132

### Release #52 (09.04.2025)
**NB!** Current release drops compatibility with Procedural Materials version 0. Version 1 introduced in 2007 so older scenes with Procedural material will not open (As workaround open and save scene in previous build)

* Minor change in Output Module code: https://github.com/makiam/Tokonga/pull/127
* Drop Procedure Material version 0 compatibility: https://github.com/makiam/Tokonga/pull/129

### Release #51 

* Update animation tracks code: https://github.com/makiam/Tokonga/pull/126

### Release #50 (25.03.2025)

* Fixed regression in procedure loading code: https://github.com/makiam/Tokonga/pull/123
* Groovy and Script editor libraries updated to latest: https://github.com/makiam/Tokonga/pull/119

### Release #49 (17.03.2025)

* Update UI for Commentary module: https://github.com/makiam/Tokonga/pull/118

### Release #48 (10.03.2025)

* Updatу procedure code: https://github.com/makiam/Tokonga/pull/115

### Release #47 (03.03.2025)

* Update some procedural modules dialogs: https://github.com/makiam/Tokonga/pull/111
* Update base procedural module internals: https://github.com/makiam/Tokonga/pull/112

### Release #46 (17.02.2025)

* Update procedural modules annotations: https://github.com/makiam/Tokonga/pull/106

### Release #45 (10.02.2025)

* Broken Equality module is replaced with new Color and Numeric equality modules: https://github.com/makiam/Tokonga/pull/105

### Release #44 (03.02.2025)

* Minor update in Layout Window Edit menu Undo/Redo implementation: https://github.com/makiam/Tokonga/pull/101

### Release #43 (28.01.2025)

* Updated External Object Wizard: https://github.com/makiam/Tokonga/pull/94
* Minor code updates: https://github.com/makiam/Tokonga/pull/95

### Release #42 (07.01.2025)

* Logging library version update: https://github.com/makiam/Tokonga/pull/92
* Minor update Polymesh dialogs: https://github.com/makiam/Tokonga/pull/93
* Minor code changes affects UI and animation tracks code: https://github.com/makiam/Tokonga/pull/91 and https://github.com/makiam/Tokonga/pull/90

### Release #41 (31.12.2024)

* Logging library version update: https://github.com/makiam/Tokonga/pull/83
* Minor procedural editor modules menu update: https://github.com/makiam/Tokonga/pull/84
* Update Ok/Cancel button processing code and Windows closing button processing: https://github.com/makiam/Tokonga/pull/86 and https://github.com/makiam/Tokonga/pull/88

### Release #40 (23.12.2024)

* Minor updates in Procedure Editor code: https://github.com/makiam/Tokonga/pull/79
* Update for Scripted Object Parameters editor: now new paraneter created with generated name: https://github.com/makiam/Tokonga/pull/81

### Release #39 (16.12.2024)

* Update some localization data: https://github.com/makiam/Tokonga/pull/73
* Updated Color chooser dialog to response on Escape and window close button: https://github.com/makiam/Tokonga/pull/76
* Logger implementation switched from tiny log to logback: https://github.com/makiam/Tokonga/pull/74

### Release #38 (09.12.2024)

* Fixed regression error adding default track for new scene object: https://github.com/makiam/Tokonga/pull/71
* Update application dialogs: https://github.com/makiam/Tokonga/pull/70
* Minor rework for layout animation menu: https://github.com/makiam/Tokonga/pull/72

### Release #37 (02.12.2024)

* Update Image dialogs: https://github.com/makiam/Tokonga/pull/69

### Release #36 (25.11.2024)

* Jetbrains Mono font used in Script editor: https://github.com/makiam/Tokonga/pull/66
* Main Window code split to smaller parts: https://github.com/makiam/Tokonga/pull/63

### Release #35 (18.11.2024)

* Keyboard shortcut scripts now compiled only once at first invocation: https://github.com/makiam/Tokonga/pull/61

### Release #34 (13.11.2024)

* Support for plugins blacklist: https://github.com/makiam/Tokonga/pull/59
* Update application library: https://github.com/makiam/Tokonga/pull/58

### Release #33 (05.11.2024)

* Update Scene materials collection: https://github.com/makiam/Tokonga/pull/54
* ObjectInfo class update: https://github.com/makiam/Tokonga/pull/55
* Score view classes update: https://github.com/makiam/Tokonga/pull/56
* Polymesh Bevel Dialog update: https://github.com/makiam/Tokonga/pull/57

### Release #32 (30.10.2024)

* Standard Theme layout and tests update: https://github.com/makiam/Tokonga/pull/50
* More menu items used typed code bindings: https://github.com/makiam/Tokonga/pull/51
* Polymesh create tool updated: https://github.com/makiam/Tokonga/pull/53
* Underlaying buoy library updated: https://github.com/makiam/Tokonga/pull/52
(Buoy sources: https://github.com/makiam/Buoy/archive/refs/tags/1.1.3.zip)

NB, Last change may affect 3rd-party plugins

### Release #31 (21.10.2024)

* Build contains only minor code cleanup in Polymesh and Plugin Manager plugins

### Release #30 (14.10.2024)

* Updated Polymesh Divide Edges dialog and also some i18n changes: https://github.com/makiam/Tokonga/pull/45
* Deleted unused code from Layout: https://github.com/makiam/Tokonga/pull/42
* Clean Plugin Registry code: https://github.com/makiam/Tokonga/pull/41
* Fixed Script Editor regression: https://github.com/makiam/Tokonga/commit/6206d7925447c6f2077a6edc58826e9ce4db726a
* Update application startup code: https://github.com/makiam/Tokonga/pull/43
