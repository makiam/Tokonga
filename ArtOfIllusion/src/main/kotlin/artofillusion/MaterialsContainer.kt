/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion

import artofillusion.material.Material
import artofillusion.material.MaterialMapping
import artofillusion.`object`.ObjectInfo
import java.util.Collections
import org.greenrobot.eventbus.EventBus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

    fun removeMaterial(index: Int) {
        if(index in (this as Scene)._materials.indices) {
            val action = RemoveMaterialAction(this as Scene, index).execute()
        }
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

    /**
     * Get the material by index.
     */
    fun getMaterial(index: Int): Material? = (this as Scene)._materials.getOrNull(index)

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

        override fun getName() = "Add Material"
    }

    class RemoveMaterialAction(val scene: Scene, val index: Int) : UndoableEdit {
        private val matMap: MutableMap<ObjectInfo?, MaterialMapping?> = HashMap<ObjectInfo?, MaterialMapping?>()
        private val material: Material = scene._materials[index]

        init {
            scene.objects.filter { it.geometry.material == material }.forEach { matMap.put(it, it.geometry.materialMapping) }
        }

        override fun undo() {
            TODO("Not yet implemented")
        }

        override fun redo() {
            scene._materials.remove(material)
            this.matMap.keys.forEach { it?.setMaterial(null, null) }

            EventBus.getDefault().post(MaterialRemovedEvent(scene, material, index))
        }

        override fun getName() = "Remove Material"

        companion object  {
            private val log: Logger = LoggerFactory.getLogger(RemoveMaterialAction::class.java)
        }
    }

}
