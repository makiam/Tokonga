/* Copyright (C) 2016-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 *
 * @author makiam
 *
 */
@Suite
@SuiteDisplayName("Animation Suite")
@SelectClasses({ActorTest.class, IKTrackTest.class, JointEqualityTest.class, JointDOFEqualityTest.class,
        PoseTrackTest.class, ScoreTest.class, TextureTrackTest.class, TimecourseTest.class,
        TrackGraphTest.class, TrackQuantizeTest.class, VisibilityTrackTest.class, WeightTrackTest.class, TrackRestoreTest.class})
public class AnimationSuite {
}
