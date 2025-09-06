package artofillusion.polymesh

import artofillusion.LayoutWindow
import artofillusion.UndoableEdit
import artofillusion.`object`.ObjectInfo
import artofillusion.`object`.SplineMesh
import lombok.extern.slf4j.Slf4j

@Slf4j
class CreatePolymeshFromSpline internal constructor(private val layout: LayoutWindow, private val source: ObjectInfo) : UndoableEdit {
    private var item: ObjectInfo? = null

    override fun undo() {
        val ii = layout.scene.objects.indexOf(item)
        if (ii == -1) return
        layout.removeObject(ii, null)
    }

    override fun redo() {
        val pm = PolyMesh(source.getObject() as SplineMesh?)
        item = ObjectInfo(pm, source.coordinateSystem.duplicate(), "Polymesh " + source.getName())
        layout.addObject(item, null)
    }

    override fun getName(): String = "Convert to Polymesh";
}
