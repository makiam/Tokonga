/* Copyright (C) 2000-2011 by Peter Eastman
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.Scene;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
/**
 * This represents a module in a procedure. This is an abstract class, whose
 * subclasses represent specific kinds of modules.
 */
public class ProceduralModule<P extends Module> extends artofillusion.procedural.Module<P> {

    public ProceduralModule(String name, IOPort[] input, IOPort[] output, Point position) {
        super(name, input, output, position);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        super.writeToStream(out, theScene);
    }

    public Dimension getDimension() {
        return new Dimension(bounds.height, bounds.width);
    }

    public List<java.util.function.Supplier<ProceduralModule<?>>> getModuleSuppliers() {
        return List.of();
    }

    @Target(value = ElementType.TYPE)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface Category {

        String value();
    }
}
