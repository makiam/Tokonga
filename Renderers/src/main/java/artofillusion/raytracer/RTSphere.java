/* Copyright (C) 1999-2013 by Peter Eastman
   Editions copyright (C) by Petri Ihalainen 2020

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.raytracer;

import artofillusion.material.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;

/**
 * RTSphere represents a sphere to be raytraced. It is defined by specifying a Sphere
 * object, and the transformations to and from local coordinates. It must be a true
 * sphere, not an ellipsoid. That is, all of its radii must be equal.
 */
public class RTSphere extends RTObject {

    final Sphere theSphere;
    final double r;
    final double r2;
    final double cx;
    final double cy;
    final double cz;
    final double[] param;
    final boolean bumpMapped;
    final Mat4 toLocal;
    Mat4 fromLocal;

    public static final double TOL = 1e-12;

    private double sphereTol;

    public RTSphere(Sphere sphere, Mat4 fromLocal, Mat4 toLocal, double[] param) {
        theSphere = sphere;
        this.param = param;
        cx = fromLocal.m14 / fromLocal.m44;
        cy = fromLocal.m24 / fromLocal.m44;
        cz = fromLocal.m34 / fromLocal.m44;
        r = sphere.getRadii().x;
        r2 = r * r;
        bumpMapped = sphere.getTexture().hasComponent(Texture.BUMP_COMPONENT);
        if (bumpMapped) {
            this.fromLocal = fromLocal;
        }
        this.toLocal = toLocal;
        sphereTol = Math.max(Math.max(Math.abs(fromLocal.m14), Math.abs(fromLocal.m24)), Math.abs(fromLocal.m34)) + r;
        sphereTol = Math.max(sphereTol, r2) * TOL;
    }

    /**
     * Get the TextureMapping for this object.
     */
    @Override
    public final TextureMapping getTextureMapping() {
        return theSphere.getTextureMapping();
    }

    /**
     * Get the MaterialMapping for this object.
     */
    @Override
    public final MaterialMapping getMaterialMapping() {
        return theSphere.getMaterialMapping();
    }

    /**
     * Determine whether the given ray intersects this sphere.
     */
    @Override
    public SurfaceIntersection checkIntersection(Ray r) {
        Vec3 orig = r.getOrigin(), dir = r.getDirection();
        Vec3 v1 = r.tempVec1, v2 = r.tempVec2;
        double b, c, d, root, t, t2 = 0.0;
        int numIntersections;

        v1.set(cx - orig.x, cy - orig.y, cz - orig.z);
        b = dir.x * v1.x + dir.y * v1.y + dir.z * v1.z;
        c = v1.x * v1.x + v1.y * v1.y + v1.z * v1.z - r2;
        if (c > sphereTol) {
            // Ray origin is outside sphere.

            if (b <= 0.0) {
                return SurfaceIntersection.NO_INTERSECTION;  // Ray points away from center of sphere.
            }
            d = b * b - c;
            if (d < 0.0) {
                return SurfaceIntersection.NO_INTERSECTION;
            }
            numIntersections = 2;
            root = Math.sqrt(d);
            t = b - root;
            t2 = b + root;
            v2.set(orig.x + t2 * dir.x, orig.y + t2 * dir.y, orig.z + t2 * dir.z);
            projectPoint(v2);
        } else if (c < -sphereTol) {
            // Ray origin is inside sphere.

            d = b * b - c;
            if (d < 0.0) {
                return SurfaceIntersection.NO_INTERSECTION;
            }
            numIntersections = 1;
            t = b + Math.sqrt(d);
        } else {
            // Ray origin is on the surface of the sphere.

            if (b <= 0.0) {
                return SurfaceIntersection.NO_INTERSECTION;  // Ray points away from center of sphere.
            }
            d = b * b - c;
            if (d < 0.0) {
                return SurfaceIntersection.NO_INTERSECTION;
            }
            numIntersections = 1;
            t = b + Math.sqrt(d);
        }
        v1.set(orig.x + t * dir.x, orig.y + t * dir.y, orig.z + t * dir.z);
        projectPoint(v1);
        return new SphereIntersection(this, numIntersections, v1, v2, t, t2);
    }

    /**
     * Given a point, project it onto the surface of the sphere. This is necessary to
     * prevent roundoff error.
     */
    private void projectPoint(Vec3 pos) {
        double dx = pos.x - cx, dy = pos.y - cy, dz = pos.z - cz;
        double scale = r / Math.sqrt(dx * dx + dy * dy + dz * dz);
        pos.set(cx + dx * scale, cy + dy * scale, cz + dz * scale);
    }

    /**
     * Get a bounding box for this sphere.
     */
    @Override
    public BoundingBox getBounds() {
        return new BoundingBox(cx - r, cx + r, cy - r, cy + r, cz - r, cz + r);
    }

    /**
     * Determine whether any part of the surface of the sphere lies within a bounding box.
     */
    @Override
    public boolean intersectsNode(OctreeNode node) {
        Vec3 c = new Vec3(cx, cy, cz);

        // Find the nearest point of the box to the sphere.
        if (cx < node.minx) {
            c.x = node.minx;
        } else if (cx > node.maxx) {
            c.x = node.maxx;
        }
        if (cy < node.miny) {
            c.y = node.miny;
        } else if (cy > node.maxy) {
            c.y = node.maxy;
        }
        if (cz < node.minz) {
            c.z = node.minz;
        } else if (cz > node.maxz) {
            c.z = node.maxz;
        }

        // If the sphere lies entirely outside the box, return false.
        c.set(c.x - cx, c.y - cy, c.z - cz);
        if (c.length2() > r2) {
            return false;
        }

        // If the box is completely inside the sphere, return false.  Otherwise, return true.
        c.set(node.minx - cx, node.miny - cy, node.minz - cz);
        if (c.length2() > r2) {
            return true;
        }
        c.set(node.minx - cx, node.miny - cy, node.maxz - cz);
        if (c.length2() > r2) {
            return true;
        }
        c.set(node.minx - cx, node.maxy - cy, node.minz - cz);
        if (c.length2() > r2) {
            return true;
        }
        c.set(node.minx - cx, node.maxy - cy, node.maxz - cz);
        if (c.length2() > r2) {
            return true;
        }
        c.set(node.maxx - cx, node.miny - cy, node.minz - cz);
        if (c.length2() > r2) {
            return true;
        }
        c.set(node.maxx - cx, node.miny - cy, node.maxz - cz);
        if (c.length2() > r2) {
            return true;
        }
        c.set(node.maxx - cx, node.maxy - cy, node.minz - cz);
        if (c.length2() > r2) {
            return true;
        }
        c.set(node.maxx - cx, node.maxy - cy, node.maxz - cz);
        if (c.length2() > r2) {
            return true;
        }
        return false;
    }

    /**
     * Get the transformation from world coordinates to the object's local coordinates.
     */
    @Override
    public Mat4 toLocal() {
        return toLocal;
    }

    /**
     * Inner class representing an intersection with an RTSphere.
     */
    private static class SphereIntersection implements SurfaceIntersection {

        private final RTSphere sphere;
        private final int numIntersections;
        private final double dist1;
        private final double dist2;
        private final double r1x;
        private final double r1y;
        private final double r1z;
        private final double r2x;
        private final double r2y;
        private final double r2z;
        private boolean trueNormValid;
        private final Vec3 trueNorm;
        private final Vec3 pos;

        public SphereIntersection(RTSphere sphere, int numIntersections, Vec3 point1, Vec3 point2, double dist1, double dist2) {
            this.sphere = sphere;
            this.numIntersections = numIntersections;
            this.dist1 = dist1;
            this.dist2 = dist2;
            r1x = point1.x;
            r1y = point1.y;
            r1z = point1.z;
            r2x = point2.x;
            r2y = point2.y;
            r2z = point2.z;
            trueNorm = new Vec3();
            pos = new Vec3();
        }

        @Override
        public RTObject getObject() {
            return sphere;
        }

        @Override
        public int numIntersections() {
            return numIntersections;
        }

        @Override
        public void intersectionPoint(int n, Vec3 p) {
            if (n == 0) {
                p.set(r1x, r1y, r1z);
            } else {
                p.set(r2x, r2y, r2z);
            }
        }

        @Override
        public double intersectionDist(int n) {
            if (n == 0) {
                return dist1;
            } else {
                return dist2;
            }
        }

        @Override
        public void intersectionProperties(TextureSpec spec, Vec3 n, Vec3 viewDir, double size, double time) {
            calcTrueNorm();
            n.set(trueNorm);
            TextureMapping map = sphere.theSphere.getTextureMapping();
            pos.set(r1x, r1y, r1z);
            if (map instanceof UniformMapping) {
                map.getTextureSpec(pos, spec, -n.dot(viewDir), size, time, sphere.param);
            } else {
                sphere.toLocal.transform(pos);
                map.getTextureSpec(pos, spec, -n.dot(viewDir), size, time, sphere.param);
            }
            if (sphere.bumpMapped) {
                sphere.fromLocal.transformDirection(spec.bumpGrad);
                n.scale(spec.bumpGrad.dot(n) + 1.0);
                n.subtract(spec.bumpGrad);
                n.normalize();
            }
        }

        @Override
        public void intersectionTransparency(int n, RGBColor trans, double angle, double size, double time) {
            TextureMapping map = sphere.theSphere.getTextureMapping();
            if (n == 0) {
                pos.set(r1x, r1y, r1z);
            } else {
                pos.set(r2x, r2y, r2z);
            }
            if (map instanceof UniformMapping) {
                map.getTransparency(pos, trans, angle, size, time, sphere.param);
            } else {
                sphere.toLocal.transform(pos);
                map.getTransparency(pos, trans, angle, size, time, sphere.param);
            }
        }

        @Override
        public void trueNormal(Vec3 n) {
            calcTrueNorm();
            n.set(trueNorm);
        }

        /**
         * Calculate the true normal of the point of intersection.
         */
        private void calcTrueNorm() {
            if (trueNormValid) {
                return;
            }
            trueNormValid = true;
            trueNorm.set(r1x - sphere.cx, r1y - sphere.cy, r1z - sphere.cz);
            trueNorm.normalize();
        }
    }
}
