/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.image.ImageMap;

public interface ImageReference {

    /**
     * Return true if this object Texture or Material use of the specified ImageMap. Textures and Materials which
     * use ImageMaps should override this method.
     */
    default boolean usesImage(ImageMap image) {
        return false;
    }
}
