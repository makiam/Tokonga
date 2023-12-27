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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author makiam
 */
@DisplayName("Joint DOF Equality Test")
class JointDOFEqualityTest {

    private Joint source;

    public JointDOFEqualityTest() {
    }

    @BeforeEach
    void setUp() {
        CoordinateSystem cs = new CoordinateSystem();
        source = new Joint(cs, null, "Origin1");
    }

    @Test
    @DisplayName("Test DOF Are Equals")
    void testDOFAreEquals() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        Assertions.assertTrue(sourceDof.equals(targetDof));

    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Fixed")
    void testDOFAreNotEqualsByFixed() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.fixed = !targetDof.fixed;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Comfort")
    void testDOFAreNotEqualsByComfort() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.comfort = !targetDof.comfort;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Loop")
    void testDOFAreNotEqualsByLoop() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.loop = !targetDof.loop;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Min")
    void testDOFAreNotEqualsByMin() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.min = targetDof.min - 1.0;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Min Comfort")
    void testDOFAreNotEqualsByMinComfort() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.minComfort = targetDof.minComfort - 1.0;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Max")
    void testDOFAreNotEqualsByMax() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.max = targetDof.max + 1.0;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Max Comfort")
    void testDOFAreNotEqualsByMaxComfort() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.maxComfort = targetDof.maxComfort + 1.0;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Stiffness")
    void testDOFAreNotEqualsByStiffness() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.stiffness = targetDof.stiffness + 1.0;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }

    @Test
    @DisplayName("Test DOF Are Not Equals By Pos")
    void testDOFAreNotEqualsByPos() {
        Joint.DOF sourceDof = source.angle1;
        Joint.DOF targetDof = sourceDof.duplicate();
        targetDof.pos = targetDof.pos + 1.0;
        Assertions.assertFalse(sourceDof.equals(targetDof));
    }
}
