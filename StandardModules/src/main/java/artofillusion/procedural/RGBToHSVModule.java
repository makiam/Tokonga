/* Copyright 2024-20256 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import java.awt.Point;

@ProceduralModule.Category("Modules:menu.colorFunctions")
public class RGBToHSVModule extends ProceduralModule<RGBToHSVModule> {

    public RGBToHSVModule() {
        this(new Point());
    }

    public RGBToHSVModule(Point position) {
        super("RGB to HSV",
            new IOPort[]{
                new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, "Red", "(0)"),
                new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, "Green", "(0)"),
                new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, "Blue", "(0)")
            },
            new IOPort[]{
                new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Hue"),
                new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Saturation"),
                new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Value")
            },
            position);
    }

    @Override
    public double getAverageValue(int which, double blur) {
        double red = (this.linkFrom[0] != null) ? this.linkFrom[0].getAverageValue(this.linkFromIndex[0], blur) : 0.0;
        double green = (this.linkFrom[1] != null) ? this.linkFrom[1].getAverageValue(this.linkFromIndex[1], blur) : 0.0;
        double blue = (this.linkFrom[2] != null) ? this.linkFrom[2].getAverageValue(this.linkFromIndex[2], blur) : 0.0;
        return rgbToHsv(red, green, blue, which);
    }

    private static HSVColor rgbToHsv(double red, double green, double blue) {
        double min = Math.min(red, Math.min(green, blue));
        double max = Math.max(red, Math.max(green, blue));
        double delta = max - min;

        double hue;
        if (delta == 0.0) {
            hue = 0.0;
        } else if (red == max) {
            hue = 60.0 * (green - blue) / delta;
        } else if (green == max) {
            hue = 60.0 * (blue - red) / delta + 120.0;
        } else {
            hue = 60.0 * (red - green) / delta + 240.0;
        }

        double saturation = (max == 0.0) ? 0.0 : delta / max;

        if (hue < 0.0) hue += 360.0;
        return new HSVColor(hue, saturation, max);
    }

    static double rgbToHsv(double red, double green, double blue, int index) {
        HSVColor hsv = rgbToHsv(red, green, blue);
        return switch (index) {
            case 0 -> hsv.hue() / 360;
            case 1 -> hsv.saturation();
            case 2 -> hsv.value();
            default -> 0.0;
        };
    }

    record HSVColor(double hue, double saturation, double value) {}

}
