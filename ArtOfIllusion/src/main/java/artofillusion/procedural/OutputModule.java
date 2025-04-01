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
import lombok.Setter;

import java.awt.*;

/* This is a Module which represents one of the output values of a procedure. */
final public class OutputModule extends ProceduralModule<OutputModule> {

    private Rectangle bounds;
    /* All output modules should be the same width. */
    @Setter
    int width;
    final double defaultValue;
    final RGBColor defaultColor;

    public OutputModule(String name, String defaultLabel, double defaultValue) {
        this(name, defaultLabel, defaultValue, new RGBColor(), IOPort.NUMBER);
    }

    public OutputModule(String name, String defaultLabel, RGBColor defaultColor) {
        this(name, defaultLabel, 0.0, defaultColor, IOPort.COLOR);
    }

    public OutputModule(String name, String defaultLabel, double defaultValue, RGBColor defaultColor, int type) {
        super(name, new IOPort[]{new IOPort(type, IOPort.INPUT, IOPort.LEFT, name, "(" + defaultLabel + ")")},
                new IOPort[]{}, new Point(0, 0));
        this.defaultValue = defaultValue;
        this.defaultColor = defaultColor;
    }

    @Override
    public void calcSize() {
        bounds.width = defaultMetrics.stringWidth(name) + IOPort.SIZE * 4;
        bounds.height = defaultMetrics.getMaxAscent() + defaultMetrics.getMaxDescent() + IOPort.SIZE * 4;

        if (width > 0) {
            bounds.width = width;
        }
    }

    public double getAverageValue() {
        return this.getAverageValue(0, 0);
    }
    
    /* Get the output value for this module. */
    @Override
    public double getAverageValue(int which, double blur) {
        if (linkFrom[0] == null) {
            return defaultValue;
        }
        return linkFrom[0].getAverageValue(linkFromIndex[0], blur);
    }

    public void getValueGradient(Vec3 grad) {
        this.getValueGradient(0, grad, 0);
    }
    
    /* Get the gradient of the output value for this module. */
    @Override
    public void getValueGradient(int which, Vec3 grad, double blur) {
        if (linkFrom[0] == null) {
            grad.set(0.0, 0.0, 0.0);
        } else {
            linkFrom[0].getValueGradient(linkFromIndex[0], grad, blur);
        }
    }

    public void getColor(RGBColor color) {
        this.getColor(0, color, 0);
    }
    /* Get the output color for this module. */
    @Override
    public void getColor(int which, RGBColor color, double blur) {
        if (linkFrom[0] == null) {
            color.copy(defaultColor);
        } else {
            linkFrom[0].getColor(linkFromIndex[0], color, blur);
        }
    }
}
