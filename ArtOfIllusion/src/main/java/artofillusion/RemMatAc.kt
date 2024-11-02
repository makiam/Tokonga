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
        scene.objects.filter { it.geometry.material == material }.forEach { matMap.put(it, it.geometry.materialMapping) }
    }

    internal constructor(scene: Scene, which: Int) {
        this.scene = scene
        this.which = which
        this.material = scene._materials[which]
        scene.objects.filter { it.geometry.material == material }.forEach { matMap.put(it, it.geometry.materialMapping) }
    }

    override fun undo() {
    }

    override fun redo() {
        log.info("Found mapped objects: {}", matMap.size)
        scene.removeMaterial(which)
    }

    companion object  {
        private val log: Logger = LoggerFactory.getLogger(RemMatAc::class.java)
    }
}
