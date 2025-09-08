### Release #63
* Implemented Undo action for Convert To Polymesh command: https://github.com/makiam/Tokonga/pull/171
* Groovy library updated from 4.0,28 to 5.0.0: https://github.com/makiam/Tokonga/pull/169
* JOGL libraries updated to 2.6.0: https://github.com/makiam/Tokonga/pull/170

### Release #62
* Issue fixed: https://github.com/makiam/Tokonga/issues/164

### Release #61
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

### Release #57

**NB!** Current release drops compatibility with Procedural Textures version 0. Version 1 introduced in 2007 so older scenes with Procedural texture will not open (As workaround open and save scene in previous build)

* Drop Procedural Texture version 0 compatibility: https://github.com/makiam/Tokonga/pull/148

* Now Script can be run from Script Editor window using F5 shortcut

### Release #56

* Fix possible null prevents Scene to be saved in TextureTrack: https://github.com/makiam/Tokonga/pull/145

* Update Scene write/save code: https://github.com/makiam/Tokonga/pull/146

### Release #55

**NB!** Backward Compatibility breaking change!!! Current release introduces new fault-tolerant SceneCamera read/write and increase SceneCamera version to 4. For old files when Camera ImageFilter missed by some reason AOI fails to open scene. With new release missed or broken Image Filter bypassed.

* SceneCamera rework: https://github.com/makiam/Tokonga/pull/134 && https://github.com/makiam/Tokonga/pull/136

### Release #54

* Move i18n data for Translators from core app to plugin; https://github.com/makiam/Tokonga/pull/135

### Release #53

* Minor change in MaterialSpec class code: https://github.com/makiam/Tokonga/pull/133
* Blur image filter serialization issue fix and update in SceneCamera code and tests: https://github.com/makiam/Tokonga/pull/132

### Release #52
**NB!** Current release drops compatibility with Procedural Materials version 0. Version 1 introduced in 2007 so older scenes with Procedural material will not open (As workaround open and save scene in previous build)

* Minor change in Output Module code: https://github.com/makiam/Tokonga/pull/127
* Drop Procedure Material version 0 compatibility: https://github.com/makiam/Tokonga/pull/129

### Release #51

* Update animation tracks code: https://github.com/makiam/Tokonga/pull/126
