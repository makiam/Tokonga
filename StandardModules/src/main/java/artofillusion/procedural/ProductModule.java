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
import java.awt.*;

/**
 * This is a Module which outputs the product of two numbers.
 */
@ProceduralModule.Category("Modules:menu.operators")
public class ProductModule extends ProceduralModule<ProductModule> {

    boolean valueOk, errorOk;
    double value, error, valueIn1, valueIn2, errorIn1, errorIn2, lastBlur;
    final Vec3 tempVec;

    public ProductModule() {
        this(new Point());
    }

    public ProductModule(Point position) {
        super("\u00D7", new IOPort[]{new NumericInputPort(IOPort.TOP, "Value 1", "(0)"),
            new NumericInputPort(IOPort.BOTTOM, "Value 2", "(0)")},
                new IOPort[]{new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Product")},
                position);
        tempVec = new Vec3();
    }

    /* New point, so the value will need to be recalculated. */
    @Override
    public void init(PointInfo p) {
        valueOk = errorOk = false;
    }

    /* This module outputs the product of the two values. */
    @Override
    public double getAverageValue(int which, double blur) {
        if (valueOk && blur == lastBlur) {
            return value;
        }
        valueOk = true;
        lastBlur = blur;
        if (linkFrom[0] == null || linkFrom[1] == null) {
            valueIn1 = valueIn2 = value = error = 0.0;
            errorOk = true;
            return 0.0;
        }
        valueIn1 = linkFrom[0].getAverageValue(linkFromIndex[0], blur);
        valueIn2 = linkFrom[1].getAverageValue(linkFromIndex[1], blur);
        value = valueIn1 * valueIn2;
        return value;
    }

    /* Calculate the error. */
    @Override
    public double getValueError(int which, double blur) {
        if (!valueOk || blur != lastBlur) {
            getAverageValue(which, blur);
        }
        if (errorOk) {
            return error;
        }
        valueOk = errorOk = true;
        errorIn1 = linkFrom[0].getValueError(linkFromIndex[0], blur);
        errorIn2 = linkFrom[1].getValueError(linkFromIndex[1], blur);
        error = Math.abs(valueIn1 * errorIn2) + Math.abs(valueIn2 * errorIn1);
        return error;
    }

    /* Calculate the gradient. */
    @Override
    public void getValueGradient(int which, Vec3 grad, double blur) {
        if (linkFrom[0] == null || linkFrom[1] == null) {
            grad.set(0.0, 0.0, 0.0);
            return;
        }
        if (!valueOk || blur != lastBlur) {
            getAverageValue(which, blur);
        }
        linkFrom[0].getValueGradient(linkFromIndex[0], grad, blur);
        linkFrom[1].getValueGradient(linkFromIndex[1], tempVec, blur);
        grad.scale(valueIn2);
        tempVec.scale(valueIn1);
        grad.add(tempVec);

    }
}
