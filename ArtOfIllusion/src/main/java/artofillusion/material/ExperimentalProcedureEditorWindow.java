/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.material;

import artofillusion.ArtOfIllusion;
import artofillusion.Scene;
import artofillusion.procedural.Procedure;
import artofillusion.procedural.ProcedureOwner;
import java.awt.BorderLayout;

import artofillusion.ui.Translate;
import lombok.Getter;
import lombok.Setter;
import artofillusion.procedural.ui.ModulesMenu;

import javax.swing.*;
import java.awt.event.WindowEvent;

public class ExperimentalProcedureEditorWindow extends JFrame {

    public ExperimentalProcedureEditorWindow() {
        super();
    }

    public ExperimentalProcedureEditorWindow(Procedure proc, ProcedureOwner owner, Scene scene) {
        this();
        this.procedure = proc;
        this.procedureOwner = owner;
        this.scene = scene;
    }

    @Getter @Setter
    private Procedure procedure;
    @Getter @Setter
    private ProcedureOwner procedureOwner;
    @Getter @Setter
    private Scene scene;
    
    private ProcedureView view = new ProcedureView();
    private ModulesMenu menu = new ModulesMenu();
    @Override
    protected void frameInit() {
        super.frameInit();
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
            }
        });
    }
    
    void initPeer() {
        this.setIconImage(ArtOfIllusion.APP_ICON.getImage());
        this.setSize(1280, 1024);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle(procedureOwner.getWindowTitle());
        this.setIconImage(ArtOfIllusion.APP_ICON.getImage());
        this.getContentPane().setLayout(new BorderLayout());
        var split = new JSplitPane();
        split.setRightComponent(view);
        split.setLeftComponent(menu);
        this.getContentPane().add(split, java.awt.BorderLayout.CENTER);
        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(new JMenu(Translate.text("menu.edit")));
    }
}
