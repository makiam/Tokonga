package artofillusion

import artofillusion.image.ImageMap

import java.util.*

internal interface ImagesContainer {
    /*
    Get all images from scene as List
     */
    val images: List<ImageMap>
        get() = Collections.unmodifiableList((this as Scene)._images)

    /**
     * Add an image map to the scene.
     */
    fun add(image: ImageMap) {
        val scene = this as Scene
        scene._images += image
    }

    /**
     * Get the number of image maps in this scene.
     */
    fun getNumImages(): Int = (this as Scene)._images.size

    /**
     * Get the index of the specified image map.
     */
    fun indexOf(image: ImageMap): Int = (this as Scene)._images.indexOf(image)

    /**
     *Get the image map at the specified index
     */
    fun getImage(index: Int): ImageMap? = (this as Scene)._images[index]
}
