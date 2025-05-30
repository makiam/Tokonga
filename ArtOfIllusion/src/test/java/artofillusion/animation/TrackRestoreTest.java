/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.Scene;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;


@Slf4j
public class TrackRestoreTest {

    @Test
    void testWriteAndRestoreTrack() throws IOException {
        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack track = new PoseTrack(owner);
        track.setName("My Pose");
        track.setSmoothingMethod(Timecourse.APPROXIMATING);
        Assertions.assertEquals(0, owner.getTracks().length);

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);
        data.writeInt(1);
        TrackIO.INSTANCE.writeTrack(data, scene, track, (short)5);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        TrackIO.INSTANCE.readTracks(new DataInputStream(in), scene, owner, (short)5);

        Assertions.assertEquals(1, owner.getTracks().length);
        PoseTrack restored = (PoseTrack)owner.getTracks()[0];

        Assertions.assertEquals("My Pose", restored.getName());
        Assertions.assertEquals(Timecourse.APPROXIMATING, restored.getSmoothingMethod());

    }



}
