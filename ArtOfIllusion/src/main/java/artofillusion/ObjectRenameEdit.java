/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

/**
 *
 * @author MaksK
 */
public class ObjectRenameEdit implements UndoableEdit {

    private final LayoutWindow layout;
    private final String newName;
    private final String oldName;
    private final int which;

    public ObjectRenameEdit(LayoutWindow window, int which, String newName) {
        this.layout = window;
        this.newName = newName;
        this.which = which;
        this.oldName = window.getScene().getObject(which).getName();
    }

    @Override
    public void undo() {
        layout.setObjectName(which, oldName);
    }

    @Override
    public void redo() {
        layout.setObjectName(which, newName);
    }

    @Override
    public String getName() {
        return "Rename Object";
    }
}
