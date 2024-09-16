/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.tools

import artofillusion.math.RGBColor
import artofillusion.`object`.Object3D
import artofillusion.`object`.SpotLight
import artofillusion.ui.Translate
import java.util.Optional

class SpotLightFactory : PrimitiveFactory {
    override fun getName() = Translate.text("menu.spotLight")

    override fun getCategory() = "Lights"

    override fun create() = Optional.of<Object3D>(SpotLight(RGBColor(1.0f, 1.0f, 1.0f), 1.0f, 20.0, 0.0, 0.1))
}
