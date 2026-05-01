/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.awt.Point;

public class RGBToHSVModuleTest {
    @Test
    void testRGBToHSVNoGivenInputs() {
        var module = new RGBToHSVModule(new Point(0, 0));
        Assertions.assertEquals(0.0, module.getAverageValue(0, 0.0), 0.0);
        Assertions.assertEquals(0.0, module.getAverageValue(1, 0.0), 0.0);
        Assertions.assertEquals(0.0, module.getAverageValue(2, 0.0), 0.0);
    }

    @Test
    void testRGBWhite() {
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 1.0, 0));
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 1.0, 1));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 1.0, 2));
    }

    @Test
    void testRGBRed() {
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 0.0, 0));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 0.0, 1));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 0.0, 2));
    }

    @Test
    void testRGBGreen() {
        Assertions.assertEquals(0.3333333333333333, RGBToHSVModule.rgbToHsv(0.0, 1.0, 0.0, 0));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 1.0, 0.0, 1));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 1.0, 0.0, 2));
    }

    @Test
    void testRGBBlue() {
        Assertions.assertEquals(0.6666666666666666, RGBToHSVModule.rgbToHsv(0.0, 0.0, 1.0, 0));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 0.0, 1.0, 1));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 0.0, 1.0, 2));
    }

    @Test
    void testRGBYellow() {
        Assertions.assertEquals(0.16666666666666666, RGBToHSVModule.rgbToHsv(1.0, 1.0, 0.0, 0));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 0.0, 1));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 0.0, 2));
    }

    @Test
    void testRGBMagenta() {
        Assertions.assertEquals(0.8333333333333334, RGBToHSVModule.rgbToHsv(1.0, 0.0, 1.0, 0));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 1.0, 1));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 1.0, 2));
    }

    @Test
    void testRGBPurple() {
        Assertions.assertEquals(0.8333333333333334, RGBToHSVModule.rgbToHsv(0.5, 0.0, 0.5, 0));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.5, 0.0, 0.5, 1));
        Assertions.assertEquals(0.5, RGBToHSVModule.rgbToHsv(0.5, 0.0, 0.5, 2));
    }


    @Test
    void testRGBCyan() {
        Assertions.assertEquals(0.5, RGBToHSVModule.rgbToHsv(0.0, 1.0, 1.0, 0));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 1.0, 1.0, 1));
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 1.0, 1.0, 2));
    }

    @Test
    void testRGBGray() {
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(0.5, 0.5, 0.5, 0));
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(0.5, 0.5, 0.5, 1));
        Assertions.assertEquals(0.5, RGBToHSVModule.rgbToHsv(0.5, 0.5, 0.5, 2));
    }
}
