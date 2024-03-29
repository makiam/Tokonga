/* This records the information about a point in space for which a texture or material is
   being evaluated. */

 /* Copyright (C) 2000 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.procedural;

public class PointInfo {

    public double x;
    public double y;
    public double z;
    public double xsize;
    public double ysize;
    public double zsize;
    public double viewangle;
    public double t;
    public double[] param;

    public PointInfo() {
    }
}
