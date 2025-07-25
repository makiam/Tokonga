/* Copyright (C) 2001 by David M. Turner <novalis@novalis.org> and Peter Eastman
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
 *  This is a Module which outputs the lighter of two colors.
 */
@ProceduralModule.Category("Modules:menu.colorFunctions")
public class ColorLightenModule extends ProceduralModule<ColorLightenModule> {

    final RGBColor color;
    boolean colorOk;
    double lastBlur;

    public ColorLightenModule() {
        this(new Point());
    }

    public ColorLightenModule(Point position) {
        super(Translate.text("Modules:menu.lighterModule"), new IOPort[]{new ColorInputPort(IOPort.TOP, "Color 1", '(' + Translate.text("white") + ')'),
            new ColorInputPort(IOPort.BOTTOM, "Color 2", '(' + Translate.text("white") + ')')},
                new IOPort[]{new IOPort(IOPort.COLOR, IOPort.OUTPUT, IOPort.RIGHT, "Lighter")},
                position);
        color = new RGBColor(0.0f, 0.0f, 0.0f);
    }

    /* New point, so the color will need to be recalculated. */
    @Override
    public void init(PointInfo p) {
        colorOk = false;
    }

    /* Calculate the lighter color. */
    @Override
    public void getColor(int which, RGBColor c, double blur) {
        if (colorOk && blur == lastBlur) {
            c.copy(color);
            return;
        }
        float brightness1;
        colorOk = true;
        lastBlur = blur;
        if (linkFrom[0] == null) {
            color.setRGB(1.0f, 1.0f, 1.0f);
            brightness1 = 1.0f;
        } else {
            linkFrom[0].getColor(linkFromIndex[0], color, blur);
            brightness1 = color.getBrightness();
        }

        float brightness2;
        if (linkFrom[1] == null) {
            c.setRGB(1.0f, 1.0f, 1.0f);
            brightness2 = 1.0f;
        } else {
            linkFrom[1].getColor(linkFromIndex[1], c, blur);
            brightness2 = c.getBrightness();
        }
        if (brightness1 > brightness2) {
            c.copy(color);
        } else {
            color.copy(c);
        }
    }
}
