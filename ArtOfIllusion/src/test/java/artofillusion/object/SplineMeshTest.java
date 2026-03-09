/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import artofillusion.Scene;
import artofillusion.animation.Actor;
import artofillusion.math.BoundingBox;
import artofillusion.math.Vec3;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Maksim Khramov
 */
@DisplayName("SplineMesh Test")
class SplineMeshTest {

    private static Vec3[][] createSimpleGrid(int uSize, int vSize) {
        Vec3[][] vertices = new Vec3[uSize][vSize];
        for (int u = 0; u < uSize; u++) {
            for (int v = 0; v < vSize; v++) {
                vertices[u][v] = new Vec3(u, v, 0);
            }
        }
        return vertices;
    }

    @Test
    @DisplayName("Test Create SplineMesh")
    void testCreateSplineMesh() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Assertions.assertNotNull(mesh);
        Assertions.assertEquals(3, mesh.getUSize());
        Assertions.assertEquals(3, mesh.getVSize());
        Assertions.assertEquals(Mesh.INTERPOLATING, mesh.getSmoothingMethod());
        Assertions.assertFalse(mesh.isUClosed());
        Assertions.assertFalse(mesh.isVClosed());
    }

    @Test
    @DisplayName("Test Create SplineMesh with Approximating Method")
    void testCreateSplineMeshApproximating() {
        Vec3[][] vertices = createSimpleGrid(4, 4);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.APPROXIMATING, true, true);

        Assertions.assertNotNull(mesh);
        Assertions.assertEquals(Mesh.APPROXIMATING, mesh.getSmoothingMethod());
        Assertions.assertTrue(mesh.isUClosed());
        Assertions.assertTrue(mesh.isVClosed());
    }

    @Test
    @DisplayName("Test SplineMesh Duplicate")
    void testSplineMeshDuplicate() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 0.5f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh original = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, true, false);
        SplineMesh duplicate = original.duplicate();

        Assertions.assertNotNull(duplicate);
        Assertions.assertNotSame(original, duplicate);
        Assertions.assertEquals(original.getUSize(), duplicate.getUSize());
        Assertions.assertEquals(original.getVSize(), duplicate.getVSize());
        Assertions.assertEquals(original.getSmoothingMethod(), duplicate.getSmoothingMethod());
        Assertions.assertEquals(original.isUClosed(), duplicate.isUClosed());
        Assertions.assertEquals(original.isVClosed(), duplicate.isVClosed());
    }

    @Test
    @DisplayName("Test SplineMesh Copy")
    void testSplineMeshCopy() {
        Vec3[][] vertices1 = createSimpleGrid(3, 3);
        float[] uSmoothness1 = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness1 = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh original = new SplineMesh(vertices1, uSmoothness1, vSmoothness1, Mesh.INTERPOLATING, true, true);

        Vec3[][] vertices2 = createSimpleGrid(2, 2);
        float[] uSmoothness2 = new float[]{0.5f, 0.5f};
        float[] vSmoothness2 = new float[]{0.5f, 0.5f};

        SplineMesh target = new SplineMesh(vertices2, uSmoothness2, vSmoothness2, Mesh.APPROXIMATING, false, false);
        target.copyObject(original);

        Assertions.assertEquals(original.getUSize(), target.getUSize());
        Assertions.assertEquals(original.getVSize(), target.getVSize());
        Assertions.assertEquals(original.getSmoothingMethod(), target.getSmoothingMethod());
        Assertions.assertEquals(original.isUClosed(), target.isUClosed());
        Assertions.assertEquals(original.isVClosed(), target.isVClosed());
    }

    @Test
    @DisplayName("Test Get Vertex")
    void testGetVertex() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        MeshVertex vertex = mesh.getVertex(1, 2);
        Assertions.assertNotNull(vertex);
        Assertions.assertEquals(1.0, vertex.r.x, 0.001);
        Assertions.assertEquals(2.0, vertex.r.y, 0.001);
        Assertions.assertEquals(0.0, vertex.r.z, 0.001);
    }

    @Test
    @DisplayName("Test Get Vertices")
    void testGetVertices() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        MeshVertex[] allVertices = mesh.getVertices();
        Assertions.assertNotNull(allVertices);
        Assertions.assertEquals(9, allVertices.length);
    }

    @Test
    @DisplayName("Test Get Vertex Positions")
    void testGetVertexPositions() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Vec3[] positions = mesh.getVertexPositions();
        Assertions.assertNotNull(positions);
        Assertions.assertEquals(9, positions.length);
    }

    @Test
    @DisplayName("Test Set Vertex Positions")
    void testSetVertexPositions() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Vec3[] newPositions = new Vec3[9];
        for (int i = 0; i < 9; i++) {
            newPositions[i] = new Vec3(i * 2, i * 2, i * 2);
        }
        mesh.setVertexPositions(newPositions);

        Vec3[] positions = mesh.getVertexPositions();
        Assertions.assertEquals(0.0, positions[0].x, 0.001);
        Assertions.assertEquals(2.0, positions[1].x, 0.001);
    }

    @Test
    @DisplayName("Test Set Smoothing Method")
    void testSetSmoothingMethod() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
        mesh.setSmoothingMethod(Mesh.APPROXIMATING);

        Assertions.assertEquals(Mesh.APPROXIMATING, mesh.getSmoothingMethod());
    }

    @Test
    @DisplayName("Test Set Closed")
    void testSetClosed() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
        mesh.setClosed(true, true);

        Assertions.assertTrue(mesh.isUClosed());
        Assertions.assertTrue(mesh.isVClosed());
    }

    @Test
    @DisplayName("Test Get Bounds")
    void testGetBounds() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        BoundingBox bounds = mesh.getBounds();
        Assertions.assertNotNull(bounds);
    }

    @Test
    @DisplayName("Test Get Smoothness Arrays")
    void testGetSmoothnessArrays() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 0.5f, 1.0f};
        float[] vSmoothness = new float[]{0.8f, 0.8f, 0.8f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        float[] uSmooth = mesh.getUSmoothness();
        float[] vSmooth = mesh.getVSmoothness();

        Assertions.assertNotNull(uSmooth);
        Assertions.assertNotNull(vSmooth);
        Assertions.assertEquals(3, uSmooth.length);
        Assertions.assertEquals(3, vSmooth.length);
        Assertions.assertEquals(0.5f, uSmooth[1], 0.001);
        Assertions.assertEquals(0.8f, vSmooth[0], 0.001);
    }

    @Test
    @DisplayName("Test Set Smoothness")
    void testSetSmoothness() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        float[] newUSmooth = new float[]{0.3f, 0.3f, 0.3f};
        float[] newVSmooth = new float[]{0.7f, 0.7f, 0.7f};
        mesh.setSmoothness(newUSmooth, newVSmooth);

        Assertions.assertArrayEquals(newUSmooth, mesh.getUSmoothness());
        Assertions.assertArrayEquals(newVSmooth, mesh.getVSmoothness());
    }

    @Test
    @DisplayName("Test Is Closed for Open Mesh")
    void testIsClosedForOpenMesh() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Assertions.assertFalse(mesh.isClosed());
    }

    @Test
    @DisplayName("Test Subdivide Mesh")
    void testSubdivideMesh() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
        SplineMesh subdivided = SplineMesh.subdivideMesh(mesh, 0.1);

        Assertions.assertNotNull(subdivided);
        Assertions.assertTrue(subdivided.getUSize() >= mesh.getUSize());
        Assertions.assertTrue(subdivided.getVSize() >= mesh.getVSize());
    }

    @Test
    @DisplayName("Test Convert To Triangle Mesh")
    void testConvertToTriangleMesh() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        int canConvert = mesh.canConvertToTriangleMesh();
        Assertions.assertEquals(Object3D.APPROXIMATELY, canConvert);

        TriangleMesh triMesh = mesh.convertToTriangleMesh(0.1);
        Assertions.assertNotNull(triMesh);
    }

    @Test
    @DisplayName("Test Get Normals")
    void testGetNormals() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Vec3[] normals = mesh.getNormals();
        Assertions.assertNotNull(normals);
        Assertions.assertEquals(9, normals.length);
    }

    @Test
    @DisplayName("Test Get Wireframe Mesh")
    void testGetWireframeMesh() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        var wireframe = mesh.getWireframeMesh();
        Assertions.assertNotNull(wireframe);
        Assertions.assertNotNull(wireframe.vert);
        Assertions.assertNotNull(wireframe.from);
        Assertions.assertNotNull(wireframe.to);
    }

    @Test
    @DisplayName("Test Get Skeleton")
    void testGetSkeleton() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Assertions.assertNotNull(mesh.getSkeleton());
    }

    @Test
    @DisplayName("Test Is Editable")
    void testIsEditable() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Assertions.assertTrue(mesh.isEditable());
    }

    @Test
    @DisplayName("Test Can Convert To Actor")
    void testCanConvertToActor() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Assertions.assertTrue(mesh.canConvertToActor());
    }

    @Test
    @DisplayName("Test Get Posable Object")
    void testGetPosableObject() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Object3D posable = mesh.getPosableObject();
        Assertions.assertNotNull(posable);
        Assertions.assertTrue(posable instanceof Actor);
    }

    @Test
    @DisplayName("Test Get Pose Keyframe")
    void testGetPoseKeyframe() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

        Assertions.assertNotNull(mesh.getPoseKeyframe());
    }

    @Test
    @DisplayName("Test Set Size")
    void testSetSize() {
        Vec3[][] vertices = createSimpleGrid(3, 3);
        float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

        SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
        mesh.setSize(10.0, 10.0, 10.0);

        BoundingBox bounds = mesh.getBounds();
        Assertions.assertNotNull(bounds);
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Test Load SplineMesh Bad Version Low")
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        void testLoadSplineMeshBadVersionLow() {
            assertThrows(InvalidObjectException.class, () -> {
                ByteBuffer wrap = ByteBuffer.allocate(100);
                wrap.putShort((short) -1);
                new SplineMesh(new DataInputStream(new ByteArrayInputStream(wrap.array())), new Scene());
            });
        }

        @Test
        @DisplayName("Test Load SplineMesh Bad Version High")
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        void testLoadSplineMeshBadVersionHigh() {
            assertThrows(InvalidObjectException.class, () -> {
                ByteBuffer wrap = ByteBuffer.allocate(100);
                wrap.putShort((short) 2);
                new SplineMesh(new DataInputStream(new ByteArrayInputStream(wrap.array())), new Scene());
            });
        }
    }

    @Nested
    @DisplayName("Properties Tests")
    class PropertiesTests {

        @Test
        @DisplayName("Test Get Properties")
        void testGetProperties() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

            Assertions.assertNotNull(mesh.getProperties());
            Assertions.assertEquals(2, mesh.getProperties().length);
        }

        @Test
        @DisplayName("Test Get Property Value SmoothingMethod")
        void testGetPropertyValueSmoothingMethod() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

            Object value = mesh.getPropertyValue(0);
            Assertions.assertNotNull(value);
        }

        @Test
        @DisplayName("Test Get Property Value Closed Neither")
        void testGetPropertyValueClosedNeither() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

            Object value = mesh.getPropertyValue(1);
            Assertions.assertNotNull(value);
        }

        @Test
        @DisplayName("Test Get Property Value Closed U Only")
        void testGetPropertyValueClosedUOnly() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, true, false);

            Object value = mesh.getPropertyValue(1);
            Assertions.assertNotNull(value);
        }

        @Test
        @DisplayName("Test Get Property Value Closed V Only")
        void testGetPropertyValueClosedVOnly() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, true);

            Object value = mesh.getPropertyValue(1);
            Assertions.assertNotNull(value);
        }

        @Test
        @DisplayName("Test Get Property Value Closed Both")
        void testGetPropertyValueClosedBoth() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, true, true);

            Object value = mesh.getPropertyValue(1);
            Assertions.assertNotNull(value);
        }
    }

    @Nested
    @DisplayName("CalcInterpPoint and CalcApproxPoint Tests")
    class CalcPointTests {

        @Test
        @DisplayName("Test CalcInterpPoint")
        void testCalcInterpPoint() {
            MeshVertex[] vertices = new MeshVertex[4];
            vertices[0] = new MeshVertex(new Vec3(0, 0, 0));
            vertices[1] = new MeshVertex(new Vec3(1, 0, 0));
            vertices[2] = new MeshVertex(new Vec3(2, 0, 0));
            vertices[3] = new MeshVertex(new Vec3(3, 0, 0));

            float[] smoothness = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
            double[][] param = new double[4][0];
            double[] newParam = new double[0];

            MeshVertex result = SplineMesh.calcInterpPoint(vertices, smoothness, param, newParam, 0, 1, 2, 3);

            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.r);
        }

        @Test
        @DisplayName("Test CalcApproxPoint")
        void testCalcApproxPoint() {
            MeshVertex[] vertices = new MeshVertex[3];
            vertices[0] = new MeshVertex(new Vec3(0, 0, 0));
            vertices[1] = new MeshVertex(new Vec3(1, 0, 0));
            vertices[2] = new MeshVertex(new Vec3(2, 0, 0));

            float[] smoothness = new float[]{1.0f, 1.0f, 1.0f};
            double[][] param = new double[3][0];
            double[] newParam = new double[0];

            MeshVertex result = SplineMesh.calcApproxPoint(vertices, smoothness, param, newParam, 0, 1, 2);

            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.r);
        }
    }

    @Nested
    @DisplayName("SplineMeshKeyframe Tests")
    class KeyframeTests {

        @Test
        @DisplayName("Test SplineMeshKeyframe Duplicate")
        void testSplineMeshKeyframeDuplicate() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
            SplineMesh.SplineMeshKeyframe keyframe = (SplineMesh.SplineMeshKeyframe) mesh.getPoseKeyframe();

            SplineMesh.SplineMeshKeyframe duplicate = (SplineMesh.SplineMeshKeyframe) keyframe.duplicate();

            Assertions.assertNotNull(duplicate);
            Assertions.assertNotSame(keyframe, duplicate);
        }

        @Test
        @DisplayName("Test SplineMeshKeyframe Duplicate With Owner")
        void testSplineMeshKeyframeDuplicateWithOwner() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
            SplineMesh.SplineMeshKeyframe keyframe = (SplineMesh.SplineMeshKeyframe) mesh.getPoseKeyframe();

            SplineMesh.SplineMeshKeyframe duplicate = (SplineMesh.SplineMeshKeyframe) keyframe.duplicate(mesh);

            Assertions.assertNotNull(duplicate);
        }

        @Test
        @DisplayName("Test SplineMeshKeyframe Get Skeleton")
        void testSplineMeshKeyframeGetSkeleton() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
            SplineMesh.SplineMeshKeyframe keyframe = (SplineMesh.SplineMeshKeyframe) mesh.getPoseKeyframe();

            Assertions.assertNotNull(keyframe.getSkeleton());
        }

        @Test
        @DisplayName("Test SplineMeshKeyframe Equals")
        void testSplineMeshKeyframeEquals() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
            SplineMesh.SplineMeshKeyframe keyframe1 = (SplineMesh.SplineMeshKeyframe) mesh.getPoseKeyframe();
            SplineMesh.SplineMeshKeyframe keyframe2 = (SplineMesh.SplineMeshKeyframe) mesh.getPoseKeyframe();

            Assertions.assertTrue(keyframe1.equals(keyframe2));
        }

        @Test
        @DisplayName("Test SplineMeshKeyframe Equals Different Type")
        void testSplineMeshKeyframeEqualsDifferentType() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
            SplineMesh.SplineMeshKeyframe keyframe = (SplineMesh.SplineMeshKeyframe) mesh.getPoseKeyframe();

            Assertions.assertFalse(keyframe.equals("not a keyframe"));
        }

        @Test
        @DisplayName("Test Apply Pose Keyframe")
        void testApplyPoseKeyframe() {
            Vec3[][] vertices = createSimpleGrid(3, 3);
            float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
            float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};

            SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);
            SplineMesh.SplineMeshKeyframe keyframe = (SplineMesh.SplineMeshKeyframe) mesh.getPoseKeyframe();

            // Should not throw
            mesh.applyPoseKeyframe(keyframe);
        }
    }
}
