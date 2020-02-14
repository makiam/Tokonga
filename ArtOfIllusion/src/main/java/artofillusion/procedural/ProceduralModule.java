/* Copyright (C) 2000-2011 by Peter Eastman
   Changes copyright (C) 2020 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.procedural;

import artofillusion.ui.Translate;
import java.awt.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This represents a module in a procedure. This is an abstract class, whose subclasses represent
 * specific kinds of modules.
 */
public class ProceduralModule extends artofillusion.procedural.Module {

    private static final Point defaultPos = new Point(0,0);
    
    private final Category category;
    {
        category = this.getClass().getAnnotation(Category.class);
    }
    
    public String getCategory() {
        return category == null ? "" : Translate.text(category.value());
    }
    
    public ProceduralModule(String name, IOPort[] input, IOPort[] output) {
        this(name, input, output, defaultPos);
    }
    
    public ProceduralModule(String name, IOPort[] input, IOPort[] output, Point position) {
        super(name, input, output, position);
    }
    
    @Target(value = ElementType.TYPE)
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface Category {

        String value();
    }
}
