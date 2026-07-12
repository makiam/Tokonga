/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion;

import artofillusion.math.RGBColor;
import artofillusion.ui.Translate;

public final class ColorProperty extends Property {
    /**
     * Create an RGBColor valued property.
     *
     * @param name
     * @param value
     */
    public ColorProperty(String name, RGBColor value) {
        super(Translate.text(name), value);
    }

    /**
     * Create an RGBColor valued property.
     *
     * @param name

     */
    public ColorProperty(String name) {
        this(name, new RGBColor(1.0, 1.0, 1.0));
    }
}
