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



@DisplayName("Layered Mapping Test")
class LayeredMappingTest {

    private Object3D obj;
    private LayeredMapping map;


    @BeforeEach
    void setUpEach() {
        obj = new Sphere(1.0, 1.0, 1.0);
        map = new LayeredMapping(obj, new LayeredTexture(obj));
    }

    @Test
    void testCreateMapping() {

        Assertions.assertNotNull(map);
        Assertions.assertEquals(0, map.getNumLayers());
        Assertions.assertEquals(0, map.getMapping().length);
    }

    @Test
    void testAddLayer() {

        DummyTexture dummy = new DummyTexture();
        TextureMapping mapping = new DummyTextureMapping(obj, dummy);
        map.addLayer(0, dummy, mapping, LayeredMapping.BLEND);

        Assertions.assertEquals(1, map.getNumLayers());
        Assertions.assertEquals(1, map.getMapping().length);

        Assertions.assertEquals(LayeredMapping.BLEND, map.getLayerMode(0));
    }

    @Test
    void testAddLayer2() {

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

        DummyTexture dummy = new DummyTexture();
        TextureMapping mapping = new DummyTextureMapping(obj, dummy);


        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> map.addLayer(5, dummy, mapping, LayeredMapping.BLEND));

    }

    @Test
    @DisplayName("Delete single mapping layer")
    void testDeleteLayerSingle() {


        DummyTexture dummy0 = new DummyTexture();
        dummy0.setName("dummy0");
        TextureMapping mapping0 = new DummyTextureMapping(obj, dummy0);

        map.addLayer(0, dummy0, mapping0, LayeredMapping.BLEND);

        Assertions.assertEquals(1, map.getNumLayers());
        Assertions.assertEquals(1, map.getMapping().length);

        Assertions.assertEquals("dummy0", map.getLayer(0).getName());

        map.deleteLayer(0);
        Assertions.assertEquals(0, map.getNumLayers());
        Assertions.assertEquals(0, map.getMapping().length);
    }

    @Test
    @DisplayName("Delete not existed layer")
    void testDeleteMissedLayer() {


        Assertions.assertThrows(NegativeArraySizeException.class, () -> map.deleteLayer(0));
    }

    @Test
    @DisplayName("Delete not existed layer")
    void testDeleteMissedLayer2() {


        DummyTexture dummy0 = new DummyTexture();
        dummy0.setName("dummy0");
        TextureMapping mapping0 = new DummyTextureMapping(obj, dummy0);

        map.addLayer(0, dummy0, mapping0, LayeredMapping.BLEND);

        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> map.deleteLayer(1));
    }

    @Test
    @DisplayName("Delete first layer of two")
    void testDeleteLayerFirstOfTwo() {


        DummyTexture dummy0 = new DummyTexture();
        dummy0.setName("dummy0");
        TextureMapping mapping0 = new DummyTextureMapping(obj, dummy0);

        DummyTexture dummy1 = new DummyTexture();
        dummy1.setName("dummy1");
        TextureMapping mapping1 = new DummyTextureMapping(obj, dummy0);

        map.addLayer(0, dummy0, mapping0, LayeredMapping.BLEND);
        map.addLayer(1, dummy1, mapping1, LayeredMapping.OVERLAY_ADD_BUMPS);

        map.deleteLayer(0);
        Assertions.assertEquals(1, map.getNumLayers());
        Assertions.assertEquals(1, map.getMapping().length);
        Assertions.assertEquals("dummy1", map.getLayer(0).getName());
    }

    @Test
    @DisplayName("Move not existed layer 1")
    void testMoveLayer() {

        map.moveLayer(0,0);
        Assertions.assertEquals(0, map.getNumLayers());
    }

    @Test
    @DisplayName("Move not existed layer 2")
    void testMoveLayer1() {

        map.moveLayer(1,1);
        Assertions.assertEquals(0, map.getNumLayers());
    }

    @Test
    @DisplayName("Delete first layer of two")
    void testDeleteLayerLastOfTwo() {


        DummyTexture dummy0 = new DummyTexture();
        dummy0.setName("dummy0");
        TextureMapping mapping0 = new DummyTextureMapping(obj, dummy0);

        DummyTexture dummy1 = new DummyTexture();
        dummy1.setName("dummy1");
        TextureMapping mapping1 = new DummyTextureMapping(obj, dummy0);

        map.addLayer(0, dummy0, mapping0, LayeredMapping.BLEND);
        map.addLayer(1, dummy1, mapping1, LayeredMapping.OVERLAY_ADD_BUMPS);

        map.deleteLayer(1);
        Assertions.assertEquals(1, map.getNumLayers());
        Assertions.assertEquals(1, map.getMapping().length);
        Assertions.assertEquals("dummy0", map.getLayer(0).getName());
    }

    @Test
    void testGetParams0() {
        var params = map.getParameters();
        Assertions.assertNotNull(params);
        Assertions.assertEquals(0, params.length);
    }

    @Test
    void testGetLayerBlendingParameterNoLayers0() {
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> map.getLayerBlendingParameter(0));
    }

    @Test
    void testGetLayerBlendingParameterNoLayers1() {
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> map.getLayerBlendingParameter(1));
    }

    @Test
    void testGetSingleTextureLayerParams() {
        DummyTexture dummy1 = new DummyTexture();
        dummy1.setName("dummy1");
        TextureMapping mapping1 = new DummyTextureMapping(obj, dummy1);

        map.addLayer(0, dummy1, mapping1, LayeredMapping.BLEND);

        var tpp = map.getLayerBlendingParameter(0);

        Assertions.assertEquals(tpp.identifier, map.fractParamID[0]);
    }

    @Test
    void testGetSingleTextureLayerParamsSingleLayerAndBadIndex() {
        DummyTexture dummy1 = new DummyTexture();
        dummy1.setName("dummy1");
        TextureMapping mapping1 = new DummyTextureMapping(obj, dummy1);

        map.addLayer(0, dummy1, mapping1, LayeredMapping.BLEND);

        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, ()-> map.getLayerBlendingParameter(1));
    }
}
