/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.TextureParameter;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.NullObject;
import artofillusion.object.ObjectInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Texture Track Test")
class TextureTrackTest {

    @Test
    @DisplayName("Create texture track with all default data")
    public void testCreateTrackNoParamsSet() {
        var oi = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null Object");
        var track = new TextureTrack(oi);
        Assertions.assertEquals(Timecourse.INTERPOLATING, track.getSmoothingMethod());
        Assertions.assertNull(track.param);
        Assertions.assertNotNull(track.theWeight);
        Assertions.assertNotNull(track.getTimecourse());

        Assertions.assertEquals(oi, track.getParent());
        Assertions.assertEquals(1, track.getSubtracks().length);

    }

    @Test
    @DisplayName("Create texture track with empty params data")
    public void testCreateTrackEmptyParamsSet() {
        var no = new NullObject();
        no.setParameters(new TextureParameter[0]);
        var oi = new ObjectInfo(no, new CoordinateSystem(), "Null Object");
        var track = new TextureTrack(oi);
        Assertions.assertEquals(Timecourse.INTERPOLATING, track.getSmoothingMethod());
        Assertions.assertNotNull(track.param);
        Assertions.assertNotNull(track.theWeight);
        Assertions.assertNotNull(track.getTimecourse());

        Assertions.assertEquals(oi, track.getParent());
        Assertions.assertEquals(1, track.getSubtracks().length);

    }

    @Test
    @DisplayName("Create texture track via duplicate existed one")
    public void testDuplicateTrack() {
        var no = new NullObject();
        no.setParameters(new TextureParameter[0]);
        var oi = new ObjectInfo(no, new CoordinateSystem(), "Source Null Object");
        var track = new TextureTrack(oi);
        track.setSmoothingMethod(Timecourse.APPROXIMATING);
        var co = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Target Null Object");
        var dup = track.duplicate(co);

        Assertions.assertEquals(track.getSmoothingMethod(), dup.getSmoothingMethod());
    }

    @Test
    @DisplayName("Create texture track and set data from other")
    public void testCopyTrack() {
        var no = new NullObject();
        no.setParameters(new TextureParameter[0]);
        var oi = new ObjectInfo(no, new CoordinateSystem(), "Source Null Object");
        var track = new TextureTrack(oi);
        track.setSmoothingMethod(Timecourse.APPROXIMATING);
        var co = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Target Null Object");

        var copyDataTrack = new TextureTrack(co);
        copyDataTrack.copy(track);

        Assertions.assertEquals(track.getSmoothingMethod(), copyDataTrack.getSmoothingMethod());
    }
}
