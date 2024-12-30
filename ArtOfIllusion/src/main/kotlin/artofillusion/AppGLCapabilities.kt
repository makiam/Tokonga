/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion

import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import org.slf4j.LoggerFactory

object AppGLCapabilities {
    private val logger = LoggerFactory.getLogger(AppGLCapabilities::class.java)

    val capabilities: GLCapabilities by lazy {
        val profile = GLProfile.getMaxProgrammable(true)
        var caps = GLCapabilities(profile)
        logger.debug("GL Profile: $profile Caps: $caps")
        caps
    }

}

