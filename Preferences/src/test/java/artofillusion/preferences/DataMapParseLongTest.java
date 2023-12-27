/* Copyright (C) 2016-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.preferences;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataMapParseLongTest {

    @Test
    public void testNullPassed() {
        String val = null;
        Assertions.assertEquals(0L, DataMap.parseLong(val));
    }

    @Test
    public void testEmptyPassed() {
        String val = null;
        Assertions.assertEquals(0L, DataMap.parseLong(val));
    }

    @Test
    public void testSpacesOnly() {
        String val = "          ";
        Assertions.assertEquals(0L, DataMap.parseLong(val));
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
    public void testBig() {
        String val = "1234567890";
        testAndCompare(val);
    }

    @Test
    public void testMinusBig() {
        String val = "-1234567890";
        testAndCompare(val);
    }

    @Test
    public void testMaxLong() {

        String val = "9223372036854775807";
        testAndCompare(val);
    }

    @Test
    public void testMaxLongPlus() {

        String val = "9223372036854775808";
        testAndCompare(val);
    }

    @Test
    public void testMinLong() {

        String val = "-9223372036854775807";
        testAndCompare(val);
    }

    @Test
    public void testMinLongMinus() {

        String val = "-9223372036854775809";
        testAndCompare(val);
    }

    private void testAndCompare(String val) {
        long produced = DataMap.parseLong(val);
        long expected = Long.parseLong(val);

        Assertions.assertEquals(expected, produced);

    }
}
