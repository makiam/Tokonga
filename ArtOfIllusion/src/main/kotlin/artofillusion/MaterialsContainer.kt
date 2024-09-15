package artofillusion

import artofillusion.material.Material
import java.util.Collections
import org.greenrobot.eventbus.EventBus

internal interface MaterialsContainer {

    val materials: List<Material>
        get() = Collections.unmodifiableList((this as Scene)._materials)

    fun add(material: Material) = add(material, (this as Scene)._materials.size)

    fun add(material: Material, index: Int) {
        val action = AddMaterialAction(this as Scene, material, index).execute()
    }

    /**
     * Add a new Material to the scene.
     */
    fun addMaterial(material: Material) = add(material)

    /**
     * Add a new Material to the scene.
     *
     * @param material the Material to add
     * @param index the position in the list to add it at
     */
    fun addMaterial(material: Material, index: Int) = add(material, index)

    /**
     * Get the number of materials in this scene.
     */
    fun getNumMaterials(): Int = (this as Scene)._materials.size

    /**
     * Get the material with the specified name, or null if there is none. If
     * more than one material has the same name, this will return the first one.
     */
    fun getMaterial(name: String?): Material? = (this as Scene)._materials.firstOrNull { it.name == name }

    /**
     * Get the material by index.
     */
    fun getMaterial(index: Int): Material? = (this as Scene)._materials[index]

    /**
     * Get the index of the specified material.
     */
    fun indexOf(material: Material): Int = (this as Scene)._materials.indexOf(material)

    data class MaterialAddedEvent(val scene: Scene, val material: Material, val position: Int)
    data class MaterialRemovedEvent(val scene: Scene, val material: Material, val position: Int)

    class AddMaterialAction(val scene: Scene, val material: Material, val index: Int): UndoableEdit {

        override fun undo() {
            TODO("Not yet implemented")
        }

        override fun redo() {
            scene._materials.add(index, material)
            EventBus.getDefault().post(MaterialAddedEvent(scene, material, index))
        }
    }

}
