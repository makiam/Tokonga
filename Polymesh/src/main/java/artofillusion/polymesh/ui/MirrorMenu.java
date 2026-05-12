/*
   Copyright (C) 2026 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh.ui;

import artofillusion.polymesh.PolyMesh;
import artofillusion.polymesh.PolyMeshEditorWindow;
import artofillusion.polymesh.TurnMirrorOffDialog;
import artofillusion.ui.Translate;
import buoy.widget.BMenu;
import buoy.widget.BMenuItem;


import javax.swing.*;
import java.awt.event.ActionEvent;

public final class MirrorMenu extends BMenu {

    private final PolyMeshEditorWindow view;

    public MirrorMenu(PolyMeshEditorWindow view) {
        super(Translate.text("polymesh:menu.mirrorMesh"));
        this.view = view;

        var mi = new BMenuItem();
        mi.getComponent().setAction(new MirrorOffAction());
        mi.getComponent().setText(Translate.text("polymesh:menu.mirrorOff"));
        this.add(mi);

    }

    private class MirrorOffAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            PolyMesh mesh = (PolyMesh) view.getObject().getGeometry();
            var accept = mesh.getMirrorState() != PolyMesh.NO_MIRROR;
            if(accept) SwingUtilities.invokeLater(() -> new TurnMirrorOffDialog(MirrorMenu.this.view).setVisible(true));
        }
    }


}
