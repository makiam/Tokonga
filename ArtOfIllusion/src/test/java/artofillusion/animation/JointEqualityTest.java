/* Copyright (C) 2016-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.animation;

import artofillusion.math.CoordinateSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author makiam
 */
@DisplayName("Joint Equality Test")
class JointEqualityTest {

    @Test
    @DisplayName("Test Joint Equals By Name")
    void testJointEqualsByName() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        Assertions.assertTrue(one.equals(two));
    }

    @Test
    @DisplayName("Test Joint Not Equals By Name")
    void testJointNotEqualsByName() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin2");
        Assertions.assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Test Joint Not Equals By Angle One")
    void testJointNotEqualsByAngleOne() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.angle1.min = -90.0;
        Assertions.assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Test Joint Not Equals By Angle Two")
    void testJointNotEqualsByAngleTwo() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.angle2.min = -90.0;
        Assertions.assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Test Joint Not Equals By Twist")
    void testJointNotEqualsByTwist() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.twist.min = -90.0;
        Assertions.assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Test Joint Not Equals By Twist Not Fixed")
    void testJointNotEqualsByTwistNotFixed() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.twist.fixed = !two.twist.fixed;
        Assertions.assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Test Joint Not Equals By Twist Not Comfort")
    void testJointNotEqualsByTwistNotComfort() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.twist.comfort = !two.twist.comfort;
        Assertions.assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Test Joint Not Equals By Twist Not Loop")
    void testJointNotEqualsByTwistNotLoop() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.twist.loop = !two.twist.loop;
        Assertions.assertFalse(one.equals(two));
    }

    @Test
    @DisplayName("Test Joint Not Equals By Length")
    void testJointNotEqualsByLength() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.length.max = Double.MAX_VALUE;
        Assertions.assertFalse(one.equals(two));
    }
}
