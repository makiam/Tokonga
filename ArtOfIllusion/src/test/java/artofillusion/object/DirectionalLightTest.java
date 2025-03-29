/* Copyright (C) 2018-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import artofillusion.Scene;
import artofillusion.math.RGBColor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author maksim.khramov
 */
@DisplayName("Directional Light Test")
class DirectionalLightTest {

    @Test
    @DisplayName("Test Create Directinal Light")
    void testCreateDirectinalLight() {
        RGBColor color = new RGBColor();
        DirectionalLight light = new DirectionalLight(color, 0);
        Assertions.assertNotNull(light);
        Assertions.assertEquals(light.getColor(), color);
        Assertions.assertEquals(light.getIntensity(), 0, 0);
        Assertions.assertEquals(light.getRadius(), 1.0, 0);
        Assertions.assertEquals(Light.TYPE_NORMAL, light.getType());
        Assertions.assertEquals(light.getDecayRate(), 0.5f, 0);
    }

    @Test
    @DisplayName("Test Create Directinal Light 2")
    void testCreateDirectinalLight2() {
        RGBColor color = new RGBColor();
        DirectionalLight light = new DirectionalLight(color, 0, 5.0);
        Assertions.assertNotNull(light);
        Assertions.assertEquals(light.getColor(), color);
        Assertions.assertEquals(light.getIntensity(), 0, 0);
        Assertions.assertEquals(light.getRadius(), 5.0, 0);
        Assertions.assertEquals(Light.TYPE_NORMAL, light.getType());
        Assertions.assertEquals(light.getDecayRate(), 0.5f, 0);
    }

    @Test
    @DisplayName("Test Create Directional Light From Stream Bad Version 0")
    void testCreateDirectionalLightFromStreamBadVersion0() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Object3D version
            wrap.putShort((short) 1);
            // No matertial
            wrap.putInt(-1);
            // Default texture
            wrap.putInt(0);
            // Version
            wrap.putShort((short) -1);
            DirectionalLight light = new DirectionalLight(new DataInputStream(new ByteArrayInputStream(wrap.array())), scene);
        });
    }

    @Test
    @DisplayName("Test Create Directional Light From Stream Bad Version 1")
    void testCreateDirectionalLightFromStreamBadVersion1() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Object3D version
            wrap.putShort((short) 1);
            // No matertial
            wrap.putInt(-1);
            // Default texture
            wrap.putInt(0);
            // Version
            wrap.putShort((short) 2);
            {
                // RGB Color
                wrap.putFloat(0);
                wrap.putFloat(0.5f);
                wrap.putFloat(1f);
            }
            DirectionalLight light = new DirectionalLight(new DataInputStream(new ByteArrayInputStream(wrap.array())), scene);
        });
    }

    @Test
    @DisplayName("Test Create Directional Light From Stream Good Version 0")
    void testCreateDirectionalLightFromStreamGoodVersion0() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Object3D version
            wrap.putShort((short) 1);
            // No matertial
            wrap.putInt(-1);
            // Default texture
            wrap.putInt(0);
            // Version
            wrap.putShort((short) 0);
            {
                // RGB Color
                wrap.putFloat(0);
                wrap.putFloat(0.5f);
                wrap.putFloat(1f);
            }
            // Intensity
            wrap.putFloat(0.75f);
            DirectionalLight light = new DirectionalLight(new DataInputStream(new ByteArrayInputStream(wrap.array())), scene);
        });
    }

    @Test
    @DisplayName("Test DL Copy")
    void testDLCopy() {
        RGBColor color = new RGBColor(0, 0.5, 1);
        DirectionalLight source = new DirectionalLight(color, 3, 5.0);
        source.setType(Light.TYPE_AMBIENT);
        source.setDecayRate(0.8f);
        RGBColor targetColor = new RGBColor();
        DirectionalLight target = new DirectionalLight(targetColor, 0);
        target.copyObject(source);
        Assertions.assertEquals(target.getColor(), color);
        Assertions.assertEquals(target.getIntensity(), 3, 0);
        Assertions.assertEquals(target.getRadius(), 5.0, 0);
        Assertions.assertEquals(Light.TYPE_AMBIENT, target.getType());
        Assertions.assertEquals(target.getDecayRate(), 0.8f, 0);
    }

    /*
    Testcase fails as some original light parameters is not copied to target
     */
    @Test
    @DisplayName("Test DL Duplicate")
    void testDLDuplicate() {
        RGBColor color = new RGBColor(0, 0.5, 1);
        DirectionalLight source = new DirectionalLight(color, 3, 5.0);
        source.setType(Light.TYPE_AMBIENT);
        source.setDecayRate(0.8f);
        DirectionalLight target = source.duplicate();
        Assertions.assertEquals(target.getColor(), color);
        Assertions.assertEquals(target.getIntensity(), 3, 0);
        Assertions.assertEquals(target.getRadius(), 5.0, 0);
        Assertions.assertEquals(Light.TYPE_AMBIENT, target.getType());
        Assertions.assertEquals(target.getDecayRate(), 0.8f, 0);
    }
}
