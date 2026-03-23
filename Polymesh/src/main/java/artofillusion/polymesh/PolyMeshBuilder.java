/*
 *  Copyright (C) 2005-2007 by Francois Guillet
 *  Changes copyright (C) 2026 by Maksim Khramov

 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.polymesh;

import artofillusion.math.Vec3;
import artofillusion.polymesh.PolyMesh.Wvertex;
import artofillusion.polymesh.PolyMesh.Wedge;
import artofillusion.polymesh.PolyMesh.Wface;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class PolyMeshBuilder {

    static void buildOctahedronMesh(PolyMesh mesh) {
        var vertices = new Wvertex[6];
        vertices[0] = new Wvertex(new Vec3(0.0, -0.707107, 0.0), 0);
        vertices[1] = new Wvertex(new Vec3(0.5, 0.0, -0.5), 11);
        vertices[2] = new Wvertex(new Vec3(-0.5, 0.0, -0.5), 8);
        vertices[3] = new Wvertex(new Vec3(0.5, 0.0, 0.5), 10);
        vertices[4] = new Wvertex(new Vec3(-0.5, 0.0, 0.5), 9);
        vertices[5] = new Wvertex(new Vec3(0.0, 0.707107, 0.0), 22);
        var edges = new Wedge[24];
        edges[0] = new Wedge(1, 12, 1, 3);
        edges[1] = new Wedge(2, 13, 4, 8);
        edges[2] = new Wedge(0, 14, 2, 19);
        edges[3] = new Wedge(3, 15, 1, 17);
        edges[4] = new Wedge(4, 16, 5, 9);
        edges[5] = new Wedge(3, 17, 3, 6);
        edges[6] = new Wedge(4, 18, 3, 7);
        edges[7] = new Wedge(0, 19, 3, 5);
        edges[8] = new Wedge(5, 20, 4, 23);
        edges[9] = new Wedge(5, 21, 5, 20);
        edges[10] = new Wedge(5, 22, 6, 21);
        edges[11] = new Wedge(5, 23, 7, 22);
        edges[12] = new Wedge(0, 0, 0, 14);
        edges[13] = new Wedge(1, 1, 0, 12);
        edges[14] = new Wedge(2, 2, 0, 13);
        edges[15] = new Wedge(1, 3, 7, 11);
        edges[16] = new Wedge(2, 4, 2, 2);
        edges[17] = new Wedge(0, 5, 1, 0);
        edges[18] = new Wedge(3, 6, 6, 10);
        edges[19] = new Wedge(4, 7, 2, 16);
        edges[20] = new Wedge(2, 8, 5, 4);
        edges[21] = new Wedge(4, 9, 6, 18);
        edges[22] = new Wedge(3, 10, 7, 15);
        edges[23] = new Wedge(1, 11, 4, 1);
        var faces = new Wface[8];
        faces[0] = new Wface(14);
        faces[1] = new Wface(0);
        faces[2] = new Wface(2);
        faces[3] = new Wface(5);
        faces[4] = new Wface(8);
        faces[5] = new Wface(9);
        faces[6] = new Wface(10);
        faces[7] = new Wface(11);
        mesh.setVertices(vertices);
        mesh.setFaces(faces);
        mesh.setEdges(edges);
    }

    static void buildCubeMesh(PolyMesh mesh) {
        var vertices = new PolyMesh.Wvertex[8];
        vertices[0] = new Wvertex(new Vec3(0.5, -0.5, -0.5), 0);
        vertices[1] = new Wvertex(new Vec3(0.5, 0.5, -0.5), 1);
        vertices[2] = new PolyMesh.Wvertex(new Vec3(-0.5, 0.5, -0.5), 2);
        vertices[3] = new Wvertex(new Vec3(-0.5, -0.5, -0.5), 3);
        vertices[4] = new Wvertex(new Vec3(-0.5, -0.5, 0.5), 11);
        vertices[5] = new Wvertex(new Vec3(0.5, -0.5, 0.5), 8);
        vertices[6] = new Wvertex(new Vec3(0.5, 0.5, 0.5), 9);
        vertices[7] = new Wvertex(new Vec3(-0.5, 0.5, 0.5), 10);

        var faces = new Wface[6];
        faces[0] = new Wface(14);
        faces[1] = new Wface(0);
        faces[2] = new Wface(1);
        faces[3] = new Wface(2);
        faces[4] = new Wface(3);
        faces[5] = new Wface(8);

        var edges = new Wedge[24];
        edges[0] = new Wedge(1, 12, 1, 5);
        edges[1] = new Wedge(2, 13, 2, 6);
        edges[2] = new Wedge(3, 14, 3, 7);
        edges[3] = new Wedge(0, 15, 4, 4);
        edges[4] = new Wedge(5, 16, 4, 23);
        edges[5] = new Wedge(6, 17, 1, 20);
        edges[6] = new Wedge(7, 18, 2, 21);
        edges[7] = new Wedge(4, 19, 3, 22);
        edges[8] = new Wedge(6, 20, 5, 9);
        edges[9] = new Wedge(7, 21, 5, 10);
        edges[10] = new Wedge(4, 22, 5, 11);
        edges[11] = new Wedge(5, 23, 5, 8);

        edges[12] = new Wedge(0, 0, 0, 15);
        edges[13] = new Wedge(1, 1, 0, 12);
        edges[14] = new Wedge(2, 2, 0, 13);
        edges[15] = new Wedge(3, 3, 0, 14);
        edges[16] = new Wedge(0, 4, 1, 0);
        edges[17] = new Wedge(1, 5, 2, 1);
        edges[18] = new Wedge(2, 6, 3, 2);
        edges[19] = new Wedge(3, 7, 4, 3);
        edges[20] = new Wedge(5, 8, 1, 16);
        edges[21] = new Wedge(6, 9, 2, 17);
        edges[22] = new Wedge(7, 10, 3, 18);
        edges[23] = new Wedge(4, 11, 4, 19);

        mesh.setVertices(vertices);
        mesh.setFaces(faces);
        mesh.setEdges(edges);
    }
}
