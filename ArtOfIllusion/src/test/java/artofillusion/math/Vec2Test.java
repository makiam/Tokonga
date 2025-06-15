/* Copyright (C) 2018-2025 by Maksim Khramov

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

/**
 * @author maksim.khramov
 */
@DisplayName("Vec 2 Test")
class Vec2Test {

    @Test
    @DisplayName("Test Vec 2 Constructor 0")
    void testVec2Constructor0() {
        Vec2 test = new Vec2();
        Assertions.assertEquals(0d, test.x, 0);
        Assertions.assertEquals(0d, test.y, 0);
    }

    @Test
    @DisplayName("Test Vec 2 Constructor 1")
    void testVec2Constructor1() {
        Vec2 test = new Vec2(1.0, 1.0);
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(1.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vec 2 Constructor 2")
    void testVec2Constructor2() {
        Vec2 source = new Vec2(1.0, 1.0);
        Vec2 test = new Vec2(source);
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(1.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vec 2 Constructor 3")
    void testVec2Constructor3() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(16);
        wrap.putDouble(1.0);
        wrap.putDouble(2.0);
        Vec2 test = new Vec2(StreamUtil.stream(wrap));
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(2.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Create X Vect 2")
    void testCreateXVect2() {
        Vec2 test = Vec2.vx();
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(0.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Create Y Vect 2")
    void testCreateYVect2() {
        Vec2 test = Vec2.vy();
        Assertions.assertEquals(0.0, test.x, 0);
        Assertions.assertEquals(1.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vector 2 set")
    void testVector2set() {
        Vec2 test = new Vec2();
        test.set(1.0, 2.0);
        Assertions.assertEquals(1.0, test.x, 0);
        Assertions.assertEquals(2.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vector 2 times 0")
    void testVector2times0() {
        Vec2 test = new Vec2();
        test = test.times(2.0);
        Assertions.assertEquals(0.0, test.x, 0);
        Assertions.assertEquals(0.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vector 2 scale 0")
    void testVector2scale0() {
        Vec2 test = new Vec2();
        test.scale(2.0);
        Assertions.assertEquals(0.0, test.x, 0);
        Assertions.assertEquals(0.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vector 2 times 1")
    void testVector2times1() {
        Vec2 test = new Vec2();
        test.set(1.0, 2.0);
        test = test.times(0);
        Assertions.assertEquals(0.0, test.x, 0);
        Assertions.assertEquals(0.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vector 2 scale 1")
    void testVector2scale1() {
        Vec2 test = new Vec2();
        test.set(1.0, 2.0);
        test.scale(0);
        Assertions.assertEquals(0.0, test.x, 0);
        Assertions.assertEquals(0.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vector 2 times 2")
    void testVector2times2() {
        Vec2 test = new Vec2();
        test.set(1.0, 2.0);
        test = test.times(2);
        Assertions.assertEquals(2.0, test.x, 0);
        Assertions.assertEquals(4.0, test.y, 0);
    }

    @Test
    @DisplayName("Test Vector 2 scale 2")
    void testVector2scale2() {
        Vec2 test = new Vec2();
        test.set(1.0, 2.0);
        test.scale(2);
        Assertions.assertEquals(2.0, test.x, 0);
        Assertions.assertEquals(4.0, test.y, 0);
    }
}
