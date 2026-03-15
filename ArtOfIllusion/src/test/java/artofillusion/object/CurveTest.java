/* Copyright (C) 2018-2026 by Maksim Khramov

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
        Curve curve = source.duplicate();
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

    @Test
    @DisplayName("Test calcInterpPoint")
    void testCalcInterpPoint() {
        Vec3[] v = new Vec3[]{
            new Vec3(0f, 0f, 0f),   // index 0: i
            new Vec3(1f, 2f, 3f),   // index 1: j
            new Vec3(2f, 4f, 6f),   // index 2: k
            new Vec3(3f, 6f, 9f)    // index 3: m
        };

        float[] s = {1f, 0f, 0f, 1f};

        // Test with indices: i=0, j=1, k=2, m=3
        Vec3 result = Curve.calcInterpPoint(v, s, 0, 1, 2, 3);

        // Calculate expected values:
        // w1 = -0.0625 * s[1] = -0.0625 * 0 = 0
        // w2 = 0.5 - w1 = 0.5
        // w4 = -0.0625 * s[2] = -0.0625 * 0 = 0
        // w3 = 0.5 - w4 = 0.5
        // Result: w2*v[1] + w3*v[2] = 0.5*(1,2,3) + 0.5*(2,4,6) = (1.5, 3, 4.5)
        Vec3 expected = new Vec3(1.5f, 3f, 4.5f);

        Assertions.assertEquals(expected.x, result.x, 0.0001);
        Assertions.assertEquals(expected.y, result.y, 0.0001);
        Assertions.assertEquals(expected.z, result.z, 0.0001);

        // Test with non-zero smoothness values
        float[] s2 = {1f, 2f, 3f, 4f};
        Vec3 result2 = Curve.calcInterpPoint(v, s2, 0, 1, 2, 3);

        // w1 = -0.0625 * 2 = -0.125
        // w2 = 0.5 - (-0.125) = 0.625
        // w4 = -0.0625 * 3 = -0.1875
        // w3 = 0.5 - (-0.1875) = 0.6875
        // x: -0.125*0 + 0.625*1 + 0.6875*2 + -0.1875*3 = 0 + 0.625 + 1.375 - 0.5625 = 1.4375
        expected = new Vec3(1.4375f, 3.0f * 1.3546875f, 4.5f * 1.3546875f); // recalculating...

        // Manual verification:
        double w1_2 = -0.0625 * s2[1];       // -0.125
        double w2_2 = 0.5 - w1_2;            // 0.625
        double w4_2 = -0.0625 * s2[2];       // -0.1875
        double w3_2 = 0.5 - w4_2;            // 0.6875

        expected.x = (float)(w1_2 * v[0].x + w2_2 * v[1].x + w3_2 * v[2].x + w4_2 * v[3].x);
        expected.y = (float)(w1_2 * v[0].y + w2_2 * v[1].y + w3_2 * v[2].y + w4_2 * v[3].y);
        expected.z = (float)(w1_2 * v[0].z + w2_2 * v[1].z + w3_2 * v[2].z + w4_2 * v[3].z);

        Assertions.assertEquals(expected.x, result2.x, 0.0001);
        Assertions.assertEquals(expected.y, result2.y, 0.0001);
        Assertions.assertEquals(expected.z, result2.z, 0.0001);
    }

    @Test
    @DisplayName("Test calcInterpPoint with identical points")
    void testCalcInterpPointIdenticalPoints() {
        float value = 5f;
        Vec3[] v = new Vec3[]{
            new Vec3(value, value, value),
            new Vec3(value, value, value),
            new Vec3(value, value, value),
            new Vec3(value, value, value)
        };
        float[] s = {0f, 1f, 2f, 3f};

        Vec3 result = Curve.calcInterpPoint(v, s, 0, 1, 2, 3);

        Assertions.assertEquals(value, result.x, 0.0001);
        Assertions.assertEquals(value, result.y, 0.0001);
        Assertions.assertEquals(value, result.z, 0.0001);
    }
}
