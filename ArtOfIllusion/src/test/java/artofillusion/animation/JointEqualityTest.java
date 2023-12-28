/* Copyright (C) 2016-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.math.CoordinateSystem;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author makiam
 *
 */
public class JointEqualityTest {

    @Test
    public void testJointEqualsByName() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");

        Assert.assertTrue(one.equals(two));

    }

    @Test
    public void testJointNotEqualsByName() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin2");

        Assert.assertFalse(one.equals(two));

    }

    @Test
    public void testJointNotEqualsByAngleOne() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.angle1.min = -90.0;
        Assert.assertFalse(one.equals(two));
    }

    @Test
    public void testJointNotEqualsByAngleTwo() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.angle2.min = -90.0;
        Assert.assertFalse(one.equals(two));
    }

    @Test
    public void testJointNotEqualsByTwist() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.twist.min = -90.0;
        Assert.assertFalse(one.equals(two));
    }

    @Test
    public void testJointNotEqualsByTwistNotFixed() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.twist.fixed = !two.twist.fixed;
        Assert.assertFalse(one.equals(two));
    }

    @Test
    public void testJointNotEqualsByTwistNotComfort() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.twist.comfort = !two.twist.comfort;
        Assert.assertFalse(one.equals(two));
    }

    @Test
    public void testJointNotEqualsByTwistNotLoop() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.twist.loop = !two.twist.loop;
        Assert.assertFalse(one.equals(two));
    }

    @Test
    public void testJointNotEqualsByLength() {
        CoordinateSystem cs = new CoordinateSystem();
        Joint one = new Joint(cs, null, "Origin1");
        Joint two = new Joint(cs, null, "Origin1");
        two.length.max = Double.MAX_VALUE;
        Assert.assertFalse(one.equals(two));
    }
}
