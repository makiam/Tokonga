/* A WireframeMesh represents an object to be rendered to the screen as a wireframe.  It is
   described by an array of vertices and a list of lines to be drawn between them. */

 /* Copyright (C) 2000 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion;

import artofillusion.math.*;

public class WireframeMesh {

    public final Vec3[] vert;
    public final int[] from;
    public final int[] to;

    public WireframeMesh(Vec3[] vert, int[] from, int[] to) {
        this.vert = vert;
        this.from = from;
        this.to = to;
    }
}
