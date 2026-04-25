/* Copyright (C) 2026 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.ArtOfIllusion;
import artofillusion.ui.Translate;

import javax.swing.*;
import java.nio.file.Paths;

public class TemplatesListModel extends DefaultComboBoxModel<String> {

    public TemplatesListModel() {
        addElement(Translate.text("polymesh:cube"));
        addElement(Translate.text("polymesh:face"));
        addElement(Translate.text("polymesh:octahedron"));
        addElement(Translate.text("polymesh:cylinder"));
        addElement(Translate.text("polymesh:flatMesh"));

        var path = Paths.get(ArtOfIllusion.PLUGIN_DIRECTORY, "PolyMeshTemplates").toFile();
        for(var file: path.list()) addElement(file);
    }
}
