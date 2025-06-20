/* Copyright (C) 2016 by Lucas Stanek
   Changes copyright (C) 2023-2025 by Maksim Khramov

 This program is free software; you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation; either version 2 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion.polymesh;

import artofillusion.ViewerCanvas;
import artofillusion.math.Mat4;
import artofillusion.math.Vec3;

/**
 * This is the base class manipulator responsible for moving, resizing and rotating mesh selections.
 * SSMR = Select Scale Move Rotate
 * Variants are 2D manipulator and 3D manipulator
 */
public abstract class SSMRManipulator extends Manipulator {

    public static final short SCALE = 0;
    public static final short ROTATE = 1;
    public static final short MOVE = 2;
    public static final short ABORT = 3;
    public static final short XAXIS = 0;
    public static final short YAXIS = 1;
    public static final short ZAXIS = 2;

    public static final short ANCHOR_LEFT = 0;
    public static final short ANCHOR_RIGHT = 1;
    public static final short ANCHOR_TOP = 2;
    public static final short ANCHOR_BOTTOM = 3;
    public static final short ANCHOR_CENTER = 4;

    public SSMRManipulator(AdvancedEditingTool tool, ViewerCanvas view, PolyMeshValueWidget valueWidget) {
        super(tool, view, valueWidget);
    }

    public static class ManipulatorScalingEvent extends ManipulatorEvent {

        private final Mat4 scaleMatrix;

        public ManipulatorScalingEvent(Manipulator manipulator, Mat4 matrix, ViewerCanvas view) {
            super(manipulator, SCALE, view);
            scaleMatrix = matrix;
        }

        public Mat4 getScaleMatrix() {
            return scaleMatrix;
        }
    }

    public static class ManipulatorRotatingEvent extends ManipulatorEvent {

        private final Mat4 mat;

        public ManipulatorRotatingEvent(Manipulator manipulator, Mat4 mat, ViewerCanvas view) {
            super(manipulator, view);
            this.mat = mat;
        }

        public Mat4 getMatrix() {
            return mat;
        }

    }

    public static class ManipulatorMovingEvent extends ManipulatorEvent {

        final Vec3 drag;

        public ManipulatorMovingEvent(Manipulator manipulator, Vec3 dr, ViewerCanvas view) {
            super(manipulator, view);
            drag = dr;
        }

        public Vec3 getDrag() {
            return drag;
        }
    }
}
