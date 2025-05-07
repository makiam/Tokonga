/* Copyright (C) 2018-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.Camera;
import artofillusion.Scene;
import artofillusion.image.ComplexImage;
import artofillusion.image.filter.ImageFilter;
import artofillusion.math.CoordinateSystem;
import artofillusion.test.util.StreamUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @DisplayName("Load Scene Camera Bad Version 1 Read Version Again")
    void loadSceneCameraBadVersion1ReadVersionAgain() {
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
    @DisplayName("Load Scene Camera Bad Version 2 Read Version Again")
    void loadSceneCameraBadVersion2ReadVersionAgain() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(4);
            // Object Version
            wrap.putShort((short) 1);
            // Object Version read AGAIN !!!
            wrap.putShort((short) 5);
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
        Assertions.assertThrows(InvalidObjectException.class, () -> new SceneCamera(StreamUtil.stream(wrap), scene));

//        Assertions.assertNotNull(sc);
//        Assertions.assertEquals(90, sc.getFieldOfView(), 0);
//        Assertions.assertEquals(500, sc.getDepthOfField(), 0);
//        Assertions.assertEquals(1000, sc.getFocalDistance(), 0);
//        Assertions.assertTrue(sc.isPerspective());
//        Assertions.assertEquals(0, sc.getImageFilters().length);
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
        Assertions.assertEquals(Camera.DEFAULT_DISTANCE_TO_SCREEN, sc.getDistToPlane());
        Assertions.assertEquals(90, sc.getFieldOfView(), 0);
        Assertions.assertEquals(500, sc.getDepthOfField(), 0);
        Assertions.assertEquals(1000, sc.getFocalDistance(), 0);
        Assertions.assertTrue(sc.isPerspective());
        Assertions.assertEquals(0, sc.getImageFilters().length);
    }

    @Test
    @DisplayName("Test Load Scene Camera Version 3")
    void testLoadSceneCameraVersion3() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object Version
        wrap.putShort((short) 1);
        // Object Version read AGAIN !!!
        wrap.putShort((short) 3);
        // DistToPlane
        wrap.putDouble(1.23456);
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
        Assertions.assertEquals(1.23456, sc.getDistToPlane());
        Assertions.assertEquals(90, sc.getFieldOfView(), 0);
        Assertions.assertEquals(500, sc.getDepthOfField(), 0);
        Assertions.assertEquals(1000, sc.getFocalDistance(), 0);
        Assertions.assertTrue(sc.isPerspective());
        Assertions.assertEquals(0, sc.getImageFilters().length);
    }

    @Test
    @DisplayName("Test Load Scene Camera Version 3 With Single Filter")
    void testLoadSceneCameraVersion3WithSingleFilter() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object Version
        wrap.putShort((short) 1);
        // Object Version read AGAIN !!!
        wrap.putShort((short) 3);
        // DistToPlane
        wrap.putDouble(1.23456);
        // FOV
        wrap.putDouble(90);
        // DOF
        wrap.putDouble(500);
        // Focal distance
        wrap.putDouble(1000);
        // Perspective camera. Boolean treats as byte
        wrap.put((byte) 1);
        // Camera filters count
        wrap.putInt(1);

        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = charset.encode(TestFilter.class.getName());

        wrap.put(byteBuffer.array(), 0, byteBuffer.array().length);

        SceneCamera sc = new SceneCamera(StreamUtil.stream(wrap), scene);
        Assertions.assertNotNull(sc);
        Assertions.assertEquals(1.23456, sc.getDistToPlane());
        Assertions.assertEquals(90, sc.getFieldOfView(), 0);
        Assertions.assertEquals(500, sc.getDepthOfField(), 0);
        Assertions.assertEquals(1000, sc.getFocalDistance(), 0);
        Assertions.assertTrue(sc.isPerspective());
        Assertions.assertEquals(1, sc.getImageFilters().length);
    }

    static class TestFilter extends ImageFilter {

        @Override
        public String getName() {
            return "TestFilter";
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {

        }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {

        }

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) throws IOException {

        }
    }
    @Test
    @DisplayName("Test Load Scene Camera Version 2 No Perspective")
    void testLoadSceneCameraVersion2NoPerspective() throws IOException {
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
        Assertions.assertFalse(sc.isPerspective());
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
    @DisplayName("Test Scene Camera Duplicate")
    void testSceneCameraDuplicate2() {
        SceneCamera sc = new SceneCamera();
        sc.setDistToPlane(300);
        sc.setDepthOfField(500);
        sc.setFieldOfView(90);
        sc.setPerspective(false);
        sc.setImageFilters(new ImageFilter[]{new DummyImageFilter(), new DummyImageFilter()});
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

    @Test
    @DisplayName("Test Scene Camera Copy Object")
    void testSceneCameraCopyObject2() {
        SceneCamera sc = new SceneCamera();
        sc.setDistToPlane(300);
        sc.setDepthOfField(500);
        sc.setFieldOfView(90);
        sc.setPerspective(false);
        sc.setImageFilters(new ImageFilter[]{new DummyImageFilter(), new DummyImageFilter()});
        SceneCamera clone = new SceneCamera();
        clone.copyObject(sc);
        Assertions.assertEquals(sc.getDistToPlane(), clone.getDistToPlane(), 0);
        Assertions.assertEquals(sc.getDepthOfField(), clone.getDepthOfField(), 0);
        Assertions.assertEquals(sc.getFieldOfView(), clone.getFieldOfView(), 0);
        Assertions.assertEquals(sc.isPerspective(), clone.isPerspective());
        Assertions.assertEquals(sc.getImageFilters().length, clone.getImageFilters().length);
    }

    @Test
    void testWriteCameraAndReadBack() throws IOException {
        Scene scene = new Scene();
        SceneCamera sc = new SceneCamera();
        sc.setDistToPlane(300);
        sc.setDepthOfField(500);
        sc.setFieldOfView(90);
        sc.setPerspective(false);
        sc.setImageFilters(new ImageFilter[]{new DummyImageFilter()});

        AccumulatorStream sa = new AccumulatorStream();
        sc.writeToFile(new DataOutputStream(sa), scene);

        SceneCamera copy = new SceneCamera(sa.getStream(), scene);

        Assertions.assertEquals(sc.getDistToPlane(), copy.getDistToPlane());
        Assertions.assertEquals(sc.getDepthOfField(), copy.getDepthOfField());
        Assertions.assertEquals(sc.isPerspective(), copy.isPerspective());
        Assertions.assertEquals(sc.getFieldOfView(), copy.getFieldOfView());
        Assertions.assertEquals(1, copy.getImageFilters().length);

    }

    @Test
    void testWriteCameraAndReadBackThreeFiltersIn() throws IOException {
        Scene scene = new Scene();
        SceneCamera sc = new SceneCamera();
        sc.setDistToPlane(300);
        sc.setDepthOfField(500);
        sc.setFieldOfView(90);
        sc.setPerspective(false);
        sc.setImageFilters(new ImageFilter[]{new DummyImageFilter(), new DummyImageFilter(), new DummyImageFilter()});

        AccumulatorStream sa = new AccumulatorStream();
        sc.writeToFile(new DataOutputStream(sa), scene);

        SceneCamera copy = new SceneCamera(sa.getStream(), scene);

        Assertions.assertEquals(sc.getDistToPlane(), copy.getDistToPlane());
        Assertions.assertEquals(sc.getDepthOfField(), copy.getDepthOfField());
        Assertions.assertEquals(sc.isPerspective(), copy.isPerspective());
        Assertions.assertEquals(sc.getFieldOfView(), copy.getFieldOfView());
        Assertions.assertEquals(3, copy.getImageFilters().length);

    }

    static class AccumulatorStream extends OutputStream {
        ByteBuffer wrap = ByteBuffer.allocate(20000);

        @Override
        public void write(int b) throws IOException {
            wrap.put((byte)b);
        }

        DataInputStream getStream() {
            return new DataInputStream(new ByteArrayInputStream(wrap.array()));
        }


    }

}
