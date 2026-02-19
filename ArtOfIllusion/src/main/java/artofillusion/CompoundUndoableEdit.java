/* Copyright (C) 2025-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import java.util.LinkedList;

public final class CompoundUndoableEdit implements UndoableEdit {

    private final LinkedList<UndoableEdit> commands = new LinkedList<>();

    public void add(UndoableEdit edit) {
        commands.add(edit);
    }

    @Override
    public void undo() {
        commands.descendingIterator().forEachRemaining(command -> command.undo());
    }

    @Override
    public void redo() {
        commands.forEach(command -> command.execute());
    }

    @Override
    public String getName() {
        return commands.isEmpty() ? UndoableEdit.super.getName() : commands.get(0).getName();
    }


}
