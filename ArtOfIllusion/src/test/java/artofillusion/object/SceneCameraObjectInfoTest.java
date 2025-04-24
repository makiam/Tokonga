/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.math.CoordinateSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@DisplayName("Object Info Test for Scene Camera with filters")
public class SceneCameraObjectInfoTest {

    @Test
    public void testSceneCameraObjectInfo() throws IOException {
        var sc = new SceneCamera();
        var oi = new ObjectInfo(sc, new CoordinateSystem(), "Scene Camera");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sc.writeToFile(new DataOutputStream(bos), null);
        byte[] innerObjectBytes = bos.toByteArray();


    }
}
