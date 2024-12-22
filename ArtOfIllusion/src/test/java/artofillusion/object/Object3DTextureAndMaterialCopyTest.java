/* Copyright (C) 2018-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * @author maksim.khramov
 */
@DisplayName("Object 3 D Texture And Material Copy Test")
class Object3DTextureAndMaterialCopyTest {

    @Test
    @DisplayName("Test Set Null Source Material")
    void testSetNullSourceMaterial() {
        Dummy3DObject target = new Dummy3DObject();
        Dummy3DObject source = new Dummy3DObject();
        source.setMaterial(null, null);
        target.copyTextureAndMaterial(source);
        Assertions.assertNull(target.getMaterial());
        Assertions.assertNull(target.getMaterialMapping());
    }


}
