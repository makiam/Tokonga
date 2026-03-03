/* Copyright (C) 2002-2012 by Peter Eastman
    Changes copyright (C) 2023-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation.distortion;

import artofillusion.math.*;
import artofillusion.object.*;

/**
 * This is a distortion which twists an object.
 */
public class TwistDistortion extends Distortion {

    private final int axis;
    private final double angle;
    private final Mat4 preTransform;
    private final Mat4 postTransform;
    private final boolean forward;

    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;

    public TwistDistortion(int axis, double angle, boolean forward, Mat4 preTransform, Mat4 postTransform) {
        this.axis = axis;
        this.angle = angle;
        this.forward = forward;
        this.preTransform = preTransform;
        this.postTransform = postTransform;
    }

    /**
     * Determine whether this distortion is identical to another one.
     */
    @Override
    public boolean isIdenticalTo(Distortion d) {
        if (!(d instanceof TwistDistortion)) {
            return false;
        }
        TwistDistortion s = (TwistDistortion) d;
        if (previous != null && !previous.isIdenticalTo(s.previous)) {
            return false;
        }
        if (previous == null && s.previous != null) {
            return false;
        }
        if (axis != s.axis || angle != s.angle || forward != s.forward) {
            return false;
        }
        if (preTransform == s.preTransform && postTransform == s.postTransform) {
            return true;
        }
        return (preTransform != null && preTransform.equals(s.preTransform)
                && postTransform != null && postTransform.equals(s.postTransform));
    }

    /**
     * Create a duplicate of this object.
     */
    @Override
    public Distortion duplicate() {
        TwistDistortion d = new TwistDistortion(axis, angle, forward, preTransform, postTransform);
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
        Mesh newmesh = (Mesh) obj.duplicate();
        MeshVertex[] vert = newmesh.getVertices();
        Vec3[] newvert = new Vec3[vert.length];

        for (int i = 0; i < newvert.length; i++) {
            newvert[i] = vert[i].r;
            if (preTransform != null) {
                preTransform.transform(newvert[i]);
            }
        }

        // Find the range along the appropriate axis.
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for(var vertex: newvert) {
            double value;
            if (axis == X_AXIS) {
                value = vertex.x;
            } else if (axis == Y_AXIS) {
                value = vertex.y;
            } else {
                value = vertex.z;
            }
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        if (min >= max) {
            return obj;
        }
        if (!forward) {
            double temp = min;
            min = max;
            max = temp;
        }
        double scale = angle * (Math.PI / 180.0);
        if (axis == X_AXIS) {
            for(var vertex: newvert) {
                double c = Math.cos(scale * (vertex.x - min));
                double s = Math.sin(scale * (vertex.x - min));
                vertex.set(vertex.x, vertex.y * c - vertex.z * s, vertex.y * s + vertex.z * c);
            }
        } else if (axis == Y_AXIS) {
            for(var vertex: newvert) {
                double c = Math.cos(scale * (vertex.y - min));
                double s = Math.sin(scale * (vertex.y - min));
                vertex.set(vertex.x * c - vertex.z * s, vertex.y, vertex.x * s + vertex.z * c);
            }
        } else {
            for(var vertex: newvert) {
                double c = Math.cos(scale * (vertex.z - min));
                double s = Math.sin(scale * (vertex.z - min));
                vertex.set(vertex.x * c - vertex.y * s, vertex.x * s + vertex.y * c, vertex.z);
            }
        }
        if (postTransform != null) {
            for(var vertex: newvert) postTransform.transform(vertex);
        }
        newmesh.setVertexPositions(newvert);
        return newmesh;
    }
}
