/* Copyright (C) 2004 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation.distortion;

import artofillusion.animation.*;
import artofillusion.object.*;
import java.io.*;

/**
 * This class is a keyframe for a SkeletonShapeTrack.
 */
public class SkeletonShapeKeyframe implements Keyframe {

    private final Object3D object;
    private final Skeleton skeleton;

    public SkeletonShapeKeyframe(Object3D owner, Skeleton s) {
        object = owner;
        skeleton = s;
    }

    /**
     * Get the object to which this keyframe belongs.
     */
    public Object3D getObject() {
        return object;
    }

    /**
     * Get the skeleton for this keyframe.
     */
    public Skeleton getSkeleton() {
        return skeleton;
    }

    @Override
    public Keyframe duplicate() {
        return new SkeletonShapeKeyframe(object, skeleton.duplicate());
    }

    @Override
    public Keyframe duplicate(Object owner) {
        return new SkeletonShapeKeyframe((Object3D) owner, skeleton.duplicate());
    }

    @Override
    public Keyframe blend(Keyframe o2, double weight1, double weight2) {
        return blend(new Keyframe[]{this, o2}, new double[]{weight1, weight2});
    }

    @Override
    public Keyframe blend(Keyframe o2, Keyframe o3, double weight1, double weight2, double weight3) {
        return blend(new Keyframe[]{this, o2, o3}, new double[]{weight1, weight2, weight3});
    }

    @Override
    public Keyframe blend(Keyframe o2, Keyframe o3, Keyframe o4, double weight1, double weight2, double weight3, double weight4) {
        return blend(new Keyframe[]{this, o2, o3, o4}, new double[]{weight1, weight2, weight3, weight4});
    }

    private Keyframe blend(Keyframe[] key, double[] weight) {
        Actor actor = Actor.getActor(object);
        Skeleton base;
        if (actor != null) {
            base = actor.getGesture(0).getSkeleton();
        } else {
            base = object.getSkeleton();
        }
        Skeleton[] toBlend = new Skeleton[key.length];
        for (int i = 0; i < key.length; i++) {
            toBlend[i] = ((SkeletonShapeKeyframe) key[i]).skeleton;
        }
        Skeleton average = base.duplicate();
        base.blend(average, toBlend, weight);
        return new SkeletonShapeKeyframe(object, average);
    }

    /**
     * Determine whether this keyframe is identical to another one.
     */
    @Override
    public boolean equals(Keyframe k) {
        if (!(k instanceof SkeletonShapeKeyframe)) {
            return false;
        }
        return skeleton.equals(((SkeletonShapeKeyframe) k).skeleton);
    }

    /**
     * Write out a representation of this keyframe to a stream.
     */
    @Override
    public void writeToStream(DataOutputStream out) throws IOException {
        out.writeShort(0); // version
        skeleton.writeToStream(out);
    }

    /**
     * Reconstructs the keyframe from its serialized representation.
     */
    public SkeletonShapeKeyframe(DataInputStream in, Object parent) throws IOException {
        short version = in.readShort();
        if (version != 0) {
            throw new InvalidObjectException("");
        }
        object = (Object3D) parent;
        skeleton = new Skeleton(in);
    }
}
