/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.image.ImagesDialog;
import artofillusion.ui.Translate;
import buoy.widget.BMenu;

import javax.swing.*;

public class LayoutSceneMenu extends BMenu {

    LayoutSceneMenu(LayoutWindow layout) {
        super(Translate.text("menu.scene"));

        add(Translate.menuItem("renderScene", e -> edt(() -> new RenderSetupDialog(layout,layout.getScene()))));
        add(Translate.menuItem("renderImmediately", e -> edt(() -> RenderSetupDialog.renderImmediately(layout, layout.getScene()))));
        this.addSeparator();
        this.add(Translate.menuItem("textures", e->  edt(() ->  new TexturesAndMaterialsDialog(layout, layout.getScene()))));
        this.add(Translate.menuItem("images", e -> edt(() -> new ImagesDialog(layout, layout.getScene()))));
        this.add(Translate.menuItem("environment", e -> edt(() -> new EnvironmentPropertiesDialog(layout))));
    }

    void edt(Runnable call) {
        SwingUtilities.invokeLater(call);
    }
}
