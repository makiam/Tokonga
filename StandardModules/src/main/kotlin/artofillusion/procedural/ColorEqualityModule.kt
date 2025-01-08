/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural

import artofillusion.procedural.RGBToHSVModule.Companion.rgbToHsv
import java.awt.Point

@ProceduralModule.Category(value = "Modules:menu.colorFunctions")
class ColorEqualityModule @JvmOverloads constructor(position: Point? = Point()) : ProceduralModule<RGBToHSVModule?>(
    "Color Equality",
    arrayOf<IOPort>(
        IOPort(IOPort.COLOR, IOPort.INPUT, IOPort.LEFT, *arrayOf<String>("Red", "(0)")),
        IOPort(IOPort.COLOR, IOPort.INPUT, IOPort.LEFT, *arrayOf<String>("Green", "(0)")),
        IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, *arrayOf<String>("Tolerance", "(0)"))
    ),
    arrayOf<IOPort>(),
    position) {
    override fun getAverageValue(which: Int, blur: Double): Double {

        return 0.0;
    }
}