/* This class stores the smoothness information for a keyframe.  There is both a "left"
   and a "right" smoothness value, which can optionally be locked to force them to be
   the same. */

 /* Copyright (C) 2001 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.animation;

import java.io.*;

public class Smoothness {

    private double s1, s2;
    private boolean same;

    public Smoothness() {
        s1 = s2 = 1.0;
        same = true;
    }

    public Smoothness(double s) {
        s1 = s2 = s;
        same = true;
    }

    /* Create a duplicate of this  object. */
    public Smoothness duplicate() {
        Smoothness sm = new Smoothness();
        sm.s1 = s1;
        sm.s2 = s2;
        sm.same = same;
        return sm;
    }

    /* Get the left-hand smoothness. */
    public double getLeftSmoothness() {
        return s1;
    }

    /* Get the right-hand smoothness. */
    public double getRightSmoothness() {
        return s2;
    }

    /* Set the smoothness to a single value. */
    public void setSmoothness(double s) {
        s1 = s2 = s;
        same = true;
    }

    /* Set different smoothness values for the left and right sides. */
    public void setSmoothness(double left, double right) {
        s1 = left;
        s2 = right;
        same = false;
    }

    /* Determine whether the two smoothness values have been locked to be the same. */
    public boolean isForceSame() {
        return same;
    }

    /* Get a new Smoothness object which whose values are twice those of this object. */
    public Smoothness getSmoother() {
        Smoothness sm = new Smoothness();
        sm.s1 = Math.min(s1 * 2.0, 1.0);
        sm.s2 = (same ? s1 : Math.min(s2 * 2.0, 1.0));
        sm.same = same;
        return sm;
    }

    /* Write a serialized representation of this object to a stream. */
    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeDouble(s1);
        if (same) {
            out.writeDouble(Double.NaN);
        } else {
            out.writeDouble(s2);
        }
    }

    /* Reconstruct a Smoothness object from its serialized representation. */
    public Smoothness(DataInputStream in) throws IOException {
        s1 = in.readDouble();
        s2 = in.readDouble();
        same = Double.isNaN(s2);
        if (same) {
            s2 = s1;
        }
    }
}
