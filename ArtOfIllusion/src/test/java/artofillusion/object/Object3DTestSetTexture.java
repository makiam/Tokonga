/* Copyright (C) 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.TextureParameter;
import artofillusion.texture.ConstantParameterValue;
import artofillusion.texture.ParameterValue;
import artofillusion.texture.Texture;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Object 3D Test Texture Assignment")
public class Object3DTestSetTexture  {

    @Test
    @DisplayName("Test Set Texture without mapping")
    void testObject3DTestSetTextureAndNullMapping() {
        Object3D obj = new Dummy3DObject();
        obj.setTexture(new DummyTexture(), null);

        Assertions.assertNotNull(obj.getParameters());
        Assertions.assertNotNull(obj.getParameterValues());

        Assertions.assertEquals(obj.getParameters().length, 0);
        Assertions.assertEquals(obj.getParameterValues().length, 0);
    }

    @Test
    @DisplayName("Test Set Object Parameter when no Parameters initialized")
    void testSetObjectParameter0() {
        Object3D obj = new Dummy3DObject();

        TextureParameter tp = new TextureParameter(obj, "Dummy", 0, 100, 42);
        ParameterValue value = new ConstantParameterValue(100);

        Assertions.assertThrows(NullPointerException.class, () -> obj.getParameterValue(tp));
    }

    @Test
    @DisplayName("Test Set Object Parameter when no Parameter values initialized")
    public void testSetObjectParameter1() {
        Object3D obj = new Dummy3DObject();

        TextureParameter tp = new TextureParameter(obj, "Dummy", 0, 100, 42);
        ParameterValue value = new ConstantParameterValue(100);

        obj.setParameters(new TextureParameter[]{tp});

        Assertions.assertThrows(NullPointerException.class, () -> obj.getParameterValue(tp));
    }

    @Test
    @DisplayName("Test Set Object Texture with default mapping")
    void testSetObjectTextureWithDefaultMapping0() {
        Object3D obj = new Dummy3DObject();
        Texture tex = new DummyTexture();
        obj.setTexture(tex, tex.getDefaultMapping(obj));

        Assertions.assertEquals(0, obj.getParameters().length);
        Assertions.assertEquals(0, obj.getParameterValues().length);
    }

    @Test
    @DisplayName("Test Set Object Texture with default mapping")
    void testSetObjectTextureWithDefaultMapping1() {
        Object3D obj = new Dummy3DObject();
        Texture tex = new DummyTexture();

        obj.setTexture(tex, tex.getDefaultMapping(obj));

        Assertions.assertEquals(obj.getParameters().length, 0);
        Assertions.assertEquals(obj.getParameterValues().length, 0);
    }

}
