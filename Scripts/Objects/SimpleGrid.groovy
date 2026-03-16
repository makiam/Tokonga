private static Vec3[][] createSimpleGrid(int uSize, int vSize) {
    Vec3[][] vertices = new Vec3[uSize][vSize];
    for (int u = 0; u < uSize; u++) {
        for (int v = 0; v < vSize; v++) {
            vertices[u][v] = new Vec3(u, v, 0);
        }
    }
    return vertices;
}

Vec3[][] vertices = createSimpleGrid(3, 3);
float[] uSmoothness = new float[]{1.0f, 1.0f, 1.0f};
float[] vSmoothness = new float[]{1.0f, 1.0f, 1.0f};
        
SplineMesh mesh = new SplineMesh(vertices, uSmoothness, vSmoothness, Mesh.INTERPOLATING, false, false);

window.addObject(mesh, new CoordinateSystem(), "My Mesh", null)