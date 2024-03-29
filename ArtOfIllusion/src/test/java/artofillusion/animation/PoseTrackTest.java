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
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class PoseTrackTest {

    @Test
    public void testCreatePoseTrack() {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack pt = new PoseTrack(oi);
        Assert.assertNotNull(pt);
        Assert.assertEquals(oi, pt.getParent());
        Assert.assertEquals("Pose", pt.getName());
    }

    @Test(expected = InvalidObjectException.class)
    public void testLoadPoseTrackBadVersion0() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) -1); // Track Version

        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack pt = new PoseTrack(oi);
        pt.initFromStream(StreamUtil.stream(wrap), (Scene) null);
    }

    @Test(expected = InvalidObjectException.class)
    public void testLoadPoseTrackBadVersion1() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 3); // Track Version

        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack pt = new PoseTrack(oi);
        pt.initFromStream(StreamUtil.stream(wrap), (Scene) null);
    }
}
