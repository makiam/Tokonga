/* Copyright (C) 2018-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.animation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * @author MaksK
 */
@DisplayName("Timecourse Test")
class TimecourseTest {

    @Test
    @DisplayName("Test Create Timecourse")
    void testCreateTimecourse() {
        Timecourse tc = new Timecourse(new Keyframe[0], new double[0], new Smoothness[0]);
        Assertions.assertNotNull(tc);
        Assertions.assertEquals(0, tc.getTimes().length);
        Assertions.assertEquals(0, tc.getValues().length);
        Assertions.assertEquals(0, tc.getSmoothness().length);
    }

    @Test
    @DisplayName("Test Subdivide Timecourse When Not Enough Points to subdivide")
    void testSubdivideTimecourseNotEnoughLength() {
        Timecourse tc = new Timecourse(new Keyframe[0], new double[0], new Smoothness[0]);
        Timecourse subdiv = tc.subdivide(Timecourse.DISCONTINUOUS);
        Assertions.assertNotNull(tc);
        Assertions.assertEquals(tc, subdiv);
    }

    @Test
    @DisplayName("Test Timecourse Add And Replace Single Current Value")
    void testTimecourseAddAndReplaceSingleCurrentValue() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        var result = tc.addTimepoint(new BooleanKeyframe(false), 5, new Smoothness());
        Assertions.assertEquals(0, result);
        Assertions.assertEquals(1, tc.getValues().length);
        Assertions.assertEquals(1, tc.getTimes().length);
        Assertions.assertEquals(1, tc.getSmoothness().length);
        Assertions.assertEquals(5, tc.getTimes()[0], 0);
    }

    @Test
    @DisplayName("Test Timecourse Add As New Value")
    void testTimecourseAddAsNewValueAfterCurrent() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        var result = tc.addTimepoint(new BooleanKeyframe(false), 10, new Smoothness());
        Assertions.assertEquals(2, tc.getValues().length);
        Assertions.assertEquals(2, tc.getTimes().length);
        Assertions.assertEquals(2, tc.getSmoothness().length);
        Assertions.assertEquals(5, tc.getTimes()[0], 0);
        Assertions.assertEquals(10, tc.getTimes()[1], 0);
        Assertions.assertEquals(1, result);
    }

    @Test
    @DisplayName("Test Timecourse Add As New Value")
    void testTimecourseAddAsNewValueBeforeCurrent() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        var result = tc.addTimepoint(new BooleanKeyframe(false), 3, new Smoothness());
        Assertions.assertEquals(2, tc.getValues().length);
        Assertions.assertEquals(2, tc.getTimes().length);
        Assertions.assertEquals(2, tc.getSmoothness().length);
        Assertions.assertEquals(3, tc.getTimes()[0], 0);
        Assertions.assertEquals(5, tc.getTimes()[1], 0);
        Assertions.assertEquals(0, result);
    }

    @Test
    @DisplayName("Test Timecourse Add And Remove Single Current Value By Given Time")
    void testTimecourseAddAndRemoveSingleCurrentValueByTime() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        tc.removeTimepoint(5.0);

        Assertions.assertEquals(0, tc.getValues().length);
        Assertions.assertEquals(0, tc.getTimes().length);
        Assertions.assertEquals(0, tc.getSmoothness().length);

    }

    @Test
    @DisplayName("Test Timecourse Add And Remove Single Value By Given Not Exist Time")
    void testTimecourseAddAndRemoveSingleCurrentValueByNotExistTime() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        tc.removeTimepoint(15.0);

        Assertions.assertEquals(1, tc.getValues().length);
        Assertions.assertEquals(1, tc.getTimes().length);
        Assertions.assertEquals(1, tc.getSmoothness().length);

    }

    @Test
    @DisplayName("Test Timecourse Add And Remove Single Value By Given Not Exist Time")
    void testTimecourseAddAndRemoveCurrentValueByNotExistTime() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        tc.addTimepoint(new BooleanKeyframe(false), 10.0, new Smoothness());

        tc.removeTimepoint(15.0);

        Assertions.assertEquals(2, tc.getValues().length);
        Assertions.assertEquals(2, tc.getTimes().length);
        Assertions.assertEquals(2, tc.getSmoothness().length);

    }

    @Test
    @DisplayName("Test Timecourse Remove Single Value By Index")
    void testTimecourseRemoveSingleCurrentValueByIndex() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        tc.removeTimepoint(0);

        Assertions.assertEquals(0, tc.getValues().length);
        Assertions.assertEquals(0, tc.getTimes().length);
        Assertions.assertEquals(0, tc.getSmoothness().length);

    }

    @Test
    @DisplayName("Test Timecourse Add and Remove First Value By Index")
    void testTimecourseAddAndRemoveFirstValueByIndex() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        tc.addTimepoint(new BooleanKeyframe(false), 10.0, new Smoothness());
        tc.removeTimepoint(0);

        Assertions.assertEquals(1, tc.getValues().length);
        Assertions.assertEquals(1, tc.getTimes().length);
        Assertions.assertEquals(1, tc.getSmoothness().length);

        Assertions.assertEquals(10, tc.getTimes()[0], 0);
    }

    @Test
    @DisplayName("Test Timecourse Add and Remove Second Value By Index")
    void testTimecourseAddAndRemoveSecondValueByIndex() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        tc.addTimepoint(new BooleanKeyframe(false), 10.0, new Smoothness());
        tc.removeTimepoint(1);

        Assertions.assertEquals(1, tc.getValues().length);
        Assertions.assertEquals(1, tc.getTimes().length);
        Assertions.assertEquals(1, tc.getSmoothness().length);

        Assertions.assertEquals(5, tc.getTimes()[0], 0);
    }

    @Test
    @DisplayName("Test Clear All From Timecourse")
    void testClearAllFromTimecourse() {
        Timecourse tc = new Timecourse(new BooleanKeyframe[]{new BooleanKeyframe(true)}, new double[]{5}, new Smoothness[]{new Smoothness()});
        tc.addTimepoint(new BooleanKeyframe(false), 10, new Smoothness());
        tc.removeAllTimepoints();
        Assertions.assertEquals(0, tc.getValues().length);
        Assertions.assertEquals(0, tc.getTimes().length);
        Assertions.assertEquals(0, tc.getSmoothness().length);
    }

    @Test
    @DisplayName("Test Duplicate Empty Timecourse")
    void testDuplicateEmptyTimecourse() {
        Timecourse tc = new Timecourse(new Keyframe[0], new double[0], new Smoothness[0]);
        tc.setSubdivideAdaptively(false);
        Timecourse dup = tc.duplicate(null);
        Assertions.assertNotNull(dup);
        Assertions.assertNotEquals(dup, tc);
        Assertions.assertFalse(dup.getSubdivideAdaptively());
        Assertions.assertEquals(0, dup.getTimes().length);
        Assertions.assertEquals(0, dup.getValues().length);
        Assertions.assertEquals(0, dup.getSmoothness().length);
    }

    @Test
    @DisplayName("Test Duplicate Timecourse")
    void testDuplicateTimecourse() {
        Timecourse tc = new Timecourse(new Keyframe[0], new double[0], new Smoothness[0]);
        tc.addTimepoint(new BooleanKeyframe(true), 5, new Smoothness());
        tc.addTimepoint(new BooleanKeyframe(false), 10, new Smoothness());
        tc.setSubdivideAdaptively(false);
        Timecourse dup = tc.duplicate(null);
        Assertions.assertNotNull(dup);
        Assertions.assertNotEquals(dup, tc);
        Assertions.assertFalse(dup.getSubdivideAdaptively());
        Assertions.assertEquals(2, dup.getTimes().length);
        Assertions.assertEquals(2, dup.getValues().length);
        Assertions.assertEquals(2, dup.getSmoothness().length);
    }
}
