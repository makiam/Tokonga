/* Copyright (C) 1999-2012 by Peter Eastman
   Changes copyright (C) 2017-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import java.util.*;

/**
 * This class maintains a stack of UndoRecords for a window. It also automatically
 * records the redo records generated when they are executed.
 */
public final class UndoStack {

    private final LinkedList<UndoRecord> undoList = new LinkedList<>();
    private final LinkedList<UndoRecord> redoList = new LinkedList<>();

    ;

  /**
   * Determine whether there are any undo records available, so that an Undo command
   * could be executed.
   */

  public boolean canUndo() {
        return !undoList.isEmpty();
    }

    /**
     * Determine whether there are any redo records available, so that a Redo command
     * could be executed.
     */
    public boolean canRedo() {
        return !redoList.isEmpty();
    }

    /**
     * Add an UndoRecord to the stack.
     */
    public void addRecord(UndoRecord record) {
        int levels = ArtOfIllusion.getPreferences().getUndoLevels();
        if (levels < 1) {
            levels = 1;
        }
        while (undoList.size() >= levels) {
            undoList.removeFirst();
        }
        undoList.add(record);
        redoList.clear();
        record.cacheToDisk();
    }

    /**
     * Execute the undo record at the top of the stack.
     */
    public void executeUndo() {
        if (undoList.isEmpty()) {
            return;
        }
        UndoRecord record = undoList.removeLast();
        redoList.add(record.execute());
    }

    /**
     * Execute the redo record at the top of the stack.
     */
    public void executeRedo() {
        if (redoList.isEmpty()) {
            return;
        }
        UndoRecord record = redoList.removeLast();
        undoList.add(record.execute());
    }

    public String getRedoName() {
        return redoList.isEmpty() ? "" : redoList.getLast().getName();
    }

    public String getUndoName() {
        return undoList.isEmpty() ? "" : undoList.getLast().getName();
    }
}
