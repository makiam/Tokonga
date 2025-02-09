/* Copyright 2024-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural

import java.awt.Point

@ProceduralModule.Category("Modules:menu.colorFunctions")
class RGBToHSVModule @JvmOverloads constructor(position: Point? = Point()) : ProceduralModule<RGBToHSVModule?>(
    "RGB to HSV",
    arrayOf<IOPort>(
        IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, *arrayOf<String>("Red", "(0)")),
        IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, *arrayOf<String>("Green", "(0)")),
        IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, *arrayOf<String>("Blue", "(0)"))
    ),
    arrayOf<IOPort>(
        IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, *arrayOf<String>("Hue")),
        IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, *arrayOf<String>("Saturation")),
        IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, *arrayOf<String>("Value"))
    ),
    position) {
    override fun getAverageValue(which: Int, blur: Double): Double {
        val red: Double = this.linkFrom[0]?.getAverageValue(this.linkFromIndex[0], blur) ?: 0.0
        val green: Double = this.linkFrom[1]?.getAverageValue(this.linkFromIndex[1], blur) ?: 0.0
        val blue: Double  = this.linkFrom[2]?.getAverageValue(this.linkFromIndex[2], blur) ?: 0.0
        return rgbToHsv(red, green, blue, which)
    }
    companion object {
        @JvmStatic
        fun rgbToHsv(red: Double, green: Double, blue: Double): HSVColor {
            val min = minOf(red, green, blue)
            val max = maxOf(red, green, blue)
            val delta = max - min

            var hue = when {
                delta == 0.0 -> 0.0
                red == max -> 60.0 * (green - blue) / delta
                green == max -> 60.0 * (blue - red) / delta + 120.0
                else -> 60.0 * (red - green) / delta + 240.0
            }

            val saturation = if (max == 0.0) 0.0 else delta / max

            if (hue < 0.0) hue += 360.0
            return HSVColor(hue, saturation, max)
        }

        @JvmStatic
        fun rgbToHsv(red: Double, green: Double, blue: Double, index: Int): Double {
            val (hue, saturation, value) = rgbToHsv(red, green, blue)
            return when (index) {
                0 -> hue / 360
                1 -> saturation
                2 -> value
                else -> 0.0
            }
        }
    }


}

data class HSVColor(val hue: Double, val saturation: Double, val value: Double)

