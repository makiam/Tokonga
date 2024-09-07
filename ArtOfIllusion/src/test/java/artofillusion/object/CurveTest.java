/* Copyright (C) 2018 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import artofillusion.Scene;
import artofillusion.math.Vec3;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author maksim.khramov
 */
@DisplayName("Curve Test")
class CurveTest {

    @Test
    @DisplayName("Test Create Curve")
    void testCreateCurve() {
        Curve curve = new Curve(new Vec3[0], new float[0], Mesh.APPROXIMATING, true);
        Assertions.assertNotNull(curve);
        Assertions.assertNotNull(curve.getVertices());
        Assertions.assertEquals(0, curve.getVertices().length);
        Assertions.assertEquals(0, curve.getSmoothness().length);
        Assertions.assertEquals(Mesh.APPROXIMATING, curve.getSmoothingMethod());
    }

    @Test
    @DisplayName("Test Create Curve 2")
    void testCreateCurve2() {
        Curve curve = new Curve(new Vec3[]{new Vec3(), new Vec3()}, new float[]{0f, 1f}, Mesh.APPROXIMATING, false);
        Assertions.assertNotNull(curve);
        Assertions.assertNotNull(curve.getVertices());
        Assertions.assertEquals(2, curve.getVertices().length);
        Assertions.assertEquals(2, curve.getSmoothness().length);
        Assertions.assertEquals(Mesh.APPROXIMATING, curve.getSmoothingMethod());
        Assertions.assertFalse(curve.isClosed());
    }

    @Test
    @DisplayName("Test Curve Duplicate")
    void testCurveDuplicate() {
        Curve source = new Curve(new Vec3[]{new Vec3(), new Vec3()}, new float[]{0f, 1f}, Mesh.APPROXIMATING, false);
        Curve curve = (Curve) source.duplicate();
        Assertions.assertNotEquals(source, curve);
        Assertions.assertNotNull(curve);
        Assertions.assertNotNull(curve.getVertices());
        Assertions.assertEquals(2, curve.getVertices().length);
        Assertions.assertEquals(2, curve.getSmoothness().length);
        Assertions.assertEquals(Mesh.APPROXIMATING, curve.getSmoothingMethod());
        Assertions.assertFalse(curve.isClosed());
    }

    @Test
    @DisplayName("Test Curve Copy")
    void testCurveCopy() {
        Curve source = new Curve(new Vec3[]{new Vec3(), new Vec3()}, new float[]{0f, 1f}, Mesh.APPROXIMATING, false);
        Curve curve = new Curve(new Vec3[0], new float[0], Mesh.APPROXIMATING, true);
        curve.copyObject(source);
        Assertions.assertNotEquals(source, curve);
        Assertions.assertNotNull(curve);
        Assertions.assertNotNull(curve.getVertices());
        Assertions.assertEquals(2, curve.getVertices().length);
        Assertions.assertEquals(2, curve.getSmoothness().length);
        Assertions.assertEquals(Mesh.APPROXIMATING, curve.getSmoothingMethod());
        Assertions.assertFalse(curve.isClosed());
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Load Curve Bad Object Version 1")
    void testLoadCurveBadObjectVersion1() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            wrap.putShort((short) -1);
            new Curve(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Load Curve Bad Object Version 2")
    void testLoadCurveBadObjectVersion2() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            wrap.putShort((short) 2);
            new Curve(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Load Curve Bad Object Version 3")
    void testLoadCurveBadObjectVersion3() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            wrap.putShort((short) 1);
            // Read version again !!!!
            wrap.putShort((short) 1);
            new Curve(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);
        });
    }

    @Test
    @DisplayName("Test Load Curve")
    void testLoadCurve() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 0);
        // Read version again !!!!
        wrap.putShort((short) 0);
        // Vertex count
        wrap.putInt(0);
        // Closed curve - false
        wrap.put((byte) 0);
        wrap.putInt(Mesh.INTERPOLATING);
        Curve curve = new Curve(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);
        Assertions.assertNotNull(curve);
        Assertions.assertEquals(0, curve.getVertices().length);
        Assertions.assertEquals(false, curve.isClosed());
        Assertions.assertEquals(Mesh.INTERPOLATING, curve.getSmoothingMethod());
    }
}
