/* Copyright (C) 2024 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.animation.RotationTrack;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.Translate;

/**
 *
 * @author MaksK
 */
public class ThreeAxisRotationTrackProvider implements TrackProvider {

    @Override
    public String getCategory() {
        return Translate.text("menu.rotationTrack");
    }

    @Override
    public String getName() {
        return Translate.text("menu.xyzThreeTracks");
    }

    @Override
    public void forEach(ObjectInfo item, UndoRecord undo) {
        var x = new RotationTrack(item, Translate.text("Heading"), false, true, false, false);
        var y = new RotationTrack(item, Translate.text("Pitch"), false, false, true, false);
        var z = new RotationTrack(item, Translate.text("Bank"), false, false, false, false);


        TrackProvider.add(item, z, undo);
        TrackProvider.add(item, y, undo);
        TrackProvider.add(item, x, undo);
    }
    
}
