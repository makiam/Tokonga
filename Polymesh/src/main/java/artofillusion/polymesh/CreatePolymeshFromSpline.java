/*
   Copyright (C) 2025-2026 Maksim Khramov
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
import artofillusion.object.SplineMesh;

public final class CreatePolymeshFromSpline implements UndoableEdit {
    private final LayoutWindow layout;
    private final ObjectInfo source;
    private ObjectInfo item;

    public CreatePolymeshFromSpline(LayoutWindow layout, ObjectInfo source) {
        this.layout = layout;
        this.source = source;
    }

    @Override
    public void undo() {
        var ii = layout.getScene().getObjects().indexOf(item);
        if(ii == -1) return;
        layout.removeObject(ii, null);
    }

    @Override
    public void redo() {
        var pm = new PolyMesh((SplineMesh) source.getGeometry());
        item = new ObjectInfo(pm, source.getCoordinateSystem().duplicate(), "Polymesh" + source.getName());
        layout.addObject(item, null);
    }

    @Override
    public String getName() {
        return "Convert to Polymesh";
    }
}
