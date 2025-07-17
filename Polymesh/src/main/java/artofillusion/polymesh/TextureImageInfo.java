/* Copyright (C) 2003 by Peter Eastman
   Changes copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.texture.Texture;

/**
 * This class is used by TextureImage Exporters for storing information about texture images.
 */
public class TextureImageInfo {

    final Texture texture;
    String name;
    String diffuseFilename, specularFilename, hilightFilename, transparentFilename, emissiveFilename;
    double minU, minV, maxU, maxV;
    final double[] paramValue;

    public TextureImageInfo(Texture tex, double[] param) {
        texture = tex;
        paramValue = param;
        minU = minV = Double.MAX_VALUE;
        maxU = maxV = -Double.MAX_VALUE;
    }

}
