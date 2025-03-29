/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.animation.distortion.*;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.DummyImageFilter;

import artofillusion.object.ObjectInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TrackQuantizeTest {

    @ParameterizedTest
    @MethodSource("getTracks")
    public void testTrackQuantized(Track<?> test) {
        Assertions.assertTrue(test.isQuantized());
        var dup = test.duplicate(test.getParent());
        Assertions.assertTrue(dup.isQuantized());
    }

    static Stream<Track<?>> getTracks() {
        List<Track<?>> tracks = new ArrayList<>();

        ObjectInfo oi = new ObjectInfo(new Cube(1, 1, 1), new CoordinateSystem(), "Cube");
        tracks.add(new PositionTrack(oi));
        tracks.add(new RotationTrack(oi));
        tracks.add(new ProceduralPositionTrack(oi));
        tracks.add(new ProceduralRotationTrack(oi));
        tracks.add(new ScaleTrack(oi));
        tracks.add(new ConstraintTrack(oi));
        tracks.add(new PoseTrack(oi));
        tracks.add(new BendTrack(oi));
        tracks.add(new CustomDistortionTrack(oi));
        tracks.add(new WeightTrack(new DummyTrack()));
        tracks.add(new ShatterTrack(oi));
        tracks.add(new SkeletonShapeTrack(oi));
        tracks.add(new TwistTrack(oi));
        tracks.add(new IKTrack(oi));
        tracks.add(new TextureTrack(oi));
        tracks.add(new FilterParameterTrack(oi, new DummyImageFilter()));
        tracks.add(new ConstraintTrack(oi));
        return tracks.stream();
    }


}
