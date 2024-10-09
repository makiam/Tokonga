/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion

import java.io.File
import java.net.URL
import java.nio.file.Paths

object AppPath {
    // A clever trick for getting the location of the jar file, which David Smiley
    // posted to the Apple java-dev mailing list on April 14, 2002.  It works on
    // most, but not all, platforms, so in case of a problem we fall back to using
    // user.dir.

    val appPath: String by lazy {
        var dir: String = System.getProperty("user.dir")
        val loc = ArtOfIllusion::class.java.getResource("/artofillusion/ArtOfIllusion.class")
        if (loc.protocol == "jar") {
            val classUrl: URL = ArtOfIllusion::class.java.protectionDomain.codeSource.location
            val pp = Paths.get(File(classUrl.path).parent)
            if (pp.toFile().exists()) {
                dir = pp.toString()
            }
        }
        dir = Paths.get(dir).parent.toString()
        dir
    }
}

