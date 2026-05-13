/* Copyright (С) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion.texture;

import artofillusion.object.DummyTexture;
import artofillusion.object.DummyTextureMapping;
import artofillusion.object.Object3D;
import artofillusion.object.Sphere;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Layered Mapping Test.")
public class LayeredMappingDuplicateTest {

    private Object3D obj;
    private LayeredMapping map;


    @BeforeEach
    void setUpEach() {
        obj = new Sphere(1.0, 1.0, 1.0);
        map = new LayeredMapping(obj, new LayeredTexture(obj));
    }

    @Test
    void testCopyFromEmptyToEmpty() {
        var target = new LayeredMapping(obj, new LayeredTexture(obj));
        target.copy(map);

        Assertions.assertEquals(0, target.getNumLayers());
    }


    @Test
    void testCopyFromExistingToEmpty() {
        DummyTexture dummy0 = new DummyTexture();
        dummy0.setName("dummy0");
        TextureMapping mapping0 = new DummyTextureMapping(obj, dummy0);

        DummyTexture dummy1 = new DummyTexture();
        dummy1.setName("dummy1");
        TextureMapping mapping1 = new DummyTextureMapping(obj, dummy0);

        map.addLayer(0, dummy0, mapping0, LayeredMapping.BLEND);
        map.addLayer(0, dummy1, mapping1, LayeredMapping.OVERLAY_ADD_BUMPS);


        var target = new LayeredMapping(obj, new LayeredTexture(obj));
        target.copy(map);

        Assertions.assertEquals(2, target.getNumLayers());
        Assertions.assertEquals(dummy1, target.getLayer(0));

        Assertions.assertEquals(LayeredMapping.OVERLAY_ADD_BUMPS, target.getLayerMode(0));

    }


}

