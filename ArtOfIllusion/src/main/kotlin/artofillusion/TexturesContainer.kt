package artofillusion


import artofillusion.texture.Texture
import java.util.Collections

internal interface TexturesContainer {

    /*
    Get all textures from scene as List
     */
    val textures: List<Texture>
        get() = Collections.unmodifiableList((this as Scene)._textures)


    /**
     * Get the number of textures in this scene.
     */
    fun getNumTextures(): Int = (this as Scene)._textures.size

    /**
     * Get the texture with the specified name, or null if there is none. If
     * more than one texture has the same name, this will return the first one.
     */
    fun getTexture(name: String?): Texture? = (this as Scene)._textures.firstOrNull { it.name == name }
}