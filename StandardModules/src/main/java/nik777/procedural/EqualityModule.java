package nik777.procedural;

import artofillusion.math.RGBColor;
import artofillusion.procedural.IOPort;

import artofillusion.procedural.ProceduralModule;
import artofillusion.ui.Translate;
import java.awt.Point;

@ProceduralModule.Category("Equality")
public class EqualityModule extends ProceduralModule<EqualityModule> {
    private RGBColor tmpColor = null;

    private static double tol = 1.0E-12D;

    private static final int FALSE_VALUE = 0;

    private static final int TRUE_VALUE = 1;

    public EqualityModule() {
        this(new Point());
    }

    public EqualityModule(Point position) {
        super("Equality", new IOPort[] {
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 2, "False", "(0.0)"),
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 2, "True", "(1.0)"),
                    new IOPort(IOPort.COLOR, IOPort.INPUT, 0, "Color 1", '(' + Translate.text("white") + ')'),  //2
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 0, "Scalar 1:1", "(1.0)"),
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 0, "Scalar 1:2", "(1.0)"),
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 0, "Scalar 1:3", "(1.0)"),
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 0, "Scalar 1:4", "(1.0)"),
                    new IOPort(IOPort.COLOR, IOPort.INPUT, 1, "Color 2", '(' + Translate.text("black") + ')'),  //7
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 1, "Scalar 2:1", "(0.0)"),
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 1, "Scalar 2:2", "(0.0)"),
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 1, "Scalar 2:3", "(0.0)"),
                    new IOPort(IOPort.NUMBER, IOPort.INPUT, 1, "Scalar 2:4", "(0.0)")
                },

                new IOPort[] {
                        new IOPort(IOPort.NUMBER, IOPort.OUTPUT, 3, "True/False")
                    }, position);
        this.tmpColor = new RGBColor();
    }

    public double getAverageValue(int which, double blur) {
        boolean nonNull = false;

        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;

        for (int i = 2; i < 5; i++) {
            if (i == 2 && this.linkFrom[2] != null) {
                nonNull = true;
                if (this.linkFrom[3] != null) {
                    this.linkFrom[3].getColor(this.linkFromIndex[3], this.tmpColor, blur);
                    r = this.tmpColor.red;
                    g = this.tmpColor.green;
                    b = this.tmpColor.blue;
                }
                this.linkFrom[2].getColor(this.linkFromIndex[2], this.tmpColor, blur);
                double diff = (r - this.tmpColor.red);
                if (diff > tol || diff < -tol)
                    return getResult(false, blur);
                diff = (g - this.tmpColor.green);
                if (diff > tol || diff < -tol)
                    return getResult(false, blur);
                diff = (b - this.tmpColor.blue);
                if (diff > tol || diff < -tol)
                    return getResult(false, blur);
            }
            if (this.linkFrom[i] != null && this.linkFrom[i + 5] != null) {
                nonNull = true;
                double diff = this.linkFrom[i].getAverageValue(this.linkFromIndex[i], blur) - this.linkFrom[i + 5].getAverageValue(this.linkFromIndex[i + 5], blur);
                if (diff > tol || diff < -tol)
                    return getResult(false, blur);
            }
        }
        return getResult(nonNull, blur);
    }

    private double getResult(boolean trueFalse, double blur) {
        if (trueFalse)
            return (this.linkFrom[TRUE_VALUE] == null) ? 1.0D : this.linkFrom[TRUE_VALUE].getAverageValue(this.linkFromIndex[TRUE_VALUE], blur);
        return (this.linkFrom[FALSE_VALUE] == null) ? 0.0D : this.linkFrom[FALSE_VALUE].getAverageValue(this.linkFromIndex[FALSE_VALUE], blur);
    }

    public double getValueError(int which, double blur) {
        return tol;
    }
}
