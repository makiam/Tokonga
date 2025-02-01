/* Copyright 2024-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion

import artofillusion.image.ImagesDialog
import artofillusion.ui.Translate
import buoy.widget.BMenu
import javax.swing.SwingUtilities

class LayoutSceneMenu(private val layout: LayoutWindow) : BMenu(Translate.text("menu.scene")) {

    init {

        this.add(Translate.menuItem("renderScene") { edt { RenderSetupDialog(layout, layout.scene) }})
        this.add(Translate.menuItem("renderImmediately") { edt { RenderSetupDialog.renderImmediately(layout, layout.scene) }})
        this.addSeparator()
        this.add(Translate.menuItem("textures") { edt { TexturesAndMaterialsDialog(layout, layout.scene) }})
        this.add(Translate.menuItem("images") { edt { ImagesDialog(layout, layout.scene) }})
        this.add(Translate.menuItem("environment") { edt { EnvironmentPropertiesDialog(layout) }})
    }

    private fun edt(action: () -> Unit) = SwingUtilities.invokeLater { action() }
}
