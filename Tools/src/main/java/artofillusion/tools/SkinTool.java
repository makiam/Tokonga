/* Copyright (C) 2001-2008 by Peter Eastman
   Changes copyright (C) 2018 by Maksim Khramov

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
import java.util.*;

/**
 * The skin tool creates new objects by placing a skin over a series of curves.
 */
public class SkinTool implements ModellingTool {

    public SkinTool() {
    }

    /* Get the text that appear as the menu item.*/
    @Override
    public String getName() {
        return Translate.text("menu.skin");
    }

    /* See whether an appropriate set of objects is selected and either display an error
     message, or bring up the extrude window. */
    @Override
    public void commandSelected(LayoutWindow window) {
        Scene scene = window.getScene();
        int selection[] = window.getSelectedIndices();
        List<ObjectInfo> curves = new Vector<>();

        for (int i = 0; i < selection.length; i++) {
            ObjectInfo obj = scene.getObject(selection[i]);
            if (obj.getObject() instanceof Curve) {
                curves.add(obj);
            }
        }
        if (curves.size() < 2) {
            //TODO: Localize message
            Messages.information(UIUtilities.breakString("You must select two or more curves to create a skin across."), window.getFrame().getComponent());
            return;
        }
        Curve c = (Curve) curves.get(0).getObject();
        for (int i = 1; i < curves.size(); i++) {
            Curve c2 = (Curve) curves.get(i).getObject();
            if (c2.getVertices().length != c.getVertices().length) {
                //TODO: Localize message
                Messages.information(UIUtilities.breakString("All the curves must have the same number of points."), window.getFrame().getComponent());
                return;
            }
            if (c2.isClosed() != c.isClosed()) {
                //TODO: Localize message
                Messages.information(UIUtilities.breakString("You cannot create a skin between a closed curve and an open one."), window.getFrame().getComponent());
                return;
            }
            if (c2.getSmoothingMethod() != c.getSmoothingMethod() && c.getSmoothingMethod() != Mesh.NO_SMOOTHING && c2.getSmoothingMethod() != Mesh.NO_SMOOTHING) {
                //TODO: Localize message
                Messages.information(UIUtilities.breakString("You cannot create a skin between an interpolating curve and an approximating one."), window.getFrame().getComponent());
                return;
            }
        }
        new SkinDialog(window, curves);
    }
}
