/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural


import artofillusion.math.RGBColor
import java.awt.Point

@ProceduralModule.Category("Modules:menu.colorFunctions")
class ColorEqualityModule @JvmOverloads constructor(position: Point? = Point()) : ProceduralModule<RGBToHSVModule?>(
    "Color Equality",
    arrayOf<IOPort>(
        ColorInputPort(IOPort.LEFT, *arrayOf<String>("Color 1", "(White)")),
        ColorInputPort(IOPort.LEFT, *arrayOf<String>("Color 2", "(Black)")),
    ),
    arrayOf<IOPort>(IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "True/False")),
    position) {

    override fun getAverageValue(which: Int, blur: Double): Double {

        var colorOne = RGBColor(1.0,1.0,1.0)
        var colorTwo = RGBColor(0.0,0.0,0.0)
        if(linkFrom[0] != null) linkFrom[0].getColor(0, colorOne, 0.0)
        if(linkFrom[1] != null) linkFrom[1].getColor(1, colorTwo, 0.0)

        return if(colorOne.equals(colorTwo)) 1.0 else 0.0
    }
}
