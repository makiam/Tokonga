/* Copyright (C) 2024 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.animation.ProceduralRotationTrack;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.Translate;

public class ProceduralRotationTrackProvider implements TrackProvider {
    @Override
    public String getCategory() {
        return Translate.text("menu.rotationTrack");
    }

    @Override
    public String getName() {
        return Translate.text("menu.proceduralTrack");
    }

    @Override
    public void forEach(ObjectInfo item, UndoRecord undo) {
        TrackProvider.add(item, new ProceduralRotationTrack(item), undo);
    }
}
