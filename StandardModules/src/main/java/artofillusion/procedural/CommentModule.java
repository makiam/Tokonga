/* Copyright (C) 2004 by Peter Eastman
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.*;
import artofillusion.ui.*;
import buoy.widget.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * This is a Module which displays a comment, but otherwise has no effect on the
 * procedure.
 */
@ProceduralModule.Category("Modules:menu.values")
public class CommentModule extends ProceduralModule<CommentModule> {

    public CommentModule() {
        this(new Point());
    }

    public CommentModule(Point position) {
        this(position, "Commentary");
    }

    public CommentModule(Point position, String text) {
        super(text, new IOPort[]{}, new IOPort[]{}, position);
    }

    /**
     * Allow the user to edit the comment text.
     */
    @Override
    public boolean edit(ProcedureEditor editor, Scene theScene) {
        BTextArea ta = new BTextArea(name, 10, 40);
        JTextArea area = ta.getComponent();
        area.setFont(area.getFont().deriveFont(12f));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        PanelDialog dlg = new PanelDialog(editor.getParentFrame(), Translate.text("Modules:editComment"), BOutline.createBevelBorder(new BScrollPane(ta), false));
        if (!dlg.clickedOk()) {
            return false;
        }
        name = ta.getText();
        layout();
        return true;
    }

    /* Create a duplicate of this module. */
    @Override
    public CommentModule duplicate() {
        return new CommentModule(new Point(bounds.x, bounds.y), name);
    }

    /* Write out the parameters. */
    @Override
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        out.writeUTF(name);
    }

    /* Read in the parameters. */
    @Override
    public void readFromStream(DataInputStream in, Scene theScene) throws IOException {
        name = in.readUTF();
        layout();
    }

    /**
     * Calculate the size on the screen of this module.
     */
    @Override
    public void calcSize() {
        String[] lines = name.split("\n");
        bounds.width = 0;
        for (String line : lines) {
            int len = defaultMetrics.stringWidth(line);
            if (len > bounds.width) {
                bounds.width = len;
            }
        }
        bounds.width += IOPort.SIZE * 4;
        bounds.height = lines.length * (defaultMetrics.getMaxAscent() + defaultMetrics.getMaxDescent()) + IOPort.SIZE * 4;
    }

    /**
     * Draw the contents of the module.
     */
    @Override
    protected void drawContents(Graphics2D g) {
        g.setColor(Color.black);
        g.setFont(defaultFont);
        int lineHeight = defaultMetrics.getMaxAscent() + defaultMetrics.getMaxDescent();
        int offset = defaultMetrics.getAscent();
        String[] lines = name.split("\n");
        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], bounds.x + IOPort.SIZE * 2, bounds.y + IOPort.SIZE * 2 + offset + i * lineHeight);
        }
    }
}
