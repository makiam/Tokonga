/* Copyright (C) 2018-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion;

import artofillusion.material.UniformMaterial;
import artofillusion.math.RGBColor;
import artofillusion.object.Object3D;
import artofillusion.test.util.StreamUtil;
import artofillusion.texture.Texture;
import artofillusion.texture.TextureMapping;
import artofillusion.texture.TextureSpec;
import artofillusion.texture.UniformMapping;
import artofillusion.texture.UniformTexture;
import buoy.widget.BFrame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import buoy.widget.WindowWidget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author maksim.khramov
 */
@DisplayName("Scene Load Test")
class SceneLoadTest {

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Load Scene Bad Version 1")
    void testLoadSceneBadVersion1() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(2);
            // Scene Version
            wrap.putShort((short) -1);
            new Scene(StreamUtil.stream(wrap), true);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Load Scene Bad Version 2")
    void testLoadSceneBadVersion2() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(2);
            // Scene Version
            wrap.putShort((short) 6);
            new Scene(StreamUtil.stream(wrap), true);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Read Scene With Missed Image")
    void testReadSceneWithMissedImage() {
        assertThrows(IOException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Scene Version 2. No metadata expected to  set
            wrap.putShort((short) 2);
            // Ambient color data
            colorToBuffer(new RGBColor(100, 200, 200), wrap);
            // Fog color data
            colorToBuffer(new RGBColor(50, 50, 50), wrap);
            // Fog
            wrap.put((byte) 1);
            // Fog Distance
            wrap.putDouble(1000);
            // show grid
            wrap.put((byte) 1);
            // snap to grid
            wrap.put((byte) 1);
            // grid spacing
            wrap.putDouble(10);
            // grid Subdivisions
            wrap.putInt(10);
            // FPS
            wrap.putInt(60);
            // Image maps count
            wrap.putInt(1);
            {
                String className = "dummy.dummy.MissedImageClass";
                wrap.putShort(Integer.valueOf(className.length()).shortValue());
                wrap.put(className.getBytes());
            }
            new Scene(StreamUtil.stream(wrap), true);
        });
    }

    @Test
    @DisplayName("Test Read Empty Scene With Missed Material And Texture")
    void testReadEmptySceneWithMissedMaterialAndTexture() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Scene Version 2. No metadata expected to  set
        wrap.putShort((short) 2);
        // Ambient color data
        colorToBuffer(new RGBColor(100, 200, 200), wrap);
        // Fog color data
        colorToBuffer(new RGBColor(50, 50, 50), wrap);
        // Fog
        wrap.put((byte) 1);
        // Fog Distance
        wrap.putDouble(1000);
        // show grid
        wrap.put((byte) 1);
        // snap to grid
        wrap.put((byte) 1);
        // grid spacing
        wrap.putDouble(10);
        // grid Subdivisions
        wrap.putInt(10);
        // FPS
        wrap.putInt(60);
        // Image maps count
        wrap.putInt(0);
        // Materials count
        wrap.putInt(1);
        {
            String className = "dummy.dummy.UnknownMaterialClass";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            // Material data length
            wrap.putInt(0);
        }
        // Textures count
        wrap.putInt(1);
        {
            String className = "dummy.dummy.UnknownTextureClass";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            // Texture data length
            wrap.putInt(0);
        }
        // Objects count
        wrap.putInt(0);

        // Environment mode
        wrap.putShort((short) 0); // Set scene environment to SOLID
        {
            System.out.println(wrap.position());
            colorToBuffer(new RGBColor(45, 45, 45), wrap);
        }
        Scene scene = new Scene(StreamUtil.stream(wrap), true);
        Assertions.assertEquals(1, scene.getNumTextures());
        Assertions.assertTrue(scene.getTexture(0) instanceof UniformTexture);
        Assertions.assertTrue(scene.getTexture(0).getName().equals("<unreadable>"));
        Assertions.assertEquals(1, scene.getNumMaterials());
        Assertions.assertTrue(scene.getMaterial(0) instanceof UniformMaterial);
        Assertions.assertTrue(scene.getMaterial(0).getName().equals("<unreadable>"));
        Assertions.assertFalse(scene.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Test Read Empty Scene With Missed Material And Bad Texture")
    void testReadEmptySceneWithMissedMaterialAndBadTexture() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 2); // Scene Version 2. No metadata expected to set

        // Ambient color data
        colorToBuffer(new RGBColor(100, 200, 200), wrap);
        // Fog color data
        colorToBuffer(new RGBColor(50, 50, 50), wrap);
        // Fog
        wrap.put((byte) 1);
        // Fog Distance
        wrap.putDouble(1000);
        // show grid
        wrap.put((byte) 1);
        // snap to grid
        wrap.put((byte) 1);
        // grid spacing
        wrap.putDouble(10);
        // grid Subdivisions
        wrap.putInt(10);
        // FPS
        wrap.putInt(60);
        // Image maps count
        wrap.putInt(0);
        // Materials count
        wrap.putInt(1);
        {
            String className = "dummy.dummy.UnknownMaterialClass";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            // Material data length
            wrap.putInt(0);
        }
        // Textures count
        wrap.putInt(1);
        {
            String className = DummyTextureNoConstructor.class.getTypeName();
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            // Texture data length
            wrap.putInt(0);
        }
        // Objects count
        wrap.putInt(0);
        // Environment mode
        wrap.putShort((short) 0); // Solid EM
        {
            System.out.println(wrap.position());
            colorToBuffer(new RGBColor(45, 45, 45), wrap);
        }
        Scene scene = new Scene(StreamUtil.stream(wrap), true);
        Assertions.assertEquals(1, scene.getNumTextures());
        Assertions.assertTrue(scene.getTexture(0) instanceof UniformTexture);
        Assertions.assertTrue(scene.getTexture(0).getName().equals("<unreadable>"));
        Assertions.assertEquals(1, scene.getNumMaterials());
        Assertions.assertTrue(scene.getMaterial(0) instanceof UniformMaterial);
        Assertions.assertTrue(scene.getMaterial(0).getName().equals("<unreadable>"));
        Assertions.assertFalse(scene.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Test Read Empty Scene With Missed Material")
    void testReadEmptySceneWithMissedMaterial() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Scene Version 2. No metadata expected to  set
        wrap.putShort((short) 2);
        // Ambient color data
        colorToBuffer(new RGBColor(100, 200, 200), wrap);
        // Fog color data
        colorToBuffer(new RGBColor(50, 50, 50), wrap);
        // Fog
        wrap.put((byte) 1);
        // Fog Distance
        wrap.putDouble(1000);
        // show grid
        wrap.put((byte) 1);
        // snap to grid
        wrap.put((byte) 1);
        // grid spacing
        wrap.putDouble(10);
        // grid Subdivisions
        wrap.putInt(10);
        // FPS
        wrap.putInt(60);
        // Image maps count
        wrap.putInt(0);
        // Materials count
        wrap.putInt(1);
        {
            String className = "dummy.dummy.UnknownMaterialClass";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            // Material data length
            wrap.putInt(0);
        }
        // Textures count
        wrap.putInt(1);
        {
            String className = LoadableTexture.class.getTypeName();
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            // Texture data length
            wrap.putInt(0);
        }
        // Objects count
        wrap.putInt(0);
        // Environment mode
        // Solid EM
        wrap.putShort((short) 0);
        {
            System.out.println(wrap.position());
            colorToBuffer(new RGBColor(45, 45, 45), wrap);
        }
        Scene scene = new Scene(StreamUtil.stream(wrap), true);
        Assertions.assertEquals(1, scene.getNumTextures());
        Assertions.assertTrue(scene.getTexture(0) instanceof LoadableTexture);
        Assertions.assertEquals(1, scene.getNumMaterials());
        Assertions.assertTrue(scene.getMaterial(0) instanceof UniformMaterial);
        Assertions.assertTrue(scene.getMaterial(0).getName().equals("<unreadable>"));
        Assertions.assertFalse(scene.getErrors().isEmpty());
    }

    // This test fails as no environment texture loaded. in general this is impossible situation
    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Read Empty Scene Settings Only No Meta")
    void testReadEmptySceneSettingsOnlyNoMeta() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Scene Version 2. No metadata expected to  set
            wrap.putShort((short) 2);
            // Ambient color data
            colorToBuffer(new RGBColor(100, 200, 200), wrap);
            // Fog color data
            colorToBuffer(new RGBColor(50, 50, 50), wrap);
            // Fog
            wrap.put((byte) 1);
            // Fog Distance
            wrap.putDouble(1000);
            // show grid
            wrap.put((byte) 1);
            // snap to grid
            wrap.put((byte) 1);
            // grid spacing
            wrap.putDouble(10);
            // grid Subdivisions
            wrap.putInt(10);
            // FPS
            wrap.putInt(60);
            // Image maps count
            wrap.putInt(0);
            // Materials count
            wrap.putInt(0);
            // Textures count
            wrap.putInt(0);
            // Objects count
            wrap.putInt(0);
            // Environment mode
            // Solid EM
            wrap.putInt(0);
            {
            }
            new Scene(StreamUtil.stream(wrap), true);
        });
    }

    private static void colorToBuffer(RGBColor color, ByteBuffer buffer) {
        buffer.putFloat(color.getRed());
        buffer.putFloat(color.getGreen());
        buffer.putFloat(color.getBlue());
    }

    @DisplayName("Loadable Texture")
    static class LoadableTexture extends UniformTexture {

        public LoadableTexture(DataInputStream in, Scene theScene) {
        }
    }

    @DisplayName("Dummy Texture No Constructor")
    class DummyTextureNoConstructor extends Texture {

        public DummyTextureNoConstructor() {
        }

        @Override
        public boolean hasComponent(int component) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void getAverageSpec(TextureSpec spec, double time, double[] param) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TextureMapping getDefaultMapping(Object3D object) {
            return new UniformMapping(object, this);
        }

        @Override
        public Texture duplicate() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void edit(WindowWidget fr, Scene sc) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
