/* Copyright (C) 2025 by Maksim Khramov

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
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

@DisplayName("Object Info Test for Scene Camera with filters")
public class SceneCameraObjectInfoTest {



    @Test
    public void testSceneCameraObjectInfo() throws IOException {
        var sc = new SceneCamera();
        var oi = new ObjectInfo(sc, new CoordinateSystem(), "Scene Camera");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        byte[] innerObjectBytes = bos.toByteArray();
        int empty = innerObjectBytes.length;
        System.out.println("Size: " + empty);

        var expectedIncrement = StreamUtil.getUTFNameBufferSize(TestSceneCameraFilterNoData.class.getName());

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);
        innerObjectBytes = bos.toByteArray();
        Assertions.assertEquals(empty + expectedIncrement * 3 , innerObjectBytes.length);
        System.out.println("Size: " + innerObjectBytes.length);
    }

    @Test
    public void testSceneCameraObjectInfoWithMoreFiltersData() throws IOException {
        var sc = new SceneCamera();
        var oi = new ObjectInfo(sc, new CoordinateSystem(), "Scene Camera");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        byte[] innerObjectBytes = bos.toByteArray();
        int empty = innerObjectBytes.length;
        System.out.println("Size: " + empty);

        var expectedIncrement = StreamUtil.getUTFNameBufferSize(TestSceneCameraFilterNoData.class.getName());
        expectedIncrement *=3;
        expectedIncrement +=    StreamUtil.getUTFNameBufferSize(TestSceneCameraFilterWithDouble.class.getName());
        expectedIncrement +=8;

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterWithDouble());
        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);
        innerObjectBytes = bos.toByteArray();
        Assertions.assertEquals(empty + expectedIncrement , innerObjectBytes.length);
        System.out.println("Size: " + innerObjectBytes.length);
    }

    @Test
    public void testToRestoreBadFilter1() throws IOException {
        var sc = new SceneCamera();

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new MissedConstructorFilter());

        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        var innerObjectBytes = bos.toByteArray();
        var restore = new ByteArrayInputStream(innerObjectBytes);


        Exception ee = Assertions.assertThrows(IOException.class, () -> {
            new SceneCamera(new DataInputStream(restore), null);
        });

        Assertions.assertTrue(ee.getCause() instanceof NoSuchMethodException);
    }

    @Test
    public void testToRestoreBadFilter2() throws IOException {
        var sc = new SceneCamera();

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new PrivateClassFilter());

        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        var innerObjectBytes = bos.toByteArray();
        var restore = new ByteArrayInputStream(innerObjectBytes);


        Exception ee = Assertions.assertThrows(IOException.class, () -> {
            new SceneCamera(new DataInputStream(restore), null);
        });

        Assertions.assertTrue(ee.getCause() instanceof IllegalAccessException);
    }

    @Test
    @DisplayName("Create and restore Scene Camera with expected ReflectiveOperationException")
    public void testSceneCameraObjectInfoWithMoreFilters() throws IOException {
        var sc = new SceneCamera();
        var oi = new ObjectInfo(sc, new CoordinateSystem(), "Scene Camera");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        byte[] innerObjectBytes = bos.toByteArray();
        int empty = innerObjectBytes.length;
        System.out.println("Size: " + empty);

        var expectedIncrement = StreamUtil.getUTFNameBufferSize(TestSceneCameraFilterNoData.class.getName());
        expectedIncrement *=4;
        expectedIncrement +=    StreamUtil.getUTFNameBufferSize(TestSceneCameraFilterWithDouble.class.getName());
        expectedIncrement +=8;
        expectedIncrement += StreamUtil.getUTFNameBufferSize(PrivateTestFilter.class.getName());

        var filters = new ArrayList<>(Arrays.asList(sc.getImageFilters()));
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new TestSceneCameraFilterWithDouble());
        filters.add(new TestSceneCameraFilterNoData());
        filters.add(new PrivateTestFilter());
        sc.setImageFilters(filters.toArray(ImageFilter[]::new));

        bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);
        innerObjectBytes = bos.toByteArray();
        Assertions.assertEquals(empty + expectedIncrement , innerObjectBytes.length);
        System.out.println("Size: " + innerObjectBytes.length);

        var restore = new ByteArrayInputStream(innerObjectBytes);
        var copy = new SceneCamera(new DataInputStream(restore), null);
        var tdf = (TestSceneCameraFilterWithDouble)copy.getImageFilters()[3];
        Assertions.assertEquals(Math.PI, tdf.getValue());
        var last = copy.getImageFilters()[4];
        Assertions.assertTrue(last instanceof TestSceneCameraFilterNoData);
        last = copy.getImageFilters()[5];
        Assertions.assertTrue(last instanceof PrivateTestFilter);
    }

    @DisplayName("As class is private it expected to throw InstantiationException from newInstance() call")
    static class PrivateTestFilter extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {}

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {}

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) throws IOException {}
    }

    static class TestSceneCameraFilterNoData extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {}

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) throws IOException {}

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {}
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
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {}
    }

    @DisplayName("As class is not marked as STATIC expected no-args constructor cannot be found")
    class MissedConstructorFilter extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {}

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {}

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) throws IOException {}
    }

    @DisplayName("As class is marked as PRIVATE expected IllegalAccessException exception to be thrown")
    private static class PrivateClassFilter extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {}

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {}

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) throws IOException {}
    }


}
