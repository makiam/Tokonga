/* Copyright (C) 2006 by Peter Eastman
   Changes copyright (C) 2017-2024 by Maksim Khramov

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

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("Rgb Color Test")
class RGBColorTest {

    /**
     * Test converting to and from ERGB format.
     */
    @Test
    @DisplayName("Test Load RGB Color From Stream")
    void testLoadRGBColorFromStream() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(12);
        wrap.putFloat(0);
        wrap.putFloat(0.5f);
        wrap.putFloat(1.0f);
        RGBColor color = new RGBColor(StreamUtil.stream(wrap));
        Assertions.assertEquals(0, color.red, 0);
        Assertions.assertEquals(0.5, color.green, 0);
        Assertions.assertEquals(1, color.blue, 0);
    }

    @Test
    @DisplayName("Test ERGB")
    void testERGB() {
        RGBColor c1 = new RGBColor(), c2 = new RGBColor();
        c1.setRGB(0.0f, 0.0f, 0.0f);
        c2.setERGB(c1.getERGB());
        assertColorsEquals(c1, c2, 0.0f);
        c1.setRGB(1.0f, 1.0f, 1.0f);
        c2.setERGB(c1.getERGB());
        assertColorsEquals(c1, c2, 0.0f);
        for (int i = 0; i < 1000; i++) {
            c1.setRGB(Math.random(), Math.random(), Math.random());
            c1.scale(10.0 * Math.random());
            c2.setERGB(c1.getERGB());
            assertColorsEquals(c1, c2, c1.getMaxComponent() / 128);
        }
    }

    private void assertColorsEquals(RGBColor c1, RGBColor c2, float tol) {
        Assertions.assertEquals(c1.getRed(), c2.getRed(), tol);
        Assertions.assertEquals(c1.getGreen(), c2.getGreen(), tol);
        Assertions.assertEquals(c1.getBlue(), c2.getBlue(), tol);
    }
}
