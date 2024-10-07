/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion


import artofillusion.texture.Texture
import org.greenrobot.eventbus.EventBus
import java.util.Collections

internal interface TexturesContainer {

    /*
    Get all textures from scene as List
     */
    val textures: List<Texture>
        get() = Collections.unmodifiableList((this as Scene)._textures)

    fun add(texture: Texture) = add(texture, (this as Scene)._textures.size)

    fun add(texture: Texture, index: Int) {
        val scene = this as Scene
        scene._textures.add(index, texture)
        EventBus.getDefault().post(TextureAddedEvent(scene, texture, index))
    }

    /**
     * Add a new Texture to the scene.
     */
    fun addTexture(texture: Texture) = add(texture)
    /**
     * Add a new Texture to the scene.
     *
     * @param texture the Texture to add
     * @param index the position in the list to add it at
     */
    fun addTexture(texture: Texture, index: Int) = add(texture, index)

    /**
     * Get the number of textures in this scene.
     */
    fun getNumTextures(): Int = (this as Scene)._textures.size

    /**
     * Get the texture with the specified name, or null if there is none. If
     * more than one texture has the same name, this will return the first one.
     */
    fun getTexture(name: String?): Texture? = (this as Scene)._textures.firstOrNull { it.name == name }

    /**
     * Get the texture by index.
     */
    fun getTexture(index: Int): Texture? = (this as Scene)._textures[index]

    /**
     * Get the index of the specified texture.
     */
    fun indexOf(texture: Texture): Int = (this as Scene)._textures.indexOf(texture)

    data class TextureAddedEvent(val scene: Scene, val texture: Texture, val position: Int)
    data class TextureRemovedEvent(val scene: Scene, val texture: Texture, val position: Int)
}
