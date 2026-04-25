/* Copyright (C) 2026 by Maksim Khramov
This program is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.ArtOfIllusion;
import artofillusion.Plugin;

import java.nio.file.Files;
import java.nio.file.Paths;

public class PMAssetsWatcher implements Plugin {
    @Override
    public void onApplicationStarting() {
        var path = Paths.get(ArtOfIllusion.PLUGIN_DIRECTORY, "PolyMeshTemplates");
        if(Files.exists(path)) return;
        path.toFile().mkdirs();
    }
}
