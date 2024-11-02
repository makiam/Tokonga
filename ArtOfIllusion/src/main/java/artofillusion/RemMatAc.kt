package artofillusion

import artofillusion.material.Material
import artofillusion.material.MaterialMapping
import artofillusion.`object`.ObjectInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.HashMap

class RemMatAc : UndoableEdit {
    private val which: Int
    private val scene: Scene

    private val matMap: MutableMap<ObjectInfo?, MaterialMapping?> = HashMap<ObjectInfo?, MaterialMapping?>()
    private val material: Material

    internal constructor(scene: Scene, material: Material) {
        this.scene = scene
        this.material = material
        this.which = scene._materials.indexOf(material)
    }

    internal constructor(scene: Scene, which: Int) {
        this.scene = scene
        this.which = which
        this.material = scene._materials[which]
    }

    override fun undo() {
    }

    override fun redo() {
        val applied = scene.objects.filter { it -> it.getObject().material == material }
        log.info("Filtered: {}", applied.size)
        scene.removeMaterial(which)
    }

    companion object  {
        private val log: Logger = LoggerFactory.getLogger(RemMatAc::class.java)
    }
}
