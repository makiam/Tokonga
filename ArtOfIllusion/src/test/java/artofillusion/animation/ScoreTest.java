/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.CompoundUndoableEdit;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.ObjectInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

public class ScoreTest {

    @Test
    public void testScoreSelectionFilterAllGood() {
        Object[] input = new Object[] {new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test"), new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test"), new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test")};

        Assertions.assertEquals(3, Score.filterTargets(input).size());
    }

    public void testScoreSelectionFilterAllBad() {
        Object[] input = new Object[] {"String", "String", "String"};
        Assertions.assertEquals(0, Score.filterTargets(input).size());
    }

    @Test
    public void testScoreSelectionFilterOneGood() {
        Object[] input = new Object[] {1, new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test"), "String", "String", "String"};
        Assertions.assertEquals(1, Score.filterTargets(input).size());
    }

    @Test
    public void testGetTracks() {
        var input = new Object[] {new DummyTrack(), new DummyTrack(), new DummyTrack()};

        Stream<Track<?>> result = Arrays.stream(input).map(Track.class::cast);

        result.forEach(System.out::println);
    }

    @Test
    public void testGetTrackOwner() {
        var oi = new ObjectInfo(new Cube(1d,1d,1d), new CoordinateSystem(), "Test");
        DummyParentedWeightedTrack dwt = new DummyParentedWeightedTrack(oi);

        Assertions.assertEquals(oi, Score.getTrackOwner(dwt.getWeightTrack()));
    }

    @Test
    public void testToggleTrack() {
        Score.TrackDisableAction ta = new Score.TrackDisableAction(new DummyTrack());
        CompoundUndoableEdit cue = new CompoundUndoableEdit();
        cue.add(ta);
        cue.execute();
    }
}
