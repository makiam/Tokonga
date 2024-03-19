/* Copyright (C) 2024 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.animation.Track;
import artofillusion.animation.VisibilityTrack;
import artofillusion.object.ObjectInfo;

import java.util.List;


public interface TrackProvider {
    default String getCategory() {
        return "";
    }

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default void create(List<ObjectInfo> objects, UndoRecord undo) {
        objects.forEach(item -> {
            forEach(item, undo);
        });        
    }
    
    static void add(ObjectInfo item, Track track, UndoRecord undo) {
        undo.addCommand(UndoRecord.SET_TRACK_LIST, item, item.getTracks());
        item.addTrack(track, 0);        
    }
    
    void forEach(ObjectInfo item, UndoRecord undo);
}
