/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.script;

import artofillusion.LayoutWindow;

import javax.swing.*;


public class ScriptEditorEx extends JFrame {

    private static final long serialVersionUID = 1L;
    private LayoutWindow layout;
    public ScriptEditorEx(LayoutWindow owner) {
        this.layout = owner;
    }

    @Override
    protected void frameInit() {
        super.frameInit();
        this.setSize(800, 600);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
