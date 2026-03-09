package artofillusion.polymesh;

import artofillusion.math.Vec3;
import artofillusion.object.Mesh;
import artofillusion.object.MeshVertex;
import artofillusion.object.SplineMesh;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


class PolyMeshTest {

    @Test
    void testCreatePolymeshFromDataStream() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        var dis = new DataInputStream(stream(wrap));
        var pm = new PolyMesh(dis);
    }

    @Test
    void testCreatePolymeshFromSceneDataStream() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        var dis = new DataInputStream(stream(wrap));
        var pm = new PolyMesh(dis);
    }

    public static DataInputStream stream(ByteBuffer wrap) {
        return new DataInputStream(new ByteArrayInputStream(wrap.array()));
    }

    @Test
    void testCreatePolymeshFromSplineMesh() {
        MeshVertex[][] vertices = new MeshVertex[2][2];
        vertices[0][0] = new MeshVertex(new Vec3(-0.6f, -0.6f, 0.f));
        vertices[0][1] = new MeshVertex(new Vec3(-0.6f,  +0.6f, 0.f));
        vertices[1][0] = new MeshVertex(new Vec3( +0.6f, -0.6f, 0.f));
        vertices[1][1] = new MeshVertex(new Vec3( +0.6f,  +0.6f, 0.f));

        SplineMesh spm = new SplineMesh();
        spm.setSmoothingMethod(Mesh.APPROXIMATING);
        spm.setShape(vertices, new float[]{1f, 1f}, new float[]{1f, 1f});

        var pm = new PolyMesh(spm);
        //Polymesh takes smoothing method from original spline mesh
        Assertions.assertEquals(spm.getSmoothingMethod(), pm.getSmoothingMethod());
        Assertions.assertTrue(pm.isControlledSmoothing()); //
        Assertions.assertEquals(0, pm.getMinAngle());
        Assertions.assertEquals(90, pm.getMaxAngle());
    }
}