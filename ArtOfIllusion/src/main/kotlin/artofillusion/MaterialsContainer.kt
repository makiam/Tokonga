package artofillusion

import artofillusion.material.Material
import java.util.Collections

internal interface MaterialsContainer {

    val materials: List<Material>
        get() = Collections.unmodifiableList((this as Scene)._materials)

    fun add(material: Material) {
        val scene = this as Scene
        scene._materials.add(material)
    }

    /**
     * Get the number of materials in this scene.
     */
    fun getNumMaterials(): Int = (this as Scene)._materials.size

    /**
     * Get the material with the specified name, or null if there is none. If
     * more than one material has the same name, this will return the first one.
     */
    fun getMaterial(name: String?): Material? = (this as Scene)._materials.firstOrNull { it.name == name }
}
