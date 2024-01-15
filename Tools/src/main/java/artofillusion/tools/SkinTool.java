/* Copyright (C) 2001-2008 by Peter Eastman
   Changes copyright (C) 2022-2023 by Maksim Khramov

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
import buoy.widget.*;
import java.util.*;

/**
 * The skin tool creates new objects by placing a skin over a series of curves.
 */
public class SkinTool implements ModellingTool {

    /* Get the text that appears as the menu item.*/
    @Override
    public String getName() {
        return Translate.text("menu.skin");
    }

    /* See whether an appropriate set of objects is selected and either display an error
     message, or bring up the skin dialog window. */
    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void commandSelected(LayoutWindow window) {
        Scene scene = window.getScene();

        List<ObjectInfo> curves = new Vector<>();

        for (int j : window.getSelectedIndices()) {
            ObjectInfo obj = scene.getObject(j);
            if (obj.getObject() instanceof Curve) {
                curves.add(obj);
            }
        }
        if (curves.size() < 2) {
            new BStandardDialog("", UIUtilities.breakString(Translate.text("Tools:skin.tool.message.curves")), BStandardDialog.INFORMATION).showMessageDialog(window.getFrame());
            return;
        }
        Curve c = (Curve) curves.get(0).getObject();
        for (int i = 1; i < curves.size(); i++) {
            Curve c2 = (Curve) curves.get(i).getObject();
            if (c2.getVertices().length != c.getVertices().length) {
                new BStandardDialog("", UIUtilities.breakString(Translate.text("Tools:skin.tool.curves.same.points")), BStandardDialog.INFORMATION).showMessageDialog(window.getFrame());
                return;
            }
            if (c2.isClosed() != c.isClosed()) {
                new BStandardDialog("", UIUtilities.breakString(Translate.text("Tools:skin.tool.curve.same.close")), BStandardDialog.INFORMATION).showMessageDialog(window.getFrame());
                return;
            }
            if (c2.getSmoothingMethod() != c.getSmoothingMethod() && c.getSmoothingMethod() != Mesh.NO_SMOOTHING && c2.getSmoothingMethod() != Mesh.NO_SMOOTHING) {
                new BStandardDialog("", UIUtilities.breakString(Translate.text("Tools:skin.tool.curve.same.type")), BStandardDialog.INFORMATION).showMessageDialog(window.getFrame());
                return;
            }
        }
        new SkinDialog(window, curves);
    }

    static final class CompoundUndoableEdit implements UndoableEdit {

        private final LinkedList<UndoableEdit> undo = new LinkedList<>();
        private final LinkedList<UndoableEdit> redo = new LinkedList<>();

        public CompoundUndoableEdit(UndoableEdit edit) {
            this.add(edit);
        }
        public void add(UndoableEdit edit) {
            undo.add(edit.execute());
        }

        @Override
        public void undo() {
            undo.descendingIterator().forEachRemaining(edit -> {edit.undo(); redo.addFirst(edit);});
        }

        @Override
        public void redo() {
            redo.forEach(UndoableEdit::redo);
        }

        @Override
        public String getName() {
            return undo.isEmpty() ? UndoableEdit.super.getName() : undo.get(0).getName();
        }
    }

    static final class ObjectAddUndoableEdit implements UndoableEdit {

        @Override
        public void undo() {
            //Not yet implemented
        }

        @Override
        public void redo() {
            //Not yet implemented

        }

        @Override
        public String getName() {
            return "Skin";
        }
    }

    final class SelectionChangedUndoableEdit implements UndoableEdit {
        private final LayoutWindow layout;
        private final int[] oldSelection;
        private final int[] newSelection;

        SelectionChangedUndoableEdit(LayoutWindow window, int... newSelection) {
            layout = window;
            this.oldSelection = window.getSelectedIndices();
            this.newSelection = newSelection;
        }

        @Override
        public void undo() {
            layout.setSelection(oldSelection);
        }

        @Override
        public void redo() {
            layout.setSelection(newSelection);
        }

    }
}
