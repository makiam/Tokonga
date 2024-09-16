/*
 *  Copyright 2024 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.spmanager;

import org.junit.jupiter.api.*;

class ManagersUtilsDoubleParserTest {

    @Test
    void testSpacesOnly() {
        String val = "          ";
        Assertions.assertEquals(0, SPManagerUtils.parseDouble(val), 0);
    }

    @Test
    void testZero() {
        String val = "0";
        testAndCompare(val);
    }
    @Test
    void testManyZero0() {
        String val = "0000";
        testAndCompare(val);
    }

    @Test
    void testManyZero1() {
        String val = "0000.000";
        testAndCompare(val);
    }

    @Test
    void testOne() {
        String val = "1";
        testAndCompare(val);
    }

    @Test
    void testMinusOne() {
        String val = "-1";
        testAndCompare(val);
    }

    @Test
    void testPi() {
        String val = "3.14159";
        testAndCompare(val);
    }

    @Test
    void testExponential1() {
        String val = "1234.56E2";
        testAndCompare(val);
    }

    @Test
    void testExponential2() {
        String val = "1234.56e2";
        testAndCompare(val);
    }


    @Test
    void testExponential3() {
        String val = "-1234.56E2";
        testAndCompare(val);
    }

    @Test
    void testExponential4() {
        String val = "-1234.56e2";
        testAndCompare(val);
    }

    @Test
    void testExponential5() {
        String val = "005.0000E-02";
        testAndCompare(val);
    }

    @Test
    void testExponential6() {
        String val = "-005.0000e-02";
        testAndCompare(val);
    }



    private void testAndCompare(String val) {
        var produced = SPManagerUtils.parseDouble(val);
        var expected = Double.parseDouble(val);

        Assertions.assertEquals(expected, produced, 0.000000001);

    }


    @Test
    void testEmptyNative() {
        Assertions.assertThrows(NumberFormatException.class, () -> Double.parseDouble(""));
    }

    @Test
    void testEmptyCustom() {
        String val = "";
        var d1 = SPManagerUtils.parseDouble(val);
        Assertions.assertEquals(0, d1, 0);
    }


    @Test
    void testParseVersion() {

        Assertions.assertEquals(1002, SPManagerUtils.parseVersion("1.2"));
        Assertions.assertEquals(1020, SPManagerUtils.parseVersion("1.20"));
        Assertions.assertEquals(1020003, SPManagerUtils.parseVersion("1.20.3"));


    }
}
