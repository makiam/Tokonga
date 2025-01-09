/* Copyright (C) 1999-2004 by Peter Eastman
    Changes copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.texture.*;
import artofillusion.ui.MeshEditController;
import buoy.widget.*;

/**
 * The Mesh interface represents an object which is defined by a set of control vertices.
 */
public interface Mesh {

    int NO_SMOOTHING = 0;
    int SMOOTH_SHADING = 1;
    int INTERPOLATING = 2;
    int APPROXIMATING = 3;

    /**
     * Get the list of vertices which define the mesh.
     */
    MeshVertex[] getVertices();

    /**
     * Get a list of the positions of all vertices which define the mesh.
     */
    Vec3[] getVertexPositions();

    /**
     * Set the positions for all the vertices of the mesh.
     */
    void setVertexPositions(Vec3[] v);

    /**
     * Get an array of normal vectors, one for each vertex.
     */
    Vec3[] getNormals();

    /**
     * Get an array of TextureParameters which are defined on this mesh.
     */
    TextureParameter[] getParameters();

    /**
     * Get the values of the TextureParameters which are defined on this mesh.
     */
    ParameterValue[] getParameterValues();

    /**
     * Get the skeleton for the object. If it does not have one, this should return null.
     */
    default Skeleton getSkeleton() {
        return null;
    }

    /**
     * Set the skeleton for the object. If it cannot have a skeleton, this should do nothing.
     */
    default void setSkeleton(Skeleton s) {
    }

    /**
     * Get a MeshViewer which can be used for viewing this mesh.
     */
    default MeshViewer createMeshViewer(MeshEditController controller, RowContainer options) {
        return null;
    }
}
