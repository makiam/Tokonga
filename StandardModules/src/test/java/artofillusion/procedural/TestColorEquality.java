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

public class TestColorEquality {
    @Test
    void testNoGivenInputs() {
        var module = new ColorEqualityModule(new Point(0,0));

        Assertions.assertEquals(0.0, module.getAverageValue(0, 0.0), 0.0);
    }

    @Test
    void testAttachOnlyOne() {
        var module = new ColorEqualityModule(new Point(0,0));

        Assertions.assertEquals(0.0, module.getAverageValue(0, 0.0), 0.0);
    }
}
