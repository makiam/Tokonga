/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.animation.*;
import artofillusion.animation.distortion.*;
import artofillusion.ui.Translate;
import buoy.widget.BMenu;

import java.util.HashMap;
import java.util.Map;

public class LayoutAnimationMenu extends BMenu {

    private static final Map<String, Class<? extends Track<?>>> commandToTrack = new HashMap<>();
    static {
        commandToTrack.put("poseTrack", PoseTrack.class);
        commandToTrack.put("constraintTrack", ConstraintTrack.class);
        commandToTrack.put("visibilityTrack", VisibilityTrack.class);
        commandToTrack.put("textureTrack", TextureTrack.class);
        commandToTrack.put("bendDistortion", BendTrack.class);
        commandToTrack.put("customDistortion", CustomDistortionTrack.class);
        commandToTrack.put("scaleDistortion", ScaleTrack.class);
        commandToTrack.put("shatterDistortion", ShatterTrack.class);
        commandToTrack.put("twistDistortion", TwistTrack.class);
        commandToTrack.put("IKTrack", IKTrack.class);
        commandToTrack.put("skeletonShapeTrack", SkeletonShapeTrack.class);
    }

    LayoutAnimationMenu(LayoutWindow layout) {
        super(Translate.text("menu.animation"));
    }

    Class<? extends Track> getCommandToTrack(String command) {
        return commandToTrack.get(command);
    }

}
