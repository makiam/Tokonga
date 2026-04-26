/* Copyright (C) 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppPath {


  private static String appPath;

  // A clever trick for getting the location of the jar file, which David Smiley
  // posted to the Apple java-dev mailing list on April 14, 2002.  It works on
  // most, but not all, platforms, so in case of a problem we fall back to using
  // user.dir.

  public static String getAppPath() {
    if (appPath == null) {
      String dir = System.getProperty("user.dir");
      URL loc = ArtOfIllusion.class.getResource("/artofillusion/ArtOfIllusion.class");
      if (loc != null && "jar".equals(loc.getProtocol())) {
        URL classUrl = ArtOfIllusion.class.getProtectionDomain().getCodeSource().getLocation();
        java.nio.file.Path pp = Paths.get(new File(classUrl.getPath()).getParent());
        if (pp.toFile().exists()) {
          dir = pp.toString();
        }
      }
      dir = Paths.get(dir).getParent().toString();
      appPath = dir;
    }
    return appPath;
  }
}
