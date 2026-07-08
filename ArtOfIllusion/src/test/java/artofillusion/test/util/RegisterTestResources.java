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

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 * @author MaksK
 */
public class RegisterTestResources implements BeforeAllCallback {

    private static final AtomicBoolean registered = new AtomicBoolean(false);

    @Override
    public void beforeAll(ExtensionContext context) {
        if (registered.compareAndSet(false, true)) {
            try {
                PluginRegistry.registerResource("TranslateBundle", "artofillusion", ArtOfIllusion.class.getClassLoader(), "artofillusion", null);
            } catch (IllegalArgumentException iae) {
                // Already registered by application startup or previous test class
            }
        }
    }
}
