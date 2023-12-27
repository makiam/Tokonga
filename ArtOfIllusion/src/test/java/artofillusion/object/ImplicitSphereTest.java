/* Copyright (C) 2013 by Peter Eastman
   Changes copyright (C) 2017-2024 by Maksim Khramov
   Changes/refactor (C) 2020 by Lucas Stanek

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import artofillusion.math.*;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import java.util.*;

import static java.lang.Math.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Implicit Sphere Test")
class ImplicitSphereTest {

    private static final int testSpheres = 20;
    private static final int samplePoints = 100;
    private static final List<TestPair> pairs = new ArrayList<>();

    private static final List<TestPair> pairs = new ArrayList<>();

    @DisplayName("Test Pair")
    private static class TestPair {

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

    @BeforeAll
    static void setup_spheres_and_test_points() {
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
                .forEach((TestPair p)
                        -> Assertions.assertTrue( p.sphere.getFieldValue(p.point.x, p.point.y, p.point.z, 0, 0) > 1, ()-> p.toString()));
    }

    @Test
    @DisplayName("Point _ outside _ influence _ radius _ is _ 0")
    void point_outside_influence_radius_is_0() {
        pairs.stream().
                filter(p -> p.point.length() > p.sphere.getInfluenceRadius()).
                forEach((TestPair p)
                        -> Assertions.assertTrue(p.sphere.getFieldValue(p.point.x, p.point.y, p.point.z, 0, 0) == 0, ()-> p.toString()));
    }

    @Test
    @DisplayName("Point _ between _ radius _ and _ influence _ is _ between _ 0 _ and _ 1")
    void point_between_radius_and_influence_is_between_0_and_1() {
        pairs.stream().filter(p -> p.point.length() <= p.sphere.getInfluenceRadius() && p.point.length() >= p.sphere.getRadius()).forEach((TestPair p) -> {
            double value = p.sphere.getFieldValue(p.point.x, p.point.y, p.point.z, 0, 0);
            Assertions.assertTrue(() -> 1 > value && value > 0, () -> p.toString() + "Value: " + value);
        });
    }

    @Test
    @DisplayName("Gradient _ estimate _ within _ delta")
    void gradient_estimate_within_delta() {
        pairs.stream().filter(p -> abs(p.point.length() - p.sphere.getInfluenceRadius()) > p.sphere.getRadius() * 1e-4).forEach(p -> {
            Vec3 grad = new Vec3();
            p.sphere.getFieldGradient(p.point.x, p.point.y, p.point.z, 0, 0, grad);
            Vec3 estGrad = estimateGradient(p);
            Assertions.assertEquals(estGrad.x, grad.x, 1e-4 * abs(grad.x), "X-grad: " + p);
            Assertions.assertEquals(estGrad.y, grad.y, 1e-4 * abs(grad.y), "Y-grad: " + p);
            Assertions.assertEquals(estGrad.z, grad.z, 1e-4 * abs(grad.z), "Z-grad: " + p);
        });
    }

    /**
     * Discontinuities near the edge of influence radius make estimating the
     * gradient difficult. For these, we just give up and make sure the returned
     * gradients are in the correct octant
     */
    @Test
    @DisplayName("Gradient _ estimate _ at _ influence _ edge")
    void gradient_estimate_at_influence_edge() {
        pairs.stream().filter(p -> abs(p.point.length() - p.sphere.getInfluenceRadius()) <= p.sphere.getRadius() * 1e-4).forEach(p -> {
            Vec3 grad = new Vec3();
            p.sphere.getFieldGradient(p.point.x, p.point.y, p.point.z, 0, 0, grad);
            Vec3 estGrad = estimateGradient(p);
            Assertions.assertEquals(signum(estGrad.x), signum(grad.x), .01, "X-grad: " + p);
            Assertions.assertEquals(signum(estGrad.y), signum(grad.y), .01, "Y-grad: " + p);
            Assertions.assertEquals(signum(estGrad.z), signum(grad.z), .01, "Z-grad: " + p);

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

    @Test
    @DisplayName("Test Copy Implicit Sphere From Bad Object")
    @SuppressWarnings("ThrowableResultIgnored")
    void testCopyImplicitSphereFromBadObject() {

        Assertions.assertThrows(ClassCastException.class, () -> {
            ImplicitSphere me = new ImplicitSphere(1, 1);
            Sphere bad = new Sphere(1, 1, 1);
            me.copyObject(bad);
        });
    }

    @Test
    @DisplayName("Test Copy Implicit Sphere")
    void testCopyImplicitSphere() {
        ImplicitSphere me = new ImplicitSphere(1, 1);
        ImplicitSphere other = new ImplicitSphere(2, 3);
        me.copyObject(other);
    }

    @Test
    @DisplayName("Test Implicit Sphere Bounds 1")
    void testImplicitSphereBounds1() {
        ImplicitSphere sphere = new ImplicitSphere(1, 1);
        BoundingBox bb = sphere.getBounds();
        Assertions.assertEquals(-1.0f, bb.minx, 0);
        Assertions.assertEquals(-1.0f, bb.miny, 0);
        Assertions.assertEquals(-1.0f, bb.minz, 0);
        Assertions.assertEquals(1.0f, bb.maxx, 0);
        Assertions.assertEquals(1.0f, bb.maxy, 0);
        Assertions.assertEquals(1.0f, bb.maxz, 0);
    }

    @Test
    @DisplayName("Test Implicit Sphere Bounds 2")
    void testImplicitSphereBounds2() {
        ImplicitSphere sphere = new ImplicitSphere(1, 2);
        BoundingBox bb = sphere.getBounds();
        Assertions.assertEquals(-2.0f, bb.minx, 0);
        Assertions.assertEquals(-2.0f, bb.miny, 0);
        Assertions.assertEquals(-2.0f, bb.minz, 0);
        Assertions.assertEquals(2.0f, bb.maxx, 0);
        Assertions.assertEquals(2.0f, bb.maxy, 0);
        Assertions.assertEquals(2.0f, bb.maxz, 0);
    }
}
