/* Copyright (C) 2016-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.preferences;

import org.junit.Assert;
import org.junit.Test;

public class DataMapParserDoubleTest {


    @Test
    public void testSpacesOnly() {
        String val = "          ";
        Assert.assertEquals(0, DataMap.parseDouble(val), 0);
    }

    @Test
    public void testZero() {
        String val = "0";
        testAndCompare(val);
    }
    @Test
    public void testManyZero0() {
        String val = "0000";
        testAndCompare(val);
    }

    @Test
    public void testManyZero1() {
        String val = "0000.000";
        testAndCompare(val);
    }

    @Test
    public void testOne() {
        String val = "1";
        testAndCompare(val);
    }

    @Test
    public void testMinusOne() {
        String val = "-1";
        testAndCompare(val);
    }

    @Test
    public void testPi() {
        String val = "3.14159";
        testAndCompare(val);
    }

    @Test
    public void testExponential1() {
        String val = "1234.56E2";
        testAndCompare(val);
    }

    @Test
    public void testExponential2() {
        String val = "1234.56e2";
        testAndCompare(val);
    }


    @Test
    public void testExponential3() {
        String val = "-1234.56E2";
        testAndCompare(val);
    }

    @Test
    public void testExponential4() {
        String val = "-1234.56e2";
        testAndCompare(val);
    }

    @Test
    public void testExponential5() {
        String val = "005.0000E-02";
        testAndCompare(val);
    }

    @Test
    public void testExponential6() {
        String val = "-005.0000e-02";
        testAndCompare(val);
    }

    private void testAndCompare(String val) {
        var produced = DataMap.parseDouble(val);
        var expected = Double.parseDouble(val);

        Assert.assertEquals(expected, produced, 0.000000001);

    }

    @Test(expected = NumberFormatException.class)
    public void testEmptyNative() {
        String val = "";
        var d2 = Double.parseDouble(val);
    }

    @Test
    public void testEmptyCustom() {
        String val = "";
        var d1 = DataMap.parseDouble(val);
        Assert.assertEquals(0, d1, 0);
    }
}
