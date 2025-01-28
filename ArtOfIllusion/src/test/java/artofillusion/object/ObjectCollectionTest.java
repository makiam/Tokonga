/* Copyright (C) 2018-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import artofillusion.Scene;
import artofillusion.animation.Keyframe;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;

import java.util.Enumeration;
import java.util.Vector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author maksim.khramov
 */
@DisplayName("Object Collection Test")
class ObjectCollectionTest {

    @Test
    @DisplayName("Test Empty Collection Object Is Closed")
    void testEmptyCollectionObjectIsClosed() {
        ObjectCollection oc = new CustomObjectCollection();
        Assertions.assertNotNull(oc);
        Assertions.assertTrue(oc.isClosed());
    }

    @Test
    @DisplayName("Test Closed Single Item Collection Object Is Closed")
    void testClosedSingleItemCollectionObjectIsClosed() {
        ObjectCollection oc = new CustomObjectCollection();
        oc.cachedObjects.add(new ObjectInfo(new Cube(1, 1, 1), new CoordinateSystem(), "Cube"));
        Assertions.assertNotNull(oc);
        Assertions.assertTrue(oc.isClosed());
    }

    @Test
    @DisplayName("Test Opened Single Item Collection Object Is Not Closed")
    void testOpenedSingleItemCollectionObjectIsNotClosed() {
        ObjectCollection oc = new CustomObjectCollection();
        Curve curve = new Curve(new Vec3[0], new float[0], Mesh.APPROXIMATING, false);
        oc.cachedObjects.add(new ObjectInfo(curve, new CoordinateSystem(), "Curve"));
        Assertions.assertNotNull(oc);
        Assertions.assertFalse(oc.isClosed());
    }

    @Test
    @DisplayName("Test Object Collection Is Closed")
    void testObjectCollectionIsClosed() {
        ObjectCollection oc = new CustomObjectCollection();
        oc.cachedObjects.add(new ObjectInfo(new Cube(1, 1, 1), new CoordinateSystem(), "Cube1"));
        oc.cachedObjects.add(new ObjectInfo(new Cube(1, 1, 1), new CoordinateSystem(), "Cube2"));
        Assertions.assertNotNull(oc);
        Assertions.assertTrue(oc.isClosed());
    }

    @Test
    @DisplayName("Test Object Collection Is Not Closed")
    void testObjectCollectionIsNotClosed() {
        ObjectCollection oc = new CustomObjectCollection();
        Curve curve = new Curve(new Vec3[0], new float[0], Mesh.APPROXIMATING, false);
        oc.cachedObjects.add(new ObjectInfo(new Cube(1, 1, 1), new CoordinateSystem(), "Cube"));
        oc.cachedObjects.add(new ObjectInfo(curve, new CoordinateSystem(), "Curve"));
        Assertions.assertNotNull(oc);
        Assertions.assertFalse(oc.isClosed());
    }

    class CustomObjectCollection extends ObjectCollection {

        public CustomObjectCollection() {
            super();
            cachedObjects = new Vector<>();
        }

        @Override
        protected Enumeration<ObjectInfo> enumerateObjects(ObjectInfo info, boolean interactive, Scene scene) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CustomObjectCollection duplicate() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void copyObject(Object3D obj) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSize(double xsize, double ysize, double zsize) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Keyframe getPoseKeyframe() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void applyPoseKeyframe(Keyframe k) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
