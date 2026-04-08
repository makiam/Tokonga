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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;



@DisplayName("Layered Mapping Test")
class LayeredMappingTest {
    @Test
    void testCreateMapping() {
        Object3D obj = new Sphere(1.0, 1.0, 1.0);
        LayeredTexture tex = new LayeredTexture(obj);
        LayeredMapping map = new LayeredMapping(obj, tex);
        Assertions.assertNotNull(map);
        Assertions.assertEquals(0, map.getNumLayers());
        Assertions.assertEquals(0, map.getMapping().length);
    }

    @Test
    void testAddLayer() {
        Object3D obj = new Sphere(1.0, 1.0, 1.0);
        LayeredTexture tex = new LayeredTexture(obj);
        LayeredMapping map = new LayeredMapping(obj, tex);
        DummyTexture dummy = new DummyTexture();
        TextureMapping mapping = new DummyTextureMapping(obj, dummy);
        map.addLayer(0, dummy, mapping, LayeredMapping.BLEND);

        Assertions.assertEquals(1, map.getNumLayers());
        Assertions.assertEquals(1, map.getMapping().length);

        Assertions.assertEquals(LayeredMapping.BLEND, map.getLayerMode(0));
    }

    @Test
    void testAddLayer2() {
        Object3D obj = new Sphere(1.0, 1.0, 1.0);
        LayeredTexture tex = new LayeredTexture(obj);
        LayeredMapping map = new LayeredMapping(obj, tex);
        DummyTexture dummy0 = new DummyTexture();
        dummy0.setName("dummy0");
        TextureMapping mapping0 = new DummyTextureMapping(obj, dummy0);

        DummyTexture dummy1 = new DummyTexture();
        dummy1.setName("dummy1");
        TextureMapping mapping1 = new DummyTextureMapping(obj, dummy0);

        map.addLayer(0, dummy0, mapping0, LayeredMapping.BLEND);
        map.addLayer(0, dummy1, mapping1, LayeredMapping.OVERLAY_ADD_BUMPS);

        Assertions.assertEquals(2, map.getNumLayers());
        Assertions.assertEquals(2, map.getMapping().length);

        Assertions.assertEquals(LayeredMapping.OVERLAY_ADD_BUMPS, map.getLayerMode(0));
        Assertions.assertEquals(LayeredMapping.BLEND, map.getLayerMode(1));

        Assertions.assertEquals("dummy1", map.getLayer(0).getName());
        Assertions.assertEquals("dummy0", map.getLayer(1).getName());
    }

    @Test
    void testAddLayer3() {
        Object3D obj = new Sphere(1.0, 1.0, 1.0);
        LayeredTexture tex = new LayeredTexture(obj);
        LayeredMapping map = new LayeredMapping(obj, tex);
        DummyTexture dummy0 = new DummyTexture();
        dummy0.setName("dummy0");
        TextureMapping mapping0 = new DummyTextureMapping(obj, dummy0);

        DummyTexture dummy1 = new DummyTexture();
        dummy1.setName("dummy1");
        TextureMapping mapping1 = new DummyTextureMapping(obj, dummy0);

        map.addLayer(0, dummy0, mapping0, LayeredMapping.BLEND);
        map.addLayer(1, dummy1, mapping1, LayeredMapping.OVERLAY_ADD_BUMPS);

        Assertions.assertEquals(2, map.getNumLayers());
        Assertions.assertEquals(2, map.getMapping().length);

        Assertions.assertEquals(LayeredMapping.BLEND, map.getLayerMode(0));
        Assertions.assertEquals(LayeredMapping.OVERLAY_ADD_BUMPS, map.getLayerMode(1));

        Assertions.assertEquals("dummy0", map.getLayer(0).getName());
        Assertions.assertEquals("dummy1", map.getLayer(1).getName());
    }

    @Test
    void testAddLayerBadIndex() {
        Object3D obj = new Sphere(1.0, 1.0, 1.0);
        LayeredTexture tex = new LayeredTexture(obj);
        final LayeredMapping map = new LayeredMapping(obj, tex);
        DummyTexture dummy = new DummyTexture();
        TextureMapping mapping = new DummyTextureMapping(obj, dummy);


        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            map.addLayer(5, dummy, mapping, LayeredMapping.BLEND);
        });

    }
}
