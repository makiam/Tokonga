/* Copyright (C) 2004-2012 by Peter Eastman
   Changes copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation.distortion;

import artofillusion.animation.*;
import artofillusion.object.*;

/**
 * This is a distortion which applies a pose to an object.
 */
public class PoseDistortion extends Distortion {

    private final double weight;
    private final Keyframe pose;
    private final Actor actor;
    private final boolean relative;

    public PoseDistortion(double weight, Keyframe pose, Actor actor, boolean relative) {
        this.weight = weight;
        this.pose = pose;
        this.actor = actor;
        this.relative = relative;
    }

    /**
     * Determine whether this distortion is identical to another one.
     */
    @Override
    public boolean isIdenticalTo(Distortion d) {
        if (!(d instanceof PoseDistortion)) {
            return false;
        }
        PoseDistortion s = (PoseDistortion) d;
        if (previous != null && !previous.isIdenticalTo(s.previous)) {
            return false;
        }
        if (previous == null && s.previous != null) {
            return false;
        }
        if (weight != s.weight) {
            return false;
        }
        return pose.equals(s.pose);
    }

    /**
     * Create a duplicate of this object.
     */
    @Override
    public Distortion duplicate() {
        PoseDistortion d = new PoseDistortion(weight, pose.duplicate(), actor, relative);
        if (previous != null) {
            d.previous = previous.duplicate();
        }
        return d;
    }

    /**
     * Apply the Distortion, and return a transformed mesh.
     */
    @Override
    public Mesh transform(Mesh obj) {
        if (previous != null) {
            obj = previous.transform(obj);
        }
        try {
            Keyframe toApply = pose;
            if (actor != null && pose instanceof Actor.ActorKeyframe) {
                Gesture base = actor.getGesture(0);
                Gesture current = (Gesture) ((Object3D) obj).getPoseKeyframe();
                Gesture poseGesture = (Gesture) ((Actor.ActorKeyframe) pose).createObjectKeyframe(actor);
                if (relative) {
                    toApply = base.blend(new Gesture[]{current, poseGesture}, new double[]{1.0, weight});
                } else if (weight < 1.0) {
                    toApply = base.blend(new Gesture[]{current, poseGesture}, new double[]{1.0 - weight, weight});
                } else {
                    toApply = poseGesture;
                }
            } else {
                Keyframe base = ((Object3D) obj).getPoseKeyframe();
                toApply = pose;
                if (relative) {
                    toApply = base.blend(toApply, 1.0, weight);
                } else if (weight < 1.0) {
                    toApply = base.blend(toApply, 1.0 - weight, weight);
                }
            }
            Object3D newmesh = obj.duplicate();
            newmesh.applyPoseKeyframe(toApply);
            return (Mesh) newmesh;
        } catch (ClassCastException ex) {
            // The distorted object is no longer the same class as the original object, so that the
            // pose can no longer be applied to it.  There is nothing we can do about this, so just
            // ignore it.

            return obj;
        }
    }
}
