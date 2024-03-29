/* Copyright (C) 2002-2008 by Peter Eastman
   Changes copyright (C) 2022-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.ui.*;

/**
 * The tube tool creates Tube objects from Curves.
 */
public class TubeTool implements ModellingTool {

    /* Get the text that appear as the menu item.*/
    @Override
    public String getName() {
        return Translate.text("menu.tubeTool");
    }

    /* See whether an appropriate set of objects is selected and either display an error
     message, or bring up the extrude window. */
    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void commandSelected(LayoutWindow window) {
        Scene scene = window.getScene();
        int[] selection = window.getSelectedIndices();

        if (selection.length == 1) {
            ObjectInfo info = scene.getObject(selection[0]);
            if (info.getObject() instanceof Curve) {
                new TubeDialog(window, info);
                return;
            }
        }
        MessageDialog.create().withOwner(window.getComponent()).withTitle(this.getName()).info((Object)Translate.text("Tools:tube.tool.message"));
    }
}
