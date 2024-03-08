/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.ObjectInfo;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ScoreTest extends TestCase {

    @Test
    public void testScoreSelectionFilterAllGood() {
        Object[] input = new Object[] {new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test"), new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test"), new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test")};

        Assert.assertEquals(3, Score.filterTargets(input).size());
    }

    public void testScoreSelectionFilterAllBad() {
        Object[] input = new Object[] {"String", "String", "String"};
        Assert.assertEquals(0, Score.filterTargets(input).size());
    }

    @Test
    public void testScoreSelectionFilterOneGood() {
        Object[] input = new Object[] {1, new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test"), "String", "String", "String"};
        Assert.assertEquals(1, Score.filterTargets(input).size());
    }
}