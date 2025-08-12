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
 * This interface represents any object which can be used to represent a keyframe on an
 * animation track.
 * <p>
 * Every Keyframe class should also provide a constructor of the following form, which
 * reconstructs the keyframe from its serialized representation.
 * public KeyframeClass(DataInputStream in, Object parent) throws IOException, InvalidObjectException
 */
public interface Keyframe<K extends Keyframe<K>> {

    /**
     * Create a duplicate of this keyframe.
     */
    K duplicate();

    /**
     * Create a duplicate of this keyframe for a (possibly different) object.
     */
    K duplicate(Object owner);

    /**
     * Get the list of graphable values for this keyframe.
     */
    default double[] getGraphValues() {
        return new double[0];
    }

    /**
     * Set the list of graphable values for this keyframe.
     */
    default void setGraphValues(double[] values) {
    }

    /**
     * Return a new Keyframe which is a weighted average of this one and one other.
     */
    K blend(K o2, double weight1, double weight2);

    /**
     * Return a new Keyframe which is a weighted average of this one and two others.
     */
    K blend(K o2, K o3, double weight1, double weight2, double weight3);

    /**
     * Return a new Keyframe which is a weighted average of this one and three others.
     */
    K blend(K o2, K o3, K o4, double weight1, double weight2, double weight3, double weight4);

    /**
     * Determine whether this keyframe is identical to another one.
     */
    boolean equals(Keyframe k);

    /**
     * Write out a representation of this keyframe to a stream.
     */
    void writeToStream(DataOutputStream out) throws IOException;
}
