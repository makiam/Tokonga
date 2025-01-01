/* Copyright (C) 2018 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.animation;

import artofillusion.LayoutWindow;
import artofillusion.Scene;
import artofillusion.test.util.StreamUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author maksim.khramov
 */
@DisplayName("Weight Track Test")
class WeightTrackTest {

    private static DummyTrack parent;

    @BeforeAll
    static void setUpClass() {
        parent = new DummyTrack();
    }

    @Test
    @DisplayName("Test Create Weight Track")
    void testCreateWeightTrack() {
        WeightTrack weight = new WeightTrack(parent);
        Assertions.assertNotNull(weight);
        Assertions.assertEquals("Weight", weight.getName());
        Assertions.assertEquals(parent, weight.parent);
        Assertions.assertTrue(weight.isEnabled());
    }

    @Test
    @DisplayName("Test Duplicate Weight Track")
    void testDuplicateWeightTrack() {
        WeightTrack weight = new WeightTrack(parent);
        WeightTrack dup = weight.duplicate(parent);
        Assertions.assertNotNull(dup);

        Assertions.assertNotEquals(weight, dup);
        Assertions.assertEquals("Weight", dup.getName());
        Assertions.assertEquals(parent, dup.parent);
        Assertions.assertTrue(weight.isEnabled());
    }

    @Test
    @DisplayName("Test Copy Weight Track")
    void testCopyWeightTrack() {
        WeightTrack weight = new WeightTrack(parent);
        weight.setSmoothingMethod(Timecourse.DISCONTINUOUS);
        DummyTrack dupParent = new DummyTrack();
        WeightTrack dup = new WeightTrack(dupParent);
        dup.copy(weight);

        Assertions.assertNotEquals(weight, dup);
        Assertions.assertEquals("Weight", dup.getName());
        Assertions.assertEquals(dupParent, dup.parent);
        Assertions.assertTrue(weight.isEnabled());
        Assertions.assertEquals(dup.getSmoothingMethod(), weight.getSmoothingMethod());
    }

    @Test
    @DisplayName("Test Get Weight For Disabled Track")
    void testGetWeightForDisabledTrack() {
        WeightTrack weight = new WeightTrack(parent);
        weight.setEnabled(false);
        Assertions.assertEquals(1.0, weight.getWeight(0), 0);
    }

    @Test
    @DisplayName("Test Load From Stream Track Bad Version")
    void testLoadFromStreamTrackBadVersion() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(12);
            // Track Version
            wrap.putShort((short) 1);
            WeightTrack track = new WeightTrack(parent);
            track.initFromStream(StreamUtil.stream(wrap), null);
        });
    }

    @Test
    @DisplayName("Test Load From Stream Track")
    void testLoadFromStreamTrack() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(120);
        // Track Version
        wrap.putShort((short) 0);
        String trackName = "Weight";
        wrap.putShort(Integer.valueOf(trackName.length()).shortValue());
        wrap.put(trackName.getBytes());
        // Is Enabled
        wrap.put((byte) 1);
        wrap.putInt(Timecourse.LINEAR);
        // KeysCount
        wrap.putInt(1);
        {
            // Time;
            wrap.putDouble(0);
            // Scalar Keyframe data;
            wrap.putDouble(1);
            // Smoothness data
            {
                wrap.putDouble(0);
                wrap.putDouble(1);
            }
        }
        WeightTrack track = new WeightTrack(parent);
        track.initFromStream(StreamUtil.stream(wrap), null);
        Assertions.assertTrue(track.isEnabled());
        Assertions.assertEquals(Timecourse.LINEAR, track.getSmoothingMethod());
        Assertions.assertEquals(1, track.getKeyTimes().length);
    }

    @DisplayName("Dummy Track")
    static class DummyTrack extends Track<DummyTrack> {

        @Override
        public void edit(LayoutWindow win) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void apply(double time) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public DummyTrack duplicate(Object parent) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void copy(DummyTrack tr) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double[] getKeyTimes() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int moveKeyframe(int which, double time) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void deleteKeyframe(int which) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isNullTrack() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeToStream(DataOutputStream out, Scene scene) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void initFromStream(DataInputStream in, Scene scene) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
