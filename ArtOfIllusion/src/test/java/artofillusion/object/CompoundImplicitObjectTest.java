/* 
   Copyright (C) 2017-2023 by Maksim Khramov   

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.animation.Keyframe;
import artofillusion.math.BoundingBox;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;


import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author maksim.khramov
 */
@DisplayName("Compound Implicit Object Test")
class CompoundImplicitObjectTest {

    @Test
    @DisplayName("Test Get Initial Cutoff")
    void testGetInitialCutoff() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        Assertions.assertEquals(1.0, cio.getCutoff(), 0);
    }

    @Test
    @DisplayName("Test Compound Implicit Object Is Editable")
    void testCompoundImplicitObjectIsEditable() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        Assertions.assertTrue(cio.isEditable());
    }

    @Test
    @DisplayName("Test Get Empty Object Size")
    void testGetEmptyObjectSize() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        Assertions.assertEquals(0, cio.getNumObjects());
    }

    @Test
    @DisplayName("Test Empty Compound Bounds")
    void testEmptyCompoundBounds() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        BoundingBox bb = cio.getBounds();
        Assertions.assertEquals(0f, bb.minx, 0);
        Assertions.assertEquals(0f, bb.miny, 0);
        Assertions.assertEquals(0f, bb.minz, 0);
        Assertions.assertEquals(0f, bb.maxx, 0);
        Assertions.assertEquals(0f, bb.maxy, 0);
        Assertions.assertEquals(0f, bb.maxz, 0);
    }

    @Test
    @DisplayName("Test Compound Bounds Single")
    void testCompoundBoundsSingle() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        cio.addObject(new ImplicitSphere(1.0, 1.0), new CoordinateSystem());
        BoundingBox bb = cio.getBounds();
        Assertions.assertEquals(-1.0f, bb.minx, 0);
        Assertions.assertEquals(-1.0f, bb.miny, 0);
        Assertions.assertEquals(-1.0f, bb.minz, 0);
        Assertions.assertEquals(1.0f, bb.maxx, 0);
        Assertions.assertEquals(1.0f, bb.maxy, 0);
        Assertions.assertEquals(1.0f, bb.maxz, 0);
    }

    @Test
    @DisplayName("Test Compound Bounds Double")
    void testCompoundBoundsDouble() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        cio.addObject(new ImplicitSphere(1.0, 1.0), new CoordinateSystem());
        cio.addObject(new ImplicitSphere(1.0, 2.0), new CoordinateSystem());
        BoundingBox bb = cio.getBounds();
        Assertions.assertEquals(-2.0f, bb.minx, 0);
        Assertions.assertEquals(-2.0f, bb.miny, 0);
        Assertions.assertEquals(-2.0f, bb.minz, 0);
        Assertions.assertEquals(2.0f, bb.maxx, 0);
        Assertions.assertEquals(2.0f, bb.maxy, 0);
        Assertions.assertEquals(2.0f, bb.maxz, 0);
    }

    @Test
    @DisplayName("Test Get Object From Empty Compound Negative")
    void testGetObjectFromEmptyCompoundNegative() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            CompoundImplicitObject cio = new CompoundImplicitObject();
            cio.getObject(0);
        });
    }

    @Test
    @DisplayName("Test Set Object To Empty Compund Negative")
    void testSetObjectToEmptyCompundNegative() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            CompoundImplicitObject cio = new CompoundImplicitObject();
            cio.setObject(0, new ImplicitSphere(1.0, 1.0));
        });
    }

    @Test
    @DisplayName("Test Set Object To Compound")
    void testSetObjectToCompound() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        ImplicitSphere sphereOriginal = new ImplicitSphere(1.0, 1.0);
        cio.addObject(sphereOriginal, new CoordinateSystem());
        ImplicitSphere sphereReplacement = new ImplicitSphere(1.0, 1.0);
        cio.setObject(0, sphereReplacement);
        ImplicitSphere test = (ImplicitSphere) cio.getObject(0);
        Assertions.assertEquals(test, sphereReplacement);
    }

    @Test
    @DisplayName("Test Set Object Coordinates For Empty Compound")
    void testSetObjectCoordinatesForEmptyCompound() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            CompoundImplicitObject cio = new CompoundImplicitObject();
            cio.setObjectCoordinates(0, new CoordinateSystem());
        });
    }

    @Test
    @DisplayName("Test Set Object Coordinates For Compound")
    void testSetObjectCoordinatesForCompound() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        cio.addObject(new ImplicitSphere(1.0, 1.0), new CoordinateSystem());
        cio.setObjectCoordinates(0, new CoordinateSystem(Vec3.vx(), Vec3.vy(), Vec3.vz()));
    }

    @Test
    @DisplayName("Test Add Compound")
    void testAddCompound() {
        CompoundImplicitObject cio = new CompoundImplicitObject();
        cio.addObject(new ImplicitSphere(1.0, 1.0), new CoordinateSystem());
        Assertions.assertEquals(1, cio.getNumObjects());
    }

    @Test
    @DisplayName("Test Duplicate Empty Compound")
    void testDuplicateEmptyCompound() {
        CompoundImplicitObject source = new CompoundImplicitObject();
        CompoundImplicitObject target = (CompoundImplicitObject) source.duplicate();
        Assertions.assertNotEquals(target, source);
        Assertions.assertEquals(0, target.getNumObjects());
    }

    @Test
    @DisplayName("Test Duplicate Compound")
    void testDuplicateCompound() {
        CompoundImplicitObject source = new CompoundImplicitObject();
        ImplicitObject sourceImplicit = new ImplicitSphere(1.0, 1.0);
        CoordinateSystem ccs = new CoordinateSystem();
        source.addObject(sourceImplicit, ccs);
        CompoundImplicitObject target = (CompoundImplicitObject) source.duplicate();
        Assertions.assertNotEquals(target, source);
        Assertions.assertEquals(1, target.getNumObjects());
        Assertions.assertNotEquals(sourceImplicit, target.getObject(0));
        CoordinateSystem tcs = target.getObjectCoordinates(0);
        Assertions.assertEquals(ccs, tcs);
        Assertions.assertNotSame(ccs, tcs);
    }

    @Test
    @DisplayName("Test Get Pose Key Frame From Empty Compound")
    void testGetPoseKeyFrameFromEmptyCompound() {
        CompoundImplicitObject source = new CompoundImplicitObject();
        Keyframe keyframe = source.getPoseKeyframe();
        Assertions.assertNotNull(keyframe);
        Assertions.assertTrue(keyframe instanceof CompoundImplicitObject.CompoundImplicitKeyframe);
        Assertions.assertTrue(((CompoundImplicitObject.CompoundImplicitKeyframe) keyframe).key.isEmpty());
    }

    @Test
    @DisplayName("Test Get Pose Key Frame From Compound")
    void testGetPoseKeyFrameFromCompound() {
        CompoundImplicitObject source = new CompoundImplicitObject();
        ImplicitObject sourceImplicit = new ImplicitSphere(1.0, 1.0);
        CoordinateSystem ccs = new CoordinateSystem();
        source.addObject(sourceImplicit, ccs);
        Keyframe keyframe = source.getPoseKeyframe();
        Assertions.assertNotNull(keyframe);
        Assertions.assertTrue(keyframe instanceof CompoundImplicitObject.CompoundImplicitKeyframe);
        Assertions.assertFalse(((CompoundImplicitObject.CompoundImplicitKeyframe) keyframe).key.isEmpty());
    }
}
