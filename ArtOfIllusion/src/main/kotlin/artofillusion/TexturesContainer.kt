package artofillusion


import artofillusion.MaterialsContainer.MaterialAssetEvent
import artofillusion.material.Material
import artofillusion.texture.Texture
import org.greenrobot.eventbus.EventBus
import java.util.Collections

internal interface TexturesContainer {

    /*
    Get all textures from scene as List
     */
    val textures: List<Texture>
        get() = Collections.unmodifiableList((this as Scene)._textures)

    fun add(texture: Texture) {
        val scene = this as Scene
        scene._textures += texture
        val message: TextureAssetEvent = TextureAssetEvent(scene, texture)
        EventBus.getDefault().post(message)
    }
    fun add(texture: Texture, index: Int) {
        val scene = this as Scene
        scene._textures.add(index, texture)
        val message: TextureAssetEvent = TextureAssetEvent(scene, texture)
        EventBus.getDefault().post(message)
    }

    /**
     * Add a new Texture to the scene.
     */
    fun addTexture(texture: Texture) = add(texture)
    /**
     * Add a new Texture to the scene.
     *
     * @param tex the Texture to add
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

    data class TextureAssetEvent(val scene: Scene, val texture: Texture, val position: Int = scene._textures.size -1)
}
