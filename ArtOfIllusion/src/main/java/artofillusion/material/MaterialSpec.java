/* MaterialSpec describes the properties of a point in the interior of an object. */

 /* Copyright (C) 2000 by Peter Eastman
   Changes copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.material;

import artofillusion.math.RGBColor;

public class MaterialSpec {

    public final RGBColor transparency = new RGBColor();
    public final RGBColor color = new RGBColor();
    public final RGBColor scattering = new RGBColor();
    public double eccentricity = 0.0;


}
