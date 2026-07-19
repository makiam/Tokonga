/* Copyright (C) 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.object.Object3D;
import artofillusion.script.ScriptRunner;
import artofillusion.script.ScriptedObject;
import artofillusion.ui.Translate;

import java.util.Optional;

public class ScriptedObjectProvider implements PrimitiveFactory {
    @Override
    public String getCategory() {
        return "Scripting";
    }

    @Override
    public String getName() {
        return Translate.text("menu.createScriptObject");
    }

    @Override
    public Optional<Object3D> create() {
        ScriptedObject obj = new ScriptedObject("", ScriptRunner.Language.GROOVY.name);
        return Optional.of(obj);
    }

    @Override
    public String getObjectName() {
        return "Scripted object";
    }
}
