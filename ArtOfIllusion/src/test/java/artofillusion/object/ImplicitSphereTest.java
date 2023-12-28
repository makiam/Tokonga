/* Copyright (C) 2013 by Peter Eastman
   Changes copyright (C) 2017-2023 by Maksim Khramov
   Changes/refactor (C) 2020 by Lucas Stanek

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.math.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import static java.lang.Math.*;
import org.junit.BeforeClass;

public class ImplicitSphereTest {

    private static final int testSpheres = 20;
    private static final int samplePoints = 100;
    private static final List<TestPair> pairs = new ArrayList<>();

    private static class TestPair {  //It was this, or abuse AbstractMap.SimpleEntry

        public final ImplicitSphere sphere;
        public final Vec3 point;

        public TestPair(ImplicitSphere sp, Vec3 pnt) {
            sphere = sp;
            point = pnt;
        }

        @Override
        public String toString() {
            return "TestPair: {" + "Point: " + point + ", Length: " + point.length() + " Implicit Sphere: radius: " + sphere.getRadius() + " Influence radius: " + sphere.getInfluenceRadius() + "}";
        }
        
        
    }

    @BeforeClass
    public static void setup_spheres_and_test_points() {
        for (int sphere = 0; sphere < testSpheres; sphere++) {
            double r1 = 0.1 + random();
            double r2 = 0.1 + random();
            ImplicitSphere currentSphere = new ImplicitSphere(min(r1, r2), max(r1, r2));

            for (int point = 0; point < samplePoints; point++) {
                /* Factor 2.05 is to cover the entire possible size range of
                 * test spheres (-1.1 to 1.1)
                 */
                Vec3 currentPoint = new Vec3(2.05 * (random() - 0.5), 2.05 * (random() - 0.5), 2.05 * (random() - 0.5));

                pairs.add(new TestPair(currentSphere, currentPoint));
            }
        }
    }

    @Test
    public void point_inside_radius_is_greater_than_1() {
        pairs.stream()
                .filter(p -> p.point.length() < p.sphere.getRadius())
                .forEach(p
                        -> Assert.assertTrue(p.toString(),
                        p.sphere.getFieldValue(p.point.x, p.point.y, p.point.z, 0, 0) > 1));
    }

    @Test
    public void point_outside_influence_radius_is_0() {
        pairs.stream()
                .filter(p -> p.point.length() > p.sphere.getInfluenceRadius())
                .forEach(p -> Assert.assertTrue(p.toString(), p.sphere.getFieldValue(p.point.x, p.point.y, p.point.z, 0, 0) == 0));
    }

    @Test
    public void point_between_radius_and_influence_is_between_0_and_1() {
        pairs.stream()
                .filter(p -> p.point.length() <= p.sphere.getInfluenceRadius() && p.point.length() >= p.sphere.getRadius())
                .forEach((TestPair p)
                        -> {
                    double value = p.sphere.getFieldValue(p.point.x, p.point.y, p.point.z, 0, 0);
                    Assert.assertTrue(p + "\nValue:" + value, 1 > value && value > 0);
                });
    }

    @Test
    public void gradient_estimate_within_delta() {
        pairs.stream()
                .filter(p -> abs(p.point.length() - p.sphere.getInfluenceRadius())
                > p.sphere.getRadius() * 1e-4)
                .forEach((TestPair p)
                        -> {
                    Vec3 grad = new Vec3();
                    p.sphere.getFieldGradient(p.point.x, p.point.y, p.point.z, 0, 0, grad);
                    Vec3 estGrad = estimateGradient(p);
                    Assert.assertEquals("X-grad" + p, estGrad.x, grad.x, 1e-4 * abs(grad.x));
                    Assert.assertEquals("Y-grad" + p, estGrad.y, grad.y, 1e-4 * abs(grad.y));
                    Assert.assertEquals("Z-grad" + p, estGrad.z, grad.z, 1e-4 * abs(grad.z));
                });
    }

    /**
     * Discontinuities near the edge of influence radius make estimating the
     * gradient difficult. For these, we just give up and make sure the returned
     * gradients are in the correct octant
     */
    @Test
    public void gradient_estimate_at_influence_edge() {
        pairs.stream()
                .filter(p -> abs(p.point.length() - p.sphere.getInfluenceRadius())
                <= p.sphere.getRadius() * 1e-4)
                .forEach((TestPair p)
                        -> {
                    Vec3 grad = new Vec3();
                    p.sphere.getFieldGradient(p.point.x, p.point.y, p.point.z, 0, 0, grad);
                    Vec3 estGrad = estimateGradient(p);
                    Assert.assertEquals("X-grad" + p, signum(estGrad.x), signum(grad.x), .01);
                    Assert.assertEquals("Y-grad" + p, signum(estGrad.y), signum(grad.y), .01);
                    Assert.assertEquals("Z-grad" + p, signum(estGrad.z), signum(grad.z), .01);
                });
    }

    private Vec3 estimateGradient(TestPair pair) {
        ImplicitSphere sphere = pair.sphere;
        Vec3 point = pair.point;
        double step = sphere.getRadius() * 1e-4;
        double vx1 = sphere.getFieldValue(point.x - step, point.y, point.z, 0, 0);
        double vx2 = sphere.getFieldValue(point.x + step, point.y, point.z, 0, 0);
        double vy1 = sphere.getFieldValue(point.x, point.y - step, point.z, 0, 0);
        double vy2 = sphere.getFieldValue(point.x, point.y + step, point.z, 0, 0);
        double vz1 = sphere.getFieldValue(point.x, point.y, point.z - step, 0, 0);
        double vz2 = sphere.getFieldValue(point.x, point.y, point.z + step, 0, 0);
        return new Vec3((vx2 - vx1) / (2 * step), (vy2 - vy1) / (2 * step), (vz2 - vz1) / (2 * step));
    }

    @Test(expected = ClassCastException.class)
    public void testCopyImplicitSphereFromBadObject() {
        ImplicitSphere me = new ImplicitSphere(1, 1);
        Sphere bad = new Sphere(1, 1, 1);
        me.copyObject(bad);
    }

    @Test
    public void testCopyImplicitSphere() {
        ImplicitSphere me = new ImplicitSphere(1, 1);
        ImplicitSphere other = new ImplicitSphere(2, 3);

        me.copyObject(other);
    }

    @Test
    public void testImplicitSphereBounds1() {
        ImplicitSphere sphere = new ImplicitSphere(1, 1);
        BoundingBox bb = sphere.getBounds();

        Assert.assertEquals(-1.0f, bb.minx, 0);
        Assert.assertEquals(-1.0f, bb.miny, 0);
        Assert.assertEquals(-1.0f, bb.minz, 0);
        Assert.assertEquals(1.0f, bb.maxx, 0);
        Assert.assertEquals(1.0f, bb.maxy, 0);
        Assert.assertEquals(1.0f, bb.maxz, 0);
    }

    @Test
    public void testImplicitSphereBounds2() {
        ImplicitSphere sphere = new ImplicitSphere(1, 2);
        BoundingBox bb = sphere.getBounds();

        Assert.assertEquals(-2.0f, bb.minx, 0);
        Assert.assertEquals(-2.0f, bb.miny, 0);
        Assert.assertEquals(-2.0f, bb.minz, 0);
        Assert.assertEquals(2.0f, bb.maxx, 0);
        Assert.assertEquals(2.0f, bb.maxy, 0);
        Assert.assertEquals(2.0f, bb.maxz, 0);
    }

}
