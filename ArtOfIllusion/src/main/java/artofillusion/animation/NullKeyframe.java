/* Copyright (C) 2001-2002 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import java.io.*;

/**
 * This is a keyframes which contains no information. It is occasionally useful as a
 * placeholder.
 */
public class NullKeyframe implements Keyframe<NullKeyframe> {

    public NullKeyframe() {
    }

    /* Create a duplicate of this keyframe. */
    @Override
    public NullKeyframe duplicate() {
        return new NullKeyframe();
    }

    /* Create a duplicate of this keyframe for a (possibly different) object. */
    @Override
    public NullKeyframe duplicate(Object owner) {
        return new NullKeyframe();
    }

    /* These methods return a new Keyframe which is a weighted average of this one and one,
     two, or three others. */
    @Override
    public NullKeyframe blend(NullKeyframe o2, double weight1, double weight2) {
        return new NullKeyframe();
    }

    @Override
    public NullKeyframe blend(NullKeyframe o2, NullKeyframe o3, double weight1, double weight2, double weight3) {
        return new NullKeyframe();
    }

    @Override
    public NullKeyframe blend(NullKeyframe o2, NullKeyframe o3, NullKeyframe o4, double weight1, double weight2, double weight3, double weight4) {
        return new NullKeyframe();
    }

    /* Determine whether this keyframe is identical to another one. */
    @Override
    public boolean equals(Keyframe k) {
        return (k instanceof NullKeyframe);
    }

    /* Write out a representation of this keyframe to a stream. */
    @Override
    public void writeToStream(DataOutputStream out) throws IOException {
    }

    /* Reconstructs the keyframe from its serialized representation. */
    public NullKeyframe(DataInputStream in, Object parent) throws IOException {
    }
}
