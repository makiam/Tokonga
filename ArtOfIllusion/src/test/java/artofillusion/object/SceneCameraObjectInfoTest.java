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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

@DisplayName("Object Info Test for Scene Camera with filters")
public class SceneCameraObjectInfoTest {
    private static int getUTFNameBufferSize(String name) throws IOException {
        ByteArrayOutputStream nameStream = new ByteArrayOutputStream();
        new DataOutputStream(nameStream).writeUTF(name);
        return nameStream.toByteArray().length;
    }

    @Test
    public void testSceneCameraObjectInfo() throws IOException {
        var sc = new SceneCamera();
        var oi = new ObjectInfo(sc, new CoordinateSystem(), "Scene Camera");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        byte[] innerObjectBytes = bos.toByteArray();
        int empty = innerObjectBytes.length;
        System.out.println("Size: " + empty);

        var expectedIncrement = SceneCameraObjectInfoTest.getUTFNameBufferSize(TestSceneCameraFilterNoData.class.getName());

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
    public void testSceneCameraObjectInfoWIthMoreFiltersData() throws IOException {
        var sc = new SceneCamera();
        var oi = new ObjectInfo(sc, new CoordinateSystem(), "Scene Camera");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);

        byte[] innerObjectBytes = bos.toByteArray();
        int empty = innerObjectBytes.length;
        System.out.println("Size: " + empty);

        var expectedIncrement = SceneCameraObjectInfoTest.getUTFNameBufferSize(TestSceneCameraFilterNoData.class.getName());
        expectedIncrement *=3;
        expectedIncrement +=    SceneCameraObjectInfoTest.getUTFNameBufferSize(TestSceneCameraFilterWithDouble.class.getName());
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

    private static class TestSceneCameraFilterNoData extends ImageFilter {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        }

        @Override
        public void initFromStream(DataInputStream in, Scene theScene) throws IOException {

        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {

        }
    }

    private static class TestSceneCameraFilterWithDouble extends ImageFilter {

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

        }

        @Override
        public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {

        }
    }
}
