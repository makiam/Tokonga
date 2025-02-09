/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural

import java.awt.Point

@ProceduralModule.Category(value = "Modules:menu.operators")
class NumberEqualityModule @JvmOverloads constructor(position: Point? = Point()): ProceduralModule<NumberEqualityModule?>(    "Number Equality",
    arrayOf<IOPort>(
        NumericInputPort(IOPort.LEFT, *arrayOf<String>("Value 1", "(0)")),
        NumericInputPort(IOPort.LEFT, *arrayOf<String>("Value 2", "(0)")),
        NumericInputPort(IOPort.LEFT, *arrayOf<String>("Tolerance", "(1.0E-12D)"))
    ),
    arrayOf<IOPort>(IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "True/False")),
    position) {

    override fun getAverageValue(which: Int, blur: Double): Double {
        if (linkFrom[0] == null || linkFrom[1] == null) return 0.0
        val ldv0 = linkFrom[0].getAverageValue(0, 0.0 );
        val ldv1 = linkFrom[1].getAverageValue(1, 0.0 );
        val tolerance = if (linkFrom[2] == null) NumberEqualityModule.defaultTolerance else linkFrom[2].getAverageValue(2, 0.0)
        return if (Math.abs(ldv0 - ldv1) < tolerance) 1.0 else 0.0
    }

    companion object {
        const val defaultTolerance = 1.0E-12
    }


}