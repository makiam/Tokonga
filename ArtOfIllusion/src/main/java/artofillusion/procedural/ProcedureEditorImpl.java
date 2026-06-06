/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.ArtOfIllusion;
import artofillusion.ui.Translate;

import javax.swing.*;

public class ProcedureEditorImpl extends JFrame {


    /**
     * Called by the constructors to init the <code>JFrame</code> properly.
     */
    @Override
    protected void frameInit() {
        super.frameInit();
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setIconImage(ArtOfIllusion.APP_ICON.getImage());
        this.setSize(1280, 1024);

        this.setJMenuBar(new JMenuBar());
        var mb = this.getJMenuBar();
        mb.add(new JMenu(Translate.text("menu.edit")));

    }

    public void setModel(Procedure proc) {
    }
}
