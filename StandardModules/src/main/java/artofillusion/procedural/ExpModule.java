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
 *  This is a Module which outputs the exponential of a number.
 */
@ProceduralModule.Category("Modules:menu.functions")
public class ExpModule extends ProceduralModule<ExpModule> {

    boolean valueOk;
    boolean errorOk;
    boolean gradOk;
    double value, error, valueIn, errorIn, lastBlur;
    final Vec3 gradient;

    public ExpModule() {
        this(new Point());
    }

    public ExpModule(Point position) {
        super("Exp", new IOPort[]{new NumericInputPort(IOPort.LEFT, "Value", "(1)")},
                new IOPort[]{new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Exponential")},
                position);
        gradient = new Vec3();
    }

    /* New point, so the value will need to be recalculated. */
    @Override
    public void init(PointInfo p) {
        valueOk = errorOk = gradOk = false;
    }

    /* This module outputs the exponential of the input value. */
    @Override
    public double getAverageValue(int which, double blur) {
        if (valueOk && blur == lastBlur) {
            return value;
        }
        valueOk = true;
        lastBlur = blur;
        if (linkFrom[0] == null) {
            value = error = 0.0;
            return 0.0;
        }
        valueIn = linkFrom[0].getAverageValue(linkFromIndex[0], blur);
        errorIn = linkFrom[0].getValueError(linkFromIndex[0], blur);
        if (errorIn == 0.0) {
            value = Math.exp(valueIn);
            error = 0.0;
            return value;
        }
        value = (Math.exp(valueIn + errorIn) - Math.exp(valueIn - errorIn)) / (2.0 * errorIn);
        error = errorIn * value;
        return value;
    }

    /* The error is calculated at the same time as the value. */
    @Override
    public double getValueError(int which, double blur) {
        if (!valueOk || blur != lastBlur) {
            getAverageValue(which, blur);
        }
        return error;
    }

    /* Calculate the gradient. */
    @Override
    public void getValueGradient(int which, Vec3 grad, double blur) {
        if (gradOk && blur == lastBlur) {
            grad.set(gradient);
            return;
        }
        if (linkFrom[0] == null) {
            grad.set(0.0, 0.0, 0.0);
            return;
        }
        if (!valueOk || blur != lastBlur) {
            getAverageValue(which, blur);
        }
        gradOk = true;
        linkFrom[0].getValueGradient(linkFromIndex[0], gradient, blur);
        gradient.scale(valueIn);
        grad.set(gradient);
    }
}
