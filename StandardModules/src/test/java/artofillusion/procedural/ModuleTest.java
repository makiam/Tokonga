/* Copyright (C) 2020 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Point;


/**
 *
 * @author MaksK
 */
public class ModuleTest {

    @Test
    public void testModuleDuplicate() {
        var source = new ColorScaleModule(new Point(128, 64));
        var target = source.duplicate();

        Assertions.assertTrue(target instanceof ColorScaleModule);
        Assertions.assertNotNull(target);
        Assertions.assertEquals(128, target.getBounds().x);
        Assertions.assertEquals(64, target.getBounds().y);

    }
}
