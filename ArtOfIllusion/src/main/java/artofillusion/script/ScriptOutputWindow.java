/* Copyright (C) 2002-2008 by Peter Eastman
 * Changes copyright (C) 2024-2026 by Maksim Khramov
 *
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import java.io.*;
import java.awt.*;

import javax.swing.JTextArea;

/**
 * This class creates a window for displaying output from scripts.
 */
public class ScriptOutputWindow extends OutputStream {

    private ScriptOutputWindowImpl window;
    private JTextArea text;

    @Override
    public void write(final int b) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(() -> write(b));
            return;
        }
        if (window == null) {
            createWindow();
        } else if (!window.isVisible()) {
            window.setVisible(true);
        }
        text.append(String.valueOf((char) b));
    }

    @Override
    public void write(byte[] b, final int off, final int len) {
        if (!EventQueue.isDispatchThread()) {
            final byte[] bytes = b.clone();
            EventQueue.invokeLater(() -> write(bytes, off, len));
            return;
        }
        if (window == null) {
            createWindow();
        } else if (!window.isVisible()) {
            window.setVisible(true);
        }
        text.append(new String(b, off, len));
    }

    /**
     * Create the window.
     */
    private void createWindow() {
        window = new ScriptOutputWindowImpl();
        window.pack();
        window.setVisible(true);
        text = window.getTextArea();
    }
}
