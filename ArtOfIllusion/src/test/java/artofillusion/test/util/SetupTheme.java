/* Copyright (C) 2023-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.test.util;

import artofillusion.ArtOfIllusion;
import artofillusion.PluginRegistry;
import artofillusion.ui.ThemeManager;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 *
 * @author MaksK
 */
public class SetupTheme implements BeforeAllCallback {
    private static int ref = 0;

    protected void before() {
        if (ref != 0) {
            return;
        }
        ref++;
        try {
            PluginRegistry.registerResource("UITheme", "default", ArtOfIllusion.class.getClassLoader(), "artofillusion/Icons/defaultTheme.xml", null);
            ThemeManager.initThemes();
        } catch (IllegalArgumentException iae) {
            ref++;
        }

    }

    @Override
    public void beforeAll(ExtensionContext context) {
        before();
    }
    
}
