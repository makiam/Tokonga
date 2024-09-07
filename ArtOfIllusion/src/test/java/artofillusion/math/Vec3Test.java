/* Copyright (C) 2018 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.math;

import artofillusion.test.util.StreamUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author maksim.khramov
 */
@DisplayName("Vec 3 Test")
class Vec3Test {

    @Test
    @DisplayName("Test Vec 3 Constructor 0")
    void testVec3Constructor0() {
        Vec3 test = new Vec3();
        Assertions.assertEquals(0d, test.x, 0);
        Assertions.assertEquals(0d, test.y, 0);
        Assertions.assertEquals(0d, test.z, 0);
    }

    @Test
    @DisplayName("Test Vec 3 Constructor 1")
    void testVec3Constructor1() {
        Vec3 test = new Vec3(1.0, 2.0, 3.0);
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(2.0, test.y, 0);
        Assertions.assertEquals(3.0, test.z, 0);
    }

    @Test
    @DisplayName("Test Vec 3 Constructor 2")
    void testVec3Constructor2() {
        Vec3 source = new Vec3(1.0, 2.0, 3.0);
        Vec3 test = new Vec3(source);
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(2.0, test.y, 0);
        Assertions.assertEquals(3.0, test.z, 0);
    }

    @Test
    @DisplayName("Test Vec 3 Constructor 3")
    void testVec3Constructor3() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(24);
        wrap.putDouble(1.0);
        wrap.putDouble(2.0);
        wrap.putDouble(3.0);
        Vec3 test = new Vec3(StreamUtil.stream(wrap));
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(2.0, test.y, 0);
        Assertions.assertEquals(2.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Create X Vect 2")
    void testCreateXVect2() {
        Vec3 test = Vec3.vx();
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(0.0, test.y, 0);
        Assertions.assertEquals(0.0, test.z, 0);
    }

    @Test
    @DisplayName("Test Create Y Vect 2")
    void testCreateYVect2() {
        Vec3 test = Vec3.vy();
        Assertions.assertEquals(0.0, test.x, 0);
        Assertions.assertEquals(1.0, test.y, 0);
        Assertions.assertEquals(0.0, test.z, 0);
    }

    @Test
    @DisplayName("Test Create Z Vect 2")
    void testCreateZVect2() {
        Vec3 test = Vec3.vz();
        Assertions.assertEquals(0.0, test.x, 0);
        Assertions.assertEquals(0.0, test.y, 0);
        Assertions.assertEquals(1.0, test.z, 0);
    }

    @Test
    @DisplayName("Test Vector 3 set")
    void testVector3set() {
        Vec3 test = new Vec3();
        test.set(1.0, 2.0, 3.0);
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(2.0, test.y, 0);
        Assertions.assertEquals(3.0, test.z, 0);
    }
}
