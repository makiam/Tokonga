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
        scene._textures.add(texture)
        val message: TextureAssetEvent = TextureAssetEvent(scene, texture)
        EventBus.getDefault().post(message)
    }

    /**
     * Get the number of textures in this scene.
     */
    fun getNumTextures(): Int = (this as Scene)._textures.size

    /**
     * Get the texture with the specified name, or null if there is none. If
     * more than one texture has the same name, this will return the first one.
     */
    fun getTexture(name: String?): Texture? = (this as Scene)._textures.firstOrNull { it.name == name }

    data class TextureAssetEvent(val scene: Scene, val texture: Texture, val position: Int = scene._textures.size -1)
}