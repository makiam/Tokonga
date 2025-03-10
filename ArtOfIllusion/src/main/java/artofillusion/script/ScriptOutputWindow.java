/* Copyright (C) 2002-2008 by Peter Eastman
 * Changes copyright (C) 2024 by Maksim Khramov
 *
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.awt.*;

/**
 * This class creates a window for displaying output from scripts.
 */
public class ScriptOutputWindow extends OutputStream {

    BFrame window;
    BTextArea text;

    @Override
    public void write(final int b) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    write(b);
                }
            });
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
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    write(bytes, off, len);
                }
            });
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
        window = new BFrame("Script Output");
        BorderContainer content = new BorderContainer();
        window.setContent(content);
        text = new BTextArea(10, 60);
        text.setFont(UIUtilities.getDefaultFont());
        BScrollPane sp = new BScrollPane(text, BScrollPane.SCROLLBAR_ALWAYS, BScrollPane.SCROLLBAR_ALWAYS);
        content.add(sp, BorderContainer.CENTER);
        content.add(Translate.button("close", event -> closeWindow()), BorderContainer.SOUTH, new LayoutInfo());
        window.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ScriptOutputWindow.this.closeWindow();
            }
        });
        window.pack();
        window.setVisible(true);
    }

    /**
     * Hide the window.
     */
    private void closeWindow() {
        window.setVisible(false);
        text.setText("");
    }
}
