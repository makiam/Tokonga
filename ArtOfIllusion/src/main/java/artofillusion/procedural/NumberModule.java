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
 * This is a Module which outputs a number.
 */
@ProceduralModule.Category("Modules:menu.values")
public class NumberModule extends ProceduralModule<NumberModule> {

    private double value;

    public NumberModule() {
        this(new Point());
    }

    public NumberModule(Point position) {
        this(position, 0);
    }

    public NumberModule(Point position, double v) {
        super(Double.toString(v), new IOPort[]{}, new IOPort[]{
            new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Value")},
                position);
        value = v;
    }

    /**
     * Get the value.
     */
    public double getValue() {
        return value;
    }

    /**
     * Set the value.
     */
    public void setValue(double v) {
        value = v;
    }

    /**
     * Allow the user to set a new value.
     */
    @Override
    public boolean edit(final ProcedureEditor editor, Scene theScene) {
        final ValueField field = new ValueField(value, ValueField.NONE);
        field.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent() {
                value = field.getValue();
                editor.updatePreview();
            }
        });
        ComponentsDialog dlg = new ComponentsDialog(editor.getParentFrame(), Translate.text("Modules:selectValue"), new Widget[]{field},
                new String[]{null});
        if (!dlg.clickedOk()) {
            return false;
        }
        value = field.getValue();
        name = Double.toString(value);
        layout();
        return true;
    }

    /**
     * This module simply outputs the value.
     */
    @Override
    public double getAverageValue(int which, double blur) {
        return value;
    }

    public void getValueGradient(Vec3 grad, double blur) {
        grad.set(0.0, 0.0, 0.0);
    }

    /* Create a duplicate of this module. */
    @Override
    public NumberModule duplicate() {
        NumberModule mod = new NumberModule(new Point(bounds.x, bounds.y));

        mod.value = value;
        mod.name = "" + value;
        return mod;
    }

    /* Write out the parameters. */
    @Override
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        out.writeDouble(value);
    }

    /* Read in the parameters. */
    @Override
    public void readFromStream(DataInputStream in, Scene theScene) throws IOException {
        value = in.readDouble();
        name = "" + value;
        layout();
    }
}
