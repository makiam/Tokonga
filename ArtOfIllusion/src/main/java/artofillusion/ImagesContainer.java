/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.image.ImageMap;

import java.util.Collections;
import java.util.List;

sealed interface ImagesContainer permits Scene {

    /**
     * Get all images from scene as List
     */
    default List<ImageMap> getImages() {
        var scene = (Scene) this;
        return Collections.unmodifiableList(scene.images);
    }

    /**
     * Add an image map to the scene.
     */
    default void add(ImageMap image) {
        var scene = (Scene) this;
        scene.images.add(image);
    }

    /**
     * Add an image map to the scene.
     */
    default void addImage(ImageMap image) {
        add(image);
    }

    /**
     * Get the number of image maps in this scene.
     */
    default int getNumImages() {
        var scene = (Scene) this;
        return scene.images.size();
    }

    /**
     * Get the index of the specified image map.
     */
    default int indexOf(ImageMap image) {
        var scene = (Scene) this;
        return scene.images.indexOf(image);
    }

    /**
     * Get the imageMmap at the specified index
     */
    default ImageMap getImage(int index) {
        var scene = (Scene) this;
        return scene.images.get(index);
    }

    /**
     * Replace an ImageMap with another one
     */
    default void replaceImage(int which, ImageMap image) {
        replace(which, image);
    }

    /**
     * Replace an ImageMap with another one
     */
    default void replace(int which, ImageMap image) {
        var scene = (Scene) this;
        scene.images.set(which, image);
    }
}
