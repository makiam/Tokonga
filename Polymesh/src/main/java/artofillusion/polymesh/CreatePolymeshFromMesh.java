/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.LayoutWindow;
import artofillusion.UndoableEdit;
import artofillusion.object.ObjectInfo;
import artofillusion.object.TriangleMesh;

public final class CreatePolymeshFromMesh implements UndoableEdit {
    private final LayoutWindow layout;
    private final ObjectInfo source;
    private ObjectInfo item;
    private final int mode;

    public CreatePolymeshFromMesh(LayoutWindow layout, ObjectInfo source, int response) {
        this.layout = layout;
        this.source = source;
        this.mode = response;
    }

    @Override
    public void undo() {
        var ii = layout.getScene().getObjects().indexOf(item);
        if(ii == -1) return;
        layout.removeObject(ii, null);
    }

    @Override
    public void redo() {
        var pm = new PolyMesh((TriangleMesh) source.getObject(), mode == 0 || mode == 1, mode == 1);
        item = new ObjectInfo(pm, source.getCoordinateSystem().duplicate(), "Polymesh " + source.getName());
        layout.addObject(item, null);
    }

    @Override
    public String getName() {
        return "Convert to Polymesh";
    }
}
