/* Copyright (C) 2018 by Maksim Khramov

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
import artofillusion.test.util.StreamUtil;

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
@DisplayName("Pose Track Test")
class PoseTrackTest {

    @Test
    @DisplayName("Test Create Pose Track")
    void testCreatePoseTrack() {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack pt = new PoseTrack(oi);
        Assertions.assertNotNull(pt);
        Assertions.assertEquals(oi, pt.getParent());
        Assertions.assertEquals(pt.getName(), "Pose");
    }

    @Test
    @DisplayName("Test Load Pose Track Bad Version 0")
    void testLoadPoseTrackBadVersion0() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Track Version
            wrap.putShort((short) -1);
            Object3D obj = new Cube(1, 1, 1);
            ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
            PoseTrack pt = new PoseTrack(oi);
            pt.initFromStream(StreamUtil.stream(wrap), (Scene) null);
        });
    }

    @Test
    @DisplayName("Test Load Pose Track Bad Version 1")
    void testLoadPoseTrackBadVersion1() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Track Version
            wrap.putShort((short) 3);
            Object3D obj = new Cube(1, 1, 1);
            ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
            PoseTrack pt = new PoseTrack(oi);
            pt.initFromStream(StreamUtil.stream(wrap), (Scene) null);
        });
    }
}
