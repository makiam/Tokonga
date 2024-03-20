/* Copyright (C) 2024 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.animation.PositionTrack;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.Translate;

/**
 *
 * @author MaksK
 */
public class ThreeAxisPositionTrackProvider implements TrackProvider {

    @Override
    public String getCategory() {
        return Translate.text("menu.positionTrack");
    }

    @Override
    public String getName() {
        return Translate.text("menu.xyzThreeTracks");
    }

    @Override
    public void forEach(ObjectInfo item, UndoRecord undo) {
        var x = new PositionTrack(item, Translate.text("menu.xTrack"), true, false, false);
        var y = new PositionTrack(item, Translate.text("menu.yTrack"), false, true, false);
        var z = new PositionTrack(item, Translate.text("menu.zTrack"), false, false, false);


        TrackProvider.add(item, z, undo);
        TrackProvider.add(item, y, undo);
        TrackProvider.add(item, x, undo);
    }
    
}
