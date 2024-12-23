/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.material;

import artofillusion.Scene;
import artofillusion.procedural.Procedure;
import artofillusion.procedural.ProcedureOwner;
import buoy.widget.BFrame;


import javax.swing.*;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class ExperimentalProcedureEditor extends BFrame {
    

    private Procedure procedure;
    private ProcedureOwner owner;
    private Scene scene;
    
    public ExperimentalProcedureEditor(Procedure proc, ProcedureOwner owner, Scene scene) {
        super();
        this.procedure = proc;
        this.owner = owner;
        this.scene = scene;
        initPeer();
    }

    private void initPeer() {
        var peer = (ExperimentalProcedureEditorWindow)this.getComponent();
        peer.setScene(scene);
        peer.setProcedure(procedure);
        peer.setProcedureOwner(owner);
        peer.initPeer();
        this.setVisible(true);        
    }
    @Override
    protected JFrame createComponent() {
        return new ExperimentalProcedureEditorWindow();
    }
}
