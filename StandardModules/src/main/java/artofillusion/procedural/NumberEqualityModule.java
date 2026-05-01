/* Copyright 2025-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import java.awt.Point;

@ProceduralModule.Category("Modules:menu.operators")
public class NumberEqualityModule extends ProceduralModule<NumberEqualityModule> {

    public static final double defaultTolerance = 1.0E-12;

    public NumberEqualityModule() {
        this(new Point());
    }

    public NumberEqualityModule(Point position) {
        super("Number Equality",
            new IOPort[]{
                new NumericInputPort(IOPort.LEFT, "Value 1", "(0)"),
                new NumericInputPort(IOPort.LEFT, "Value 2", "(0)"),
                new NumericInputPort(IOPort.LEFT, "Tolerance", "(1.0E-12D)")
            },
            new IOPort[]{new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "True/False")},
            position);
    }

    @Override
    public double getAverageValue(int which, double blur) {
        if (linkFrom[0] == null || linkFrom[1] == null) return 0.0;
        double ldv0 = linkFrom[0].getAverageValue(0, 0.0);
        double ldv1 = linkFrom[1].getAverageValue(1, 0.0);
        double tolerance = (linkFrom[2] == null) ? defaultTolerance : linkFrom[2].getAverageValue(2, 0.0);
        return Math.abs(ldv0 - ldv1) < tolerance ? 1.0 : 0.0;
    }
}
