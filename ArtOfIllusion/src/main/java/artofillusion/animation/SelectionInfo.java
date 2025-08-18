/* This class stores information about a selected keyframe. */

 /* Copyright (C) 2001 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.animation;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public final class SelectionInfo {

    public final Track<?> track;
    public final Keyframe key;
    public int keyIndex = -1;
    public final boolean[] selected;


    public SelectionInfo(Track<?> tr, Keyframe k) {
        track = tr;
        key = k;

        selected = new boolean[track.getValueNames().length];

        Timecourse<?> tc = track.getTimecourse();
        if (tc == null) {
            return;
        }
        Keyframe[] keys = tc.getValues();
        int i;
        for (i = 0; keys[i] != key && i < keys.length; i++);
        if (i < keys.length) {
            keyIndex = i;
        }
        Arrays.fill(selected, true);
    }

    public int getIndex() {
        return keyIndex;
    }

}
