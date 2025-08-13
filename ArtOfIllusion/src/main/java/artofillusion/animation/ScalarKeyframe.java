/* Copyright (C) 2001-2002 by Peter Eastman
   Changes copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import java.io.*;

/**
 * This class is a scalar valued keyframe.
 */
public class ScalarKeyframe implements Keyframe<ScalarKeyframe> {

    public double val;

    public ScalarKeyframe(double d) {
        val = d;
    }

    @Override
    public ScalarKeyframe duplicate() {
        return new ScalarKeyframe(val);
    }

    @Override
    public ScalarKeyframe duplicate(Object owner) {
        return new ScalarKeyframe(val);
    }

    /**
     * Get the list of graphable values for this keyframe.
     */
    @Override
    public double[] getGraphValues() {
        return new double[]{val};
    }

    /**
     * Set the list of graphable values for this keyframe.
     */
    @Override
    public void setGraphValues(double[] values) {
        if (values.length == 1) {
            val = values[0];
        }
    }

    @Override
    public ScalarKeyframe blend(ScalarKeyframe o2, double weight1, double weight2) {
        return new ScalarKeyframe(weight1 * val + weight2 * o2.val);
    }

    @Override
    public ScalarKeyframe blend(ScalarKeyframe o2, ScalarKeyframe o3, double weight1, double weight2, double weight3) {
        return new ScalarKeyframe(weight1 * val + weight2 * o2.val + weight3 * o3.val);
    }

    @Override
    public ScalarKeyframe blend(ScalarKeyframe o2, ScalarKeyframe o3, ScalarKeyframe o4, double weight1, double weight2, double weight3, double weight4) {
        return new ScalarKeyframe(weight1 * val + weight2 * o2.val + weight3 * o3.val + weight4 * o4.val);
    }

    /**
     * Determine whether this keyframe is identical to another one.
     */
    @Override
    public boolean equals(Keyframe k) {
        if (!(k instanceof ScalarKeyframe)) {
            return false;
        }
        ScalarKeyframe key = (ScalarKeyframe) k;
        return (key.val == val);
    }

    /**
     * Write out a representation of this keyframe to a stream.
     */
    @Override
    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeDouble(val);
    }

    /**
     * Reconstructs the keyframe from its serialized representation.
     */
    @SuppressWarnings("java:S1172")
    public ScalarKeyframe(DataInputStream in, Object parent) throws IOException {
        val = in.readDouble();
    }
}
