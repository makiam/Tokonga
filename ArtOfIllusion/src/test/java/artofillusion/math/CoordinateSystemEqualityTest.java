/* Copyright (C) 2016-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author makiam
 */
@DisplayName("Coordinate System Equality Test")
class CoordinateSystemEqualityTest {

    public CoordinateSystemEqualityTest() {
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Test CS Are Equals")
    void testCSAreEquals() {
        CoordinateSystem cs1 = new CoordinateSystem();
        CoordinateSystem cs2 = new CoordinateSystem();
        Assertions.assertTrue(cs1.equals(cs2));
    }

    @Test
    @DisplayName("Test Custom CS Are Equals")
    void testCustomCSAreEquals() {
        Vec3 vector = new Vec3();
        CoordinateSystem cs1 = new CoordinateSystem(vector, vector, vector);
        CoordinateSystem cs2 = new CoordinateSystem(vector, vector, vector);
        Assertions.assertTrue(cs1.equals(cs2));
    }

    @Test
    @DisplayName("Test Custom CS Are Equals 2")
    void testCustomCSAreEquals2() {
        Vec3 vector = new Vec3(1.0, 1.0, 1.0);
        CoordinateSystem cs1 = new CoordinateSystem(vector, vector, vector);
        CoordinateSystem cs2 = new CoordinateSystem(vector, vector, vector);
        Assertions.assertTrue(cs1.equals(cs2));
    }

    @Test
    @DisplayName("Test CS Not Equals By Orig")
    void testCSNotEqualsByOrig() {
        CoordinateSystem cs1 = new CoordinateSystem();
        CoordinateSystem cs2 = new CoordinateSystem();
        cs2.orig.x += 1.0;
        Assertions.assertFalse(cs1.equals(cs2));
    }

    @Test
    @DisplayName("Test CS Not Equals By Z Dir")
    void testCSNotEqualsByZDir() {
        CoordinateSystem cs1 = new CoordinateSystem();
        CoordinateSystem cs2 = new CoordinateSystem();
        cs2.zdir.x += 1.0;
        Assertions.assertFalse(cs1.equals(cs2));
    }

    @Test
    @DisplayName("Test CS Not Equals By Up Dir")
    void testCSNotEqualsByUpDir() {
        CoordinateSystem cs1 = new CoordinateSystem();
        CoordinateSystem cs2 = new CoordinateSystem();
        cs2.updir.x += 1.0;
        Assertions.assertFalse(cs1.equals(cs2));
    }
}
