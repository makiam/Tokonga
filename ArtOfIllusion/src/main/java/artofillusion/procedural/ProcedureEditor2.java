/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion.procedural;

import artofillusion.Scene;
import buoy.widget.BFrame;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * This is the editor for editing procedures.
 */
@Slf4j
public class ProcedureEditor2 extends BFrame {

    private final Procedure proc;
    @Getter
    private final ProcedureOwner owner;
    @Getter
    private final Scene scene;

    public ProcedureEditor2(Procedure proc, ProcedureOwner owner, Scene scene) {
        super();
        this.proc = proc;
        this.owner = owner;
        this.scene = scene;

        ProcedureEditorImpl cc = (ProcedureEditorImpl) this.getComponent();
        cc.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        cc.setModel(proc);
        cc.setLayout(new BorderLayout());
        cc.getContentPane().add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JTree(), new ProcedureEditorCanvas()));
        this.setTitle(owner.getWindowTitle());

        this.setVisible(true);


    }

    /**
     * Create the JFrame which serves as this Widget's Component. This method is
     * protected so that subclasses can override it.
     */
    @Override
    protected ProcedureEditorImpl createComponent() {
        return new ProcedureEditorImpl();
    }
}
