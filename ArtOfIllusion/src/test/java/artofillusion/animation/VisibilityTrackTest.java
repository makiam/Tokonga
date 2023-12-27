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
import artofillusion.object.NullObject;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;
import artofillusion.test.util.StreamUtil;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author maksim.khramov
 */
@DisplayName("Visibility Track Test")
class VisibilityTrackTest {

    @Test
    @DisplayName("Test Create Visibility Track")
    void testCreateVisibilityTrack() {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        VisibilityTrack vt = new VisibilityTrack(oi);
        Assertions.assertNotNull(vt);
        Assertions.assertEquals(oi, vt.getParent());
    }

    @Test
    @DisplayName("Test Load Track Bad Version")
    void testLoadTrackBadVersion() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Track Version
            wrap.putShort((short) 1);
            Object3D obj = new Cube(1, 1, 1);
            ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
            VisibilityTrack vt = new VisibilityTrack(oi);
            vt.initFromStream(StreamUtil.stream(wrap), (Scene) null);
        });
    }

    @Test
    @DisplayName("Test Load Visibility Track From Stream No Keys")
    void testLoadVisibilityTrackFromStreamNoKeys() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Track Version
        wrap.putShort((short) 0);
        {
            String trackName = "Visibility Track";
            wrap.putShort(Integer.valueOf(trackName.length()).shortValue());
            wrap.put(trackName.getBytes());
        }
        // Enabled - false
        wrap.put((byte) 0);
        // No keys
        wrap.putInt(0);
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        VisibilityTrack vt = new VisibilityTrack(oi);
        vt.initFromStream(StreamUtil.stream(wrap), (Scene) null);
        Assertions.assertEquals(vt.getName(), "Visibility Track");
        Assertions.assertEquals(oi, vt.getParent());
        Assertions.assertFalse(vt.isEnabled());
        Assertions.assertEquals(0, vt.getTimecourse().getValues().length);
    }

    @Test
    @DisplayName("Test Load Visibility Track From Stream With Keys")
    void testLoadVisibilityTrackFromStreamWithKeys() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Track Version
        wrap.putShort((short) 0);
        {
            String trackName = "Visibility Track";
            wrap.putShort(Integer.valueOf(trackName.length()).shortValue());
            wrap.put(trackName.getBytes());
        }
        // Enabled - false
        wrap.put((byte) 0);
        // 2 keys
        wrap.putInt(2);
        {
            // Key 1
            wrap.putDouble(0);
            wrap.put((byte) 0);
        }
        {
            // Key 2
            wrap.putDouble(1);
            wrap.put((byte) 1);
        }
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        VisibilityTrack vt = new VisibilityTrack(oi);
        vt.initFromStream(StreamUtil.stream(wrap), (Scene) null);
        Assertions.assertEquals(vt.getName(), "Visibility Track");
        Assertions.assertEquals(oi, vt.getParent());
        Assertions.assertFalse(vt.isEnabled());
        Timecourse tc = vt.getTimecourse();
        Assertions.assertEquals(2, tc.getValues().length);
        Assertions.assertEquals(2, tc.getTimes().length);
        Assertions.assertEquals(2, tc.getSmoothness().length);
    }

    @Test
    @DisplayName("Test Duplicate Visibility Track")
    void testDuplicateVisibilityTrack() {
        ObjectInfo oi = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Test null");
        VisibilityTrack vt = new VisibilityTrack(oi);
        Track dup = vt.duplicate(oi);
        Assertions.assertNotNull(dup);
        Assertions.assertTrue(dup instanceof VisibilityTrack);
        Assertions.assertNotEquals(vt, dup);
        Assertions.assertEquals(dup.getName(), "Visibility");
        Assertions.assertEquals(oi, dup.getParent());
        Assertions.assertTrue(dup.isEnabled());
        Assertions.assertEquals(vt.getTimecourse().getValues().length, dup.getTimecourse().getValues().length);
    }

    @Test
    @DisplayName("Test Copy Visibility Track")
    void testCopyVisibilityTrack() {
        ObjectInfo oi = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Test null");
        VisibilityTrack source = new VisibilityTrack(oi);
        source.setName("Source");
        source.setEnabled(false);
        source.setQuantized(false);
        source.getTimecourse().addTimepoint(new BooleanKeyframe(true), 5, new Smoothness());
        source.getTimecourse().addTimepoint(new BooleanKeyframe(false), 10, new Smoothness());
        VisibilityTrack target = new VisibilityTrack(oi);
        target.copy(source);
        Assertions.assertEquals(target.getName(), "Source");
        Assertions.assertFalse(target.isEnabled());
        Assertions.assertFalse(target.isQuantized());
        Assertions.assertEquals(source.getTimecourse().getValues().length, target.getTimecourse().getValues().length);
    }
}
