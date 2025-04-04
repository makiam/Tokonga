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
import java.awt.*;
import java.io.*;

/**
 * This is a Module which outputs a color.
 */
@ProceduralModule.Category("Modules:menu.values")
public class ColorModule extends ProceduralModule<ColorModule> {

    private RGBColor color = new RGBColor(1.0f, 1.0f, 1.0f);

    public ColorModule() {
        this(new Point());
    }

    public ColorModule(Point position) {
        super("", new IOPort[]{}, new IOPort[]{new IOPort(IOPort.COLOR, IOPort.OUTPUT, IOPort.RIGHT, "Color")}, position);
    }

    /**
     * Get the color.
     */
    public RGBColor getColor() {
        return color;
    }

    /**
     * Set the color.
     */
    public void setColor(RGBColor c) {
        color = c;
    }

    /* Allow the user to set a new value. */
    @Override
    public boolean edit(final ProcedureEditor editor, Scene theScene) {
        final ColorChooser cc = new ColorChooser(editor.getParentFrame(), "Select Color", color, false);
        cc.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent() {
                color.copy(cc.getColor());
                editor.updatePreview();
            }
        });
        cc.setVisible(true);
        return cc.clickedOk();
    }

    /* This module simply outputs the color. */
    @Override
    public void getColor(int which, RGBColor c, double blur) {
        c.copy(color);
    }

    @Override
    public void calcSize() {
        bounds.width = bounds.height = 20 + IOPort.SIZE * 2;
    }

    @Override
    protected void drawContents(Graphics2D g) {
        g.setColor(color.getColor());
        g.fillRect(bounds.x + IOPort.SIZE, bounds.y + IOPort.SIZE, 20, 20);
    }

    /* Create a duplicate of this module. */
    @Override
    public ColorModule duplicate() {
        ColorModule mod = new ColorModule(new Point(bounds.x, bounds.y));

        mod.color.copy(color);
        return mod;
    }

    /* Write out the parameters. */
    @Override
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        color.writeToFile(out);
    }

    /* Read in the parameters. */
    @Override
    public void readFromStream(DataInputStream in, Scene theScene) throws IOException {
        color = new RGBColor(in);
    }
}
