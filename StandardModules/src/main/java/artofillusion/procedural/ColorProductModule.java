/* Copyright (C) 2000 by Peter Eastman
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.math.*;
import artofillusion.ui.*;
import java.awt.*;

/**
 * This is a Module which outputs the product of two colors.
 */
@ProceduralModule.Category("Modules:menu.colorFunctions")
public class ColorProductModule extends ProceduralModule<ColorProductModule> {

    final RGBColor color;
    boolean colorOk;
    double lastBlur;

    public ColorProductModule() {
        this(new Point());
    }

    public ColorProductModule(Point position) {
        super("\u00D7", new IOPort[]{new ColorInputPort(IOPort.TOP, "Color 1", '(' + Translate.text("white") + ')'),
            new ColorInputPort(IOPort.BOTTOM, "Color 2", '(' + Translate.text("white") + ')')},
                new IOPort[]{new IOPort(IOPort.COLOR, IOPort.OUTPUT, IOPort.RIGHT, "Product")},
                position);
        color = new RGBColor(0.0f, 0.0f, 0.0f);
    }

    /* New point, so the color will need to be recalculated. */
    @Override
    public void init(PointInfo p) {
        colorOk = false;
    }

    /* Calculate the product color. */
    @Override
    public void getColor(int which, RGBColor c, double blur) {
        if (colorOk && blur == lastBlur) {
            c.copy(color);
            return;
        }
        colorOk = true;
        lastBlur = blur;
        if (linkFrom[0] == null) {
            color.setRGB(1.0f, 1.0f, 1.0f);
        } else {
            linkFrom[0].getColor(linkFromIndex[0], color, blur);
        }
        if (linkFrom[1] == null) {
            c.setRGB(1.0f, 1.0f, 1.0f);
        } else {
            linkFrom[1].getColor(linkFromIndex[1], c, blur);
        }
        color.multiply(c);
        c.copy(color);
    }
}
