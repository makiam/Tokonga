/* Copyright (C) 2024 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.animation.PoseTrack;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.Translate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PoseTrackProvider implements TrackProvider {

    @Override
    public String getName() {
        return Translate.text("menu.poseTrack");
    }


    @Override
    public void create(List<ObjectInfo> objects, UndoRecord undo) {
        objects.removeIf(PoseTrackProvider::isNotPosable);        
        Map<Boolean, List<ObjectInfo>> pon = objects.stream().collect(Collectors.partitioningBy(PoseTrackProvider::isActor));
        pon.get(true).forEach(item -> forEach(item, undo));
            
        List<ObjectInfo> itemsToConvert = pon.get(false);
        if(itemsToConvert.isEmpty()) return;
        
        ConvertObjectDialog convert = new ConvertObjectDialog(itemsToConvert);
        convert.setVisible(true);
        if(convert.getReturnStatus() == ConvertObjectDialog.RET_CANCEL) return;
        
        boolean[] flags = convert.getConvertFlags();
        IntStream.range(0, flags.length).forEach(index -> {
            if(flags[index]) {
                ObjectInfo info = itemsToConvert.get(index);
                //undo.addCommand(UndoRecord.SET_OBJECT, .getObject().getPosableObject());
            }
        });
    }

    @Override
    public void forEach(ObjectInfo item, UndoRecord undo) {
        TrackProvider.add(item, new PoseTrack(item), undo);
    }

    /**
     * Check scene ObjectInfo posable.
     *
     * @param  info	object to check
     * @return  true if the object is posable
     */
    private static boolean isNotPosable(ObjectInfo info) {
        return info.getObject().getPosableObject() == null;
    }

    private static boolean isActor(ObjectInfo info) {
        return info.getObject().equals(info.getObject().getPosableObject());
    }
}
