/* Copyright 2025-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.PluginRegistry;
import artofillusion.math.RGBColor;

import java.awt.*;

@ProceduralModule.Category("Modules:menu.colorFunctions")
public class ColorEqualityModule extends ProceduralModule<ColorEqualityModule> {

    @PluginRegistry.UsedViaReflection
    public ColorEqualityModule() {
        this(new Point());
    }

    public ColorEqualityModule(Point position) {
        super("Color Equality",
                new IOPort[]{new ColorInputPort(IOPort.LEFT, "Color 1", "(White)"), new ColorInputPort(IOPort.RIGHT, "Color 2", "(Black)")},
                new IOPort[]{new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "True/False")},
                position);
    }

    @Override
    public double getAverageValue(int which, double blur) {
        var colorOne = new RGBColor(1.0,1.0,1.0);
        var colorTwo = new RGBColor(0.0,0.0,0.0);
        if(linkFrom[0] != null) linkFrom[0].getColor(0, colorOne, 0.0);
        if(linkFrom[1] != null) linkFrom[1].getColor(1, colorTwo, 0.0);
        return colorOne == colorTwo ? 1.0 : 0.0;
    }
}