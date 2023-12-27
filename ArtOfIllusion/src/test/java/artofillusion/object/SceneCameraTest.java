/* Copyright (C) 2018-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import artofillusion.Scene;
import artofillusion.image.ComplexImage;
import artofillusion.image.filter.ImageFilter;
import artofillusion.math.CoordinateSystem;
import artofillusion.test.util.StreamUtil;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author maksim.khramov
 */
@DisplayName("Scene Camera Test")
class SceneCameraTest {

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Load Scene Camera Bad Version 1")
    void loadSceneCameraBadVersion1() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(2);
            // Object Version
            wrap.putShort((short) -1);
            new SceneCamera(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Load Scene Camera Bad Version 1 Read Vesion Again")
    void loadSceneCameraBadVersion1ReadVesionAgain() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(4);
            // Object Version
            wrap.putShort((short) 1);
            // Object Version read AGAIN !!!
            wrap.putShort((short) -1);
            new SceneCamera(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Load Scene Camera Bad Version 2")
    void loadSceneCameraBadVersion2() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(2);
            // Object Version
            wrap.putShort((short) 2);
            new SceneCamera(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Load Scene Camera Bad Version 2 Read Vesion Again")
    void loadSceneCameraBadVersion2ReadVesionAgain() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(4);
            // Object Version
            wrap.putShort((short) 1);
            // Object Version read AGAIN !!!
            wrap.putShort((short) 3);
            new SceneCamera(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @DisplayName("Test Load Scene Camera Version 0")
    void testLoadSceneCameraVersion0() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object Version
        wrap.putShort((short) 1);
        // Object Version read AGAIN !!!
        wrap.putShort((short) 0);
        // FOV
        wrap.putDouble(90);
        // DOF
        wrap.putDouble(500);
        // Focal distance
        wrap.putDouble(1000);
        // Camera filters count
        wrap.putInt(0);
        SceneCamera sc = new SceneCamera(StreamUtil.stream(wrap), scene);
        Assertions.assertNotNull(sc);
        Assertions.assertEquals(90, sc.getFieldOfView(), 0);
        Assertions.assertEquals(500, sc.getDepthOfField(), 0);
        Assertions.assertEquals(1000, sc.getFocalDistance(), 0);
        Assertions.assertTrue(sc.isPerspective());
        Assertions.assertEquals(0, sc.getImageFilters().length);
    }

    @Test
    @DisplayName("Test Load Scene Camera Version 2")
    void testLoadSceneCameraVersion2() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object Version
        wrap.putShort((short) 1);
        // Object Version read AGAIN !!!
        wrap.putShort((short) 2);
        // FOV
        wrap.putDouble(90);
        // DOF
        wrap.putDouble(500);
        // Focal distance
        wrap.putDouble(1000);
        // Perspective camera. Boolean treats as byte
        wrap.put((byte) 1);
        // Camera filters count
        wrap.putInt(0);
        SceneCamera sc = new SceneCamera(StreamUtil.stream(wrap), scene);
        Assertions.assertNotNull(sc);
        Assertions.assertEquals(90, sc.getFieldOfView(), 0);
        Assertions.assertEquals(500, sc.getDepthOfField(), 0);
        Assertions.assertEquals(1000, sc.getFocalDistance(), 0);
        Assertions.assertTrue(sc.isPerspective());
        Assertions.assertEquals(0, sc.getImageFilters().length);
    }

    @Test
    @DisplayName("Test Load Scene Camera Version 2 No Persp")
    void testLoadSceneCameraVersion2NoPersp() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object Version
        wrap.putShort((short) 1);
        // Object Version read AGAIN !!!
        wrap.putShort((short) 2);
        // FOV
        wrap.putDouble(90);
        // DOF
        wrap.putDouble(500);
        // Focal distance
        wrap.putDouble(1000);
        // Non Perspective camera. Boolean treats as byte
        wrap.put((byte) 0);
        // Camera filters count
        wrap.putInt(0);
        SceneCamera sc = new SceneCamera(StreamUtil.stream(wrap), scene);
        Assertions.assertNotNull(sc);
        Assertions.assertEquals(90, sc.getFieldOfView(), 0);
        Assertions.assertEquals(500, sc.getDepthOfField(), 0);
        Assertions.assertEquals(1000, sc.getFocalDistance(), 0);
        Assertions.assertTrue(!sc.isPerspective());
        Assertions.assertEquals(0, sc.getImageFilters().length);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Load Camera With Bad Filter")
    void testLoadCameraWithBadFilter() {
        assertThrows(IOException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Object Version
            wrap.putShort((short) 1);
            // Object Version read AGAIN !!!
            wrap.putShort((short) 2);
            // FOV
            wrap.putDouble(90);
            // DOF
            wrap.putDouble(500);
            // Focal distance
            wrap.putDouble(1000);
            // Non Perspective camera. Boolean treats as byte
            wrap.put((byte) 0);
            // Camera filters count
            wrap.putInt(1);
            String className = "dummy.dummy.UnknownFilterClass";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            new SceneCamera(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @DisplayName("Test Load Camera With Good Filter")
    void testLoadCameraWithGoodFilter() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object Version
        wrap.putShort((short) 1);
        // Object Version read AGAIN !!!
        wrap.putShort((short) 2);
        // FOV
        wrap.putDouble(90);
        // DOF
        wrap.putDouble(500);
        // Focal distance
        wrap.putDouble(1000);
        // Non Perspective camera. Boolean treats as byte
        wrap.put((byte) 0);
        // Camera filters count
        wrap.putInt(1);
        String className = DummyImageFilter.class.getTypeName();
        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        SceneCamera sc = new SceneCamera(StreamUtil.stream(wrap), scene);
        Assertions.assertNotNull(sc);
        Assertions.assertEquals(1, sc.getImageFilters().length);
    }

    @Test
    @DisplayName("Test Scene Camera Duplicate")
    void testSceneCameraDuplicate() {
        SceneCamera sc = new SceneCamera();
        sc.setDistToPlane(300);
        sc.setDepthOfField(500);
        sc.setFieldOfView(90);
        sc.setPerspective(false);
        sc.setImageFilters(new ImageFilter[]{new DummyImageFilter()});
        SceneCamera clone = sc.duplicate();
        Assertions.assertNotNull(clone);
        Assertions.assertEquals(sc.getDistToPlane(), clone.getDistToPlane(), 0);
        Assertions.assertEquals(sc.getDepthOfField(), clone.getDepthOfField(), 0);
        Assertions.assertEquals(sc.getFieldOfView(), clone.getFieldOfView(), 0);
        Assertions.assertEquals(sc.isPerspective(), clone.isPerspective());
        Assertions.assertEquals(sc.getImageFilters().length, clone.getImageFilters().length);
    }

    @Test
    @DisplayName("Test Scene Camera Copy Object")
    void testSceneCameraCopyObject() {
        SceneCamera sc = new SceneCamera();
        sc.setDistToPlane(300);
        sc.setDepthOfField(500);
        sc.setFieldOfView(90);
        sc.setPerspective(false);
        sc.setImageFilters(new ImageFilter[]{new DummyImageFilter()});
        SceneCamera clone = new SceneCamera();
        clone.copyObject(sc);
        Assertions.assertEquals(sc.getDistToPlane(), clone.getDistToPlane(), 0);
        Assertions.assertEquals(sc.getDepthOfField(), clone.getDepthOfField(), 0);
        Assertions.assertEquals(sc.getFieldOfView(), clone.getFieldOfView(), 0);
        Assertions.assertEquals(sc.isPerspective(), clone.isPerspective());
        Assertions.assertEquals(sc.getImageFilters().length, clone.getImageFilters().length);
    }

    @DisplayName("Dummy Image Filter")
    static class DummyImageFilter extends ImageFilter {

        @Override
        public String getName() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) throws IOException {
        }
    }
}
