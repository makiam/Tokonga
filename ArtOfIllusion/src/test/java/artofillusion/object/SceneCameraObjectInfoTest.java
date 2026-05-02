/* Copyright (C) 2025-2026 by Maksim Khramov

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
import artofillusion.test.util.ReadBypassEventListener;
import artofillusion.test.util.StreamUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

@DisplayName("Object Info Test for Scene Camera with filters")
@Slf4j
class SceneCameraObjectInfoTest {

    static ReadBypassEventListener listener;

    @BeforeAll
    static void setupClass() {
        listener = new ReadBypassEventListener();
    }

    @BeforeEach
    void resetCounterBefore() {
        listener.reset();
    }

    @Test
    void testSceneCameraObjectInfo() throws IOException {
        var sc = new SceneCamera();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        byte[] innerObjectBytes = bos.toByteArray();
        int empty = innerObjectBytes.length;
        log.info("Size: {}", empty);

        var expectedIncrement = StreamUtil.getUTFNameAsByteArray(TestSceneCameraFilterNoData.class.getName()).length + 4;

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);
        innerObjectBytes = bos.toByteArray();
        Assertions.assertEquals(empty + expectedIncrement * 3 , innerObjectBytes.length);
        log.info("Size: {}",  innerObjectBytes.length);
    }

    @Test
    void testSceneCameraObjectInfoWithMoreFiltersData() throws IOException {
        var sc = new SceneCamera();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        byte[] innerObjectBytes = bos.toByteArray();
        int empty = innerObjectBytes.length;
        log.info("Size: {}", empty);

        var expectedIncrement = StreamUtil.getUTFNameAsByteArray(TestSceneCameraFilterNoData.class.getName()).length;
        expectedIncrement *=3;
        expectedIncrement +=    StreamUtil.getUTFNameAsByteArray(TestSceneCameraFilterWithDouble.class.getName()).length;
        expectedIncrement +=8;

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterWithDouble());
        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        expectedIncrement += filters.size()*4;

        bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);
        innerObjectBytes = bos.toByteArray();
        Assertions.assertEquals(empty + expectedIncrement , innerObjectBytes.length);
        log.info("Size: {}",  innerObjectBytes.length);
    }

    @Test
    void testToRestoreBadFilter1() throws IOException {
        var sc = new SceneCamera();

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new MissedConstructorFilter());

        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        var innerObjectBytes = bos.toByteArray();
        var restore = new ByteArrayInputStream(innerObjectBytes);

        new SceneCamera(new DataInputStream(restore), null);
        Assertions.assertEquals(1, listener.getCounter());
        Assertions.assertTrue(listener.getLast().message().contains(MissedConstructorFilter.class.getName()));
        Assertions.assertInstanceOf(NoSuchMethodException.class, listener.getLast().cause());

    }

    @Test
    void testToRestoreBadFilter2() throws IOException {
        var sc = new SceneCamera();

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new PrivateClassFilter());

        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        var innerObjectBytes = bos.toByteArray();
        var restore = new ByteArrayInputStream(innerObjectBytes);


        new SceneCamera(new DataInputStream(restore), null);
        Assertions.assertEquals(1, listener.getCounter());
        Assertions.assertTrue(listener.getLast().message().contains(PrivateClassFilter.class.getName()));
        Assertions.assertTrue(listener.getLast().cause() instanceof IllegalAccessException);
    }

    @Test
    @DisplayName("Create and restore Scene Camera with expected ReflectiveOperationException")
    void testSceneCameraObjectInfoWithMoreFilters() throws IOException {
        var sc = new SceneCamera();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        byte[] innerObjectBytes = bos.toByteArray();
        int empty = innerObjectBytes.length;
        log.info("Size: {}", empty);

        var expectedIncrement = StreamUtil.getUTFNameAsByteArray(TestSceneCameraFilterNoData.class.getName()).length;
        expectedIncrement *=4;
        expectedIncrement +=    StreamUtil.getUTFNameAsByteArray(TestSceneCameraFilterWithDouble.class.getName()).length;
        expectedIncrement +=8;
        expectedIncrement += StreamUtil.getUTFNameAsByteArray(PrivateTestFilter.class.getName()).length;

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterWithDouble());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new PrivateTestFilter());
        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        expectedIncrement += filters.size()*4;

        bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);
        innerObjectBytes = bos.toByteArray();
        Assertions.assertEquals(empty + expectedIncrement , innerObjectBytes.length);
        log.info("Size: {}",  innerObjectBytes.length);

        var restore = new ByteArrayInputStream(innerObjectBytes);
        var copy = new SceneCamera(new DataInputStream(restore), null);
        var tdf = (TestSceneCameraFilterWithDouble)copy.getImageFilters()[3];
        Assertions.assertEquals(Math.PI, tdf.getValue());
        var last = copy.getImageFilters()[4];
        Assertions.assertInstanceOf(TestSceneCameraFilterNoData.class, last);
        last = copy.getImageFilters()[5];
        Assertions.assertInstanceOf(PrivateTestFilter.class, last);
    }

    @DisplayName("As class is private it expected to throw InstantiationException from newInstance() call")
    static class PrivateTestFilter extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) { /* No implementation need */ }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) { /* No implementation need */ }

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) { /* No implementation need */ }
    }

    static class TestSceneCameraFilterNoData extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) { /* No implementation need */ }

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) { /* No implementation need */ }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) { /* No implementation need */ }
    }

    static class TestSceneCameraFilterWithDouble extends ImageFilter {
        @Getter private Double value;
        @Override
        public String getName() {
            return "";
        }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
            out.writeDouble(Math.PI);
        }

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) throws IOException {
            value = in.readDouble();
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) { /* No implementation need */ }
    }

    @DisplayName("As class is not marked as STATIC expected no-args constructor cannot be found")
    class MissedConstructorFilter extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) { /* No implementation need */ }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) { /* No implementation need */ }

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) { /* No implementation need */ }
    }

    @DisplayName("As class is marked as PRIVATE expected IllegalAccessException exception to be thrown")
    private static class PrivateClassFilter extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) { /* No implementation need */ }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) { /* No implementation need */ }

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) { /* No implementation need */ }
    }


}
