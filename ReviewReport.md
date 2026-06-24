Summary
This branch performs several related refactorings to the procedural module system: (1) converts Module.output from IOPort[] array to List<IOPort> with corresponding accessor changes across multiple files, (2) cleans up the Link class by removing helper methods and adding field accessors, (3) adds drag-and-drop support via a new ModuleTransferable class and DropTarget in ProcedureEditor, (4) removes debug logging from ModuleMenu, changes the selected-link color to orange, and drops :OSSpecific from the Gradle build. The refactoring direction is sound, but three critical defects and several warnings need attention before merge.

Issues Found
Severity	File:Line	Issue
CRITICAL	ModuleTransferable.java:24	isDataFlavorSupported logic is inverted — returns true for unsupported flavors
CRITICAL	ProcedureEditor.java:688-689	Output module port detection hardcodes index 0, breaking multi-port OutputModules
CRITICAL	ProcedureEditor.java:697-728	Module click priority reversed; overlapping modules select wrong one
WARNING	ProcedureEditor.java:1088-1093	Drop target handler doesn't call saveState() — no undo support for dropped modules
WARNING	Module.java:39	linkFrom remains raw Module[] array — incomplete migration from arrays
WARNING	ProcedureEditor.java:1087-1093	DropTargetDropEvent.acceptDrop() never called — may cause DnD protocol violations
SUGGESTION	ModuleTransferable.java:28-29	getTransferData ignores flavor parameter — should validate before returning data
SUGGESTION	Link.java:16-17,23-24	Redundant getter methods alongside public final fields — inconsistent API design
Detailed Findings
CRITICAL #1: Inverted isDataFlavorSupported logic (99%)
The method returns !flavor.equals(ProceduralModule.moduleFlavor), meaning it returns true for any flavor that is NOT the module flavor, and false for the actual module flavor we want to support. This will prevent drag-and-drop from working entirely. Fix: Remove the ! negation.

CRITICAL #2: Output module port detection hardcoded (95%)
Old code used output.getClickedPort(clickPos) which iterates all ports. New code hardcodes output.getInputPorts()[0] — only checks port 0. If OutputModules have more than one input port, the click will silently fail. Fix: Restore getClickedPort() call.

CRITICAL #3: Module click priority reversed (95%)
Old code iterated modules in reverse (last drawn = topmost gets priority on overlap). New code iterates selectedModules (unordered HashSet) then modules forward (bottommost gets priority). This means overlapping module clicks will pick the wrong module. Fix: Restore reverse-order iteration with proper selected/unselected handling.

WARNING #1: No undo for dropped modules (90%)
Every other mutation path calls saveState(false). Drop handler doesn't — modules added via drag-and-drop cannot be undone with Ctrl+Z. The inline comment acknowledges this gap. Fix: Add saveState(false) before addModule().

WARNING #2: Incomplete array→List migration (85%)
linkFrom still uses raw Module[] while output now uses List<IOPort>. Fix: Migrate linkFrom to List<Module> as follow-up.

WARNING #3: DnD protocol not followed (85%)
event.acceptDrop() and event.dropComplete() never called per AWT DnD specification. Fix: Add proper DnD protocol calls.

Recommendation
NEEDS CHANGES — The three critical issues (inverted DnD flavor check, broken output module port detection, reversed module click priority) must be fixed before merging. The missing undo support in the drop handler is a strong warning that should also be addressed.