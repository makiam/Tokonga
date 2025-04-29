/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.translators

import artofillusion.Scene
import artofillusion.Translator
import artofillusion.ui.Translate
import buoy.widget.BFrame
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class PLYImporter {
    companion object {
        fun importFile(parent: BFrame?) {
            val chooser = JFileChooser();
            val objFilter = FileNameExtensionFilter(Translate.text("fileFilter.ply"), "ply");
            chooser.addChoosableFileFilter(objFilter)
            chooser.isAcceptAllFileFilterUsed = true
            chooser.fileFilter = objFilter
            chooser.showOpenDialog(parent?.component);

        }
    }
}

class PLYTranslator: Translator {
    override fun getName(): String? = "Stanford Triangle Format (.ply)"

    override fun importFile(parent: BFrame?) = PLYImporter.importFile(parent);

    override fun canExport(): Boolean = false

    override fun exportFile(parent: BFrame?, theScene: Scene?) {
    }
}