/* Copyright (C) 2000-2011 by Peter Eastman
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;

/**
 * This is a Module which generates a wood pattern.
 */
@ProceduralModule.Category("Modules:menu.patterns")
public class WoodModule extends ProceduralModule<WoodModule> {

    boolean valueOk, mod;
    int octaves;
    double value, error, amplitude, spacing, lastBlur;
    final Vec3 gradient;
    final Vec3 tempVec;
    PointInfo point;

    public WoodModule() {
        this(new Point());
    }

    public WoodModule(Point position) {
        super(Translate.text("Modules:menu.woodModule"), new IOPort[]{new NumericInputPort(IOPort.LEFT, "X", "(X)"),
            new NumericInputPort(IOPort.LEFT, "Y", "(Y)"),
            new NumericInputPort(IOPort.LEFT, "Z", "(Z)"),
            new NumericInputPort(IOPort.LEFT, "Noise", "(0.5)")},
                new IOPort[]{new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Value")},
                position);
        octaves = 2;
        amplitude = 1.0;
        spacing = .25;
        mod = true;
        gradient = new Vec3();
        tempVec = new Vec3();
    }

    /**
     * Get the number of octaves.
     */
    public int getOctaves() {
        return octaves;
    }

    /**
     * Set the number of octaves.
     */
    public void setOctaves(int o) {
        octaves = o;
    }

    /**
     * Get the amplitude.
     */
    public double getAmplitude() {
        return amplitude;
    }

    /**
     * Set the amplitude.
     */
    public void setAmplitude(double a) {
        amplitude = a;
    }

    /**
     * Get the spacing.
     */
    public double getSpacing() {
        return spacing;
    }

    /**
     * Set the spacing.
     */
    public void setSpacing(double s) {
        spacing = s;
    }

    /* New point, so the value will need to be recalculated. */
    @Override
    public void init(PointInfo p) {
        point = p;
        valueOk = false;
    }

    /* Calculate the value, error, and gradient all at once, since calculating just the value
     is almost as much work as calculating all three. */
    @Override
    public double getAverageValue(int which, double blur) {
        if (valueOk && blur == lastBlur) {
            return value;
        }
        double x = (linkFrom[0] == null) ? point.x : linkFrom[0].getAverageValue(linkFromIndex[0], blur);
        double y = (linkFrom[1] == null) ? point.y : linkFrom[1].getAverageValue(linkFromIndex[1], blur);
        double z = (linkFrom[2] == null) ? point.z : linkFrom[2].getAverageValue(linkFromIndex[2], blur);
        double persistence = (linkFrom[3] == null) ? 0.5 : linkFrom[3].getAverageValue(linkFromIndex[3], blur);
        double xSize = (linkFrom[0] == null) ? 0.5 * point.xsize + blur : linkFrom[0].getValueError(linkFromIndex[0], blur);
        double ySize = (linkFrom[1] == null) ? 0.5 * point.ysize + blur : linkFrom[1].getValueError(linkFromIndex[1], blur);
        double zSize = (linkFrom[2] == null) ? 0.5 * point.zsize + blur : linkFrom[2].getValueError(linkFromIndex[2], blur);
        double amp = 0.5 * amplitude;
        double scale = 1.0;
        double d;
        double cutoff = 0.5 / Math.max(Math.max(xSize, ySize), zSize);

        // First calculate the turbulence function.
        value = 0.0;
        error = 0.0;
        gradient.set(0.0, 0.0, 0.0);
        for (int i = 0; i < octaves && cutoff > scale; i++) {
            d = amp * Noise.value(x * scale + 123.456, y * scale + 123.456, z * scale + 123.456);
            Noise.calcGradient(tempVec, x * scale + 123.456, y * scale + 123.456, z * scale + 123.456);
            if (d > 0.0) {
                tempVec.scale(amp * scale);
            } else {
                tempVec.scale(-amp * scale);
            }
            error += Math.abs(xSize * tempVec.x) + Math.abs(ySize * tempVec.y) + Math.abs(zSize * tempVec.z);
            if (scale > 0.5 * cutoff) {
                d *= 2.0 * (1.0 - scale / cutoff);
                tempVec.scale(2.0 * (1.0 - scale / cutoff));
            }
            value += Math.abs(d);
            gradient.add(tempVec);
            amp *= persistence;
            scale *= 2.0;
        }

        // Now use that to calculate the wood function.
        scale = 1.0 / spacing;
        double r = Math.sqrt(x * x + y * y), rinv = 1.0 / r;
        if (linkFrom[0] == null) {
            tempVec.set(1.0, 0.0, 0.0);
        } else {
            linkFrom[0].getValueGradient(linkFromIndex[0], tempVec, blur);
        }
        if (r == 0.0) {
            gradient.set(0.0, 0.0, 0.0);
            error = 1.0e6;
        } else {
            gradient.set(x * rinv * scale * tempVec.x + gradient.x, y * rinv * scale * tempVec.y + gradient.y, gradient.z);
            error = Math.abs(xSize * gradient.x) + Math.abs(ySize * gradient.y) + Math.abs(zSize * gradient.z);
        }
        if (!mod) {
            value = r * scale + value;
        } else if (error == 0.0) {
            value = r * scale + value;
            value -= FastMath.floor(value);
        } else if (error >= 0.5) {
            value = error = 0.5;
        } else {
            double min = r * scale + value - error;
            double max = r * scale + value + error;
            min -= FastMath.floor(min);
            max -= FastMath.floor(max);
            if (max > min) {
                value = 0.5 * (max + min);
            } else {
                value = (0.5 * max * max + 0.5 * (1.0 + min) * (1.0 - min)) / (1.0 - min + max);
                gradient.scale((max - min) / (2.0 * error));
                error = 0.5;
            }
        }
        valueOk = true;
        lastBlur = blur;
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

    /* The gradient is calculated at the same time as the value. */
    @Override
    public void getValueGradient(int which, Vec3 grad, double blur) {
        if (!valueOk || blur != lastBlur) {
            getAverageValue(which, blur);
        }
        grad.set(gradient);
    }

    /* Allow the user to set the parameters. */
    @Override
    public boolean edit(final ProcedureEditor editor, Scene theScene) {
        final ValueField octavesField = new ValueField((double) octaves, ValueField.POSITIVE + ValueField.INTEGER);
        final ValueField ampField = new ValueField(amplitude, ValueField.NONE);
        final ValueField spacingField = new ValueField(spacing, ValueField.POSITIVE);
        final BCheckBox modBox = new BCheckBox("Only Output Fraction", mod);
        Object listener = new Object() {
            void processEvent() {
                octaves = (int) octavesField.getValue();
                amplitude = ampField.getValue();
                spacing = spacingField.getValue();
                mod = modBox.getState();
                editor.updatePreview();
            }
        };
        octavesField.addEventLink(ValueChangedEvent.class, listener);
        ampField.addEventLink(ValueChangedEvent.class, listener);
        spacingField.addEventLink(ValueChangedEvent.class, listener);
        modBox.addEventLink(ValueChangedEvent.class, listener);
        ComponentsDialog dlg = new ComponentsDialog(editor.getParentFrame(), Translate.text("Modules:selectWoodProperties"),
                new Widget[]{ampField, spacingField, octavesField, modBox},
                new String[]{Translate.text("Modules:noiseAmplitude"), Translate.text("Modules:ringSpacing"), Translate.text("Modules:noiseOctaves"), null});
        return dlg.clickedOk();
    }

    /* Create a duplicate of this module. */
    @Override
    public WoodModule duplicate() {
        WoodModule module = new WoodModule(new Point(bounds.x, bounds.y));

        module.octaves = octaves;
        module.amplitude = amplitude;
        module.spacing = spacing;
        module.mod = mod;
        return module;
    }

    /* Write out the parameters. */
    @Override
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        out.writeInt(octaves);
        out.writeDouble(amplitude);
        out.writeDouble(spacing);
    }

    /* Read in the parameters. */
    @Override
    public void readFromStream(DataInputStream in, Scene theScene) throws IOException {
        octaves = in.readInt();
        amplitude = in.readDouble();
        spacing = in.readDouble();
    }
}
