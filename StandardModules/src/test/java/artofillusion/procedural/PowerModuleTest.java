/* Copyright (C) 2006 by Peter Eastman
   Changes copyright (C) 2017 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.math.*;

import artofillusion.ui.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.*;


public class PowerModuleTest {

    @BeforeAll
    public static void setUpClass() {
        Translate.setLocale(Locale.US);
    }

    @Test
    public void testValue() {
        ExprModule module = new ExprModule(new Point());
        module.setExpr("2^3");
        checkValue(module, 0.0, 8.0);
        module.setExpr("x^3");
        checkValue(module, 1.0, 1.0);
        checkValue(module, 2.0, 8.0);
        checkValue(module, 1.5, Math.pow(1.5, 3.0));
        checkValue(module, -1.0, Math.pow(-1.0, 3.0));
        checkValue(module, -1.5, Math.pow(-1.5, 3.0));
        module.setExpr("x^2.5");
        checkValue(module, 1.0, 1.0);
        checkValue(module, 2.5, Math.pow(2.5, 2.5));
        checkValue(module, -1.0, -1.0);
        checkValue(module, -2.0, -Math.pow(2.0, 2.5));
        module.setExpr("x^-2.0");
        checkValue(module, 1.0, 1.0);
        checkValue(module, 2.5, Math.pow(2.5, -2.0));
        checkValue(module, -1.0, 1.0);
        checkValue(module, -2.0, Math.pow(2.0, -2.0));
        module.setExpr("x^x");
        checkValue(module, 1.0, 1.0);
        checkValue(module, 3.0, Math.pow(3.0, 3.0));
        checkValue(module, -1.0, -1.0);
        checkValue(module, -2.0, -Math.pow(2.0, -2.0));
    }

    private void checkValue(Module module, double x, double expected) {
        PointInfo info = new PointInfo();
        info.x = x;
        info.xsize = 1e-10;
        module.init(info);
        Assertions.assertEquals(expected, module.getAverageValue(0, 0.0), 1e-4);
    }

    @Test
    public void testGradient() {
        ExprModule module = new ExprModule(new Point());
        module.setExpr("x^3");
        checkGradient(module, 1.0, 3.0);
        checkGradient(module, 2.0, 12.0);
        checkGradient(module, -1.0, 3.0);
        checkGradient(module, -2.0, 12.0);
        module.setExpr("x^-2");
        checkGradient(module, 1.0, -2.0);
        checkGradient(module, 2.0, -2.0 / 8.0);
        checkGradient(module, -1.0, 2.0);
        checkGradient(module, -2.0, 2.0 / 8.0);
        module.setExpr("x^1.5");
        checkGradient(module, 1.0, 1.5);
        checkGradient(module, 2.0, 1.5 * Math.pow(2.0, 0.5));
        checkGradient(module, -1.0, 1.5);
        checkGradient(module, -2.0, 1.5 * Math.pow(2.0, 0.5));
        module.setExpr("x^x");
        checkGradient(module, 1.0, 1.0);
        checkGradient(module, 2.0, 4.0);
        checkGradient(module, -1.0, -1.0);
        checkGradient(module, -2.0, -2.0 * Math.pow(2.0, -3.0));
    }

    private void checkGradient(Module module, double x, double expectedXGrad) {
        PointInfo info = new PointInfo();
        info.x = x;
        info.xsize = 1e-10;
        module.init(info);
        Vec3 grad = new Vec3();
        module.getValueGradient(0, grad, 0.0);
        Assertions.assertEquals(expectedXGrad, grad.x, 1e-4);
        Assertions.assertEquals(0.0, grad.y, 0.0);
        Assertions.assertEquals(0.0, grad.z, 0.0);
    }

    public void testError() {
        ExprModule module = new ExprModule(new Point());
        module.setExpr("x^3");
        checkError(module, 1.0);
        checkError(module, 10.0);
        checkError(module, -1.0);
        checkError(module, -10.0);
        module.setExpr("x^1.5");
        checkError(module, 1.0);
        checkError(module, 10.0);
        checkError(module, -1.0);
        checkError(module, -10.0);
        module.setExpr("x^0.2");
        checkError(module, 1.0);
        checkError(module, 10.0);
        checkError(module, -1.0);
        checkError(module, -10.0);
        module.setExpr("x^-2");
        checkError(module, 1.0);
        checkError(module, 10.0);
        checkError(module, -1.0);
        checkError(module, -10.0);
        module.setExpr("x^-2.5");
        checkError(module, 1.0);
        checkError(module, 10.0);
        checkError(module, -1.0);
        checkError(module, -10.0);
    }

    private void checkError(Module module, double x) {
        PointInfo info = new PointInfo();
        info.x = x;
        info.xsize = 0.01;
        module.init(info);
        double error = module.getValueError(0, 0.0);
        info.x = x - 0.005;
        info.xsize = 0.0;
        module.init(info);
        double lower = module.getAverageValue(0, 0.0);
        info.x = x + 0.005;
        module.init(info);
        double upper = module.getAverageValue(0, 0.0);
        Assertions.assertEquals(Math.abs(upper - lower) * 0.5, error, 1e-4);
    }
}
