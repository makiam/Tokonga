/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion.animation;

import artofillusion.animation.distortion.*;
import artofillusion.api.ImplementationVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TrackVersionTest {

    @ParameterizedTest
    @MethodSource("getArgs")
    public void testTrackClassImplementationVersion(Class<?> clazz, short version) {
        var anno = clazz.getAnnotation(ImplementationVersion.class);
        Assertions.assertNotNull(anno);
        Assertions.assertEquals(version, anno.current());
    }

    static Stream<Arguments> getArgs() {
        List<Arguments> args = new ArrayList<>();
        args.add(Arguments.of(PositionTrack.class, (short)1));
        args.add(Arguments.of(RotationTrack.class, (short)1));
        args.add(Arguments.of(ProceduralPositionTrack.class, (short)1));
        args.add(Arguments.of(ProceduralPositionTrack.class, (short)1));
        args.add(Arguments.of(ScaleTrack.class, (short)0));
        args.add(Arguments.of(ConstraintTrack.class, (short)0));
        args.add(Arguments.of(PoseTrack.class, (short)2));
        args.add(Arguments.of(BendTrack.class, (short)0));
        args.add(Arguments.of(CustomDistortionTrack.class, (short)0));
        args.add(Arguments.of(WeightTrack.class, (short)0));
        args.add(Arguments.of(ShatterTrack.class, (short)0));
        args.add(Arguments.of(SkeletonShapeTrack.class, (short)0));
        args.add(Arguments.of(TwistTrack.class, (short)0));
        args.add(Arguments.of(IKTrack.class, (short)1));
        args.add(Arguments.of(TextureTrack.class, (short)0));
        args.add(Arguments.of(FilterParameterTrack.class, (short)0));
        args.add(Arguments.of(ConstraintTrack.class, (short)0));
        return args.stream();
    }
}

