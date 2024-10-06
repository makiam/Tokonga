/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

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

    fun addImage(image: ImageMap) = add(image)

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
