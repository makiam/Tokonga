/* Copyright (C) 2006-2008 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.texture;

import artofillusion.TextureParameter;
import org.junit.jupiter.api.Test;

import artofillusion.object.*;
import artofillusion.procedural.*;

import java.awt.*;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.DisplayName;

@DisplayName("Layered Texture Test")
class LayeredTextureTest {

    @Test
    @DisplayName("Test Has Component")
    void testHasComponent() {
        // Create a layered texture.
        Object3D obj = new Sphere(1.0, 1.0, 1.0);
        LayeredTexture tex = new LayeredTexture(obj);
        LayeredMapping map = new LayeredMapping(obj, tex);
        tex.setMapping(map);
        UniformTexture t1 = new UniformTexture();
        UniformTexture t2 = new UniformTexture();
        map.addLayer(0, t2, t2.getDefaultMapping(obj), LayeredMapping.BLEND);
        map.addLayer(0, t1, t1.getDefaultMapping(obj), LayeredMapping.BLEND);
        // Check a few components.
        Assertions.assertTrue(tex.hasComponent(Texture.DIFFUSE_COLOR_COMPONENT));
        Assertions.assertFalse(tex.hasComponent(Texture.SPECULAR_COLOR_COMPONENT));
        t2.specularity = 0.2f;
        Assertions.assertTrue(tex.hasComponent(Texture.SPECULAR_COLOR_COMPONENT));
        // Check transparency, which has more complex rules than other components.
        Assertions.assertFalse(tex.hasComponent(Texture.TRANSPARENT_COLOR_COMPONENT));
        t1.transparency = 0.5f;
        Assertions.assertTrue(tex.hasComponent(Texture.TRANSPARENT_COLOR_COMPONENT));
        map.setLayerMode(0, LayeredMapping.OVERLAY_ADD_BUMPS);
        Assertions.assertFalse(tex.hasComponent(Texture.TRANSPARENT_COLOR_COMPONENT));
        t2.transparency = 0.9f;
        Assertions.assertTrue(tex.hasComponent(Texture.TRANSPARENT_COLOR_COMPONENT));
        map.setLayerMode(1, LayeredMapping.OVERLAY_ADD_BUMPS);
        Assertions.assertTrue(tex.hasComponent(Texture.TRANSPARENT_COLOR_COMPONENT));
    }

    @Test
    @DisplayName("Test Parameters")
    void testParameters() {
        // Create two textures, each with two parameters.
        ProceduralTexture2D tex1 = new ProceduralTexture2D();
        tex1.getProcedure().addModule(new ParameterModule(new Point()));
        tex1.getProcedure().addModule(new ParameterModule(new Point()));
        ProceduralTexture2D tex2 = new ProceduralTexture2D();
        tex2.getProcedure().addModule(new ParameterModule(new Point()));
        tex2.getProcedure().addModule(new ParameterModule(new Point()));
        // Create a layered texture containing two copies of the first texture and one of the second.
        Object3D obj = new Sphere(1.0, 1.0, 1.0);
        LayeredTexture tex = new LayeredTexture(obj);
        LayeredMapping map = new LayeredMapping(obj, tex);
        tex.setMapping(map);
        map.addLayer(0, tex2, tex2.getDefaultMapping(obj), LayeredMapping.BLEND);
        map.addLayer(1, tex2, tex2.getDefaultMapping(obj), LayeredMapping.BLEND);
        map.addLayer(2, tex1, tex1.getDefaultMapping(obj), LayeredMapping.BLEND);
        obj.setTexture(tex, map);
        // Call getParameters() twice and make sure the results are consistent.
        TextureParameter[] param = map.getParameters();
        TextureParameter[] param2 = map.getParameters();
        Assertions.assertEquals(9, param.length);
        Assertions.assertEquals(9, param2.length);
        for (int i = 0; i < param.length; i++) {
            for (int j = 0; j < param2.length; j++) {
                if (i == j) {
                    Assertions.assertEquals(param[i], param2[j]);
                } else {
                    Assertions.assertNotEquals(param[i], param2[j]);
                }
            }
        }
        // Now request the parameters for each layer separately and make sure they match the full list.
        for (int i = 0; i < 3; i++) {
            TextureParameter[] layerParam = map.getLayerParameters(i);
            Assertions.assertEquals(3, layerParam.length);
            for (int j = 0; j < layerParam.length; j++) {
                for (int k = 0; k < param.length; k++) {
                    if (k == j + i * 3) {
                        Assertions.assertEquals(param[k], layerParam[j]);
                    } else {
                        Assertions.assertNotEquals(param[k], layerParam[j]);
                    }
                }
            }
        }
        // Set the values of all parameters, the make sure they are correct.
        for (int i = 0; i < param.length; i++) {
            obj.setParameterValue(param[i], new ConstantParameterValue(i));
        }
        for (int i = 0; i < param.length; i++) {
            Assertions.assertEquals(i, obj.getParameterValue(param[i]).getAverageValue(), 0.0);
        }
        // Test the getLayerBlendingParameter() and getParameterForLayer() methods.
        for (int layer = 0; layer < 3; layer++) {
            Assertions.assertEquals(map.getLayerBlendingParameter(layer), map.getLayerParameters(layer)[0]);
            for (int parameter = 0; parameter < 2; parameter++) {
                TextureParameter oldParameter = map.getLayerMapping(layer).getParameters()[parameter];
                TextureParameter newParameter = map.getLayerParameters(layer)[parameter + 1];
                Assertions.assertNotEquals(newParameter, oldParameter);
                Assertions.assertEquals(newParameter, map.getParameterForLayer(oldParameter, layer));
            }
        }
    }
}
