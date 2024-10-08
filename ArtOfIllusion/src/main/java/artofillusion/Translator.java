/* Copyright (C) 1999-2004 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import buoy.widget.*;

/**
 * The Translator interface defines the methods for importing and exporting scenes
 * in foreign file formats.
 */
public interface Translator {

    /**
     * Get the name of the file format which this translator imports or exports.
     */
    String getName();

    /**
     * Specify whether this translator can import files.
     */
    default boolean canImport() {
        return true;
    }

    /**
     * Specify whether this translator can export files.
     */
    default boolean canExport() {
        return true;
    }

    /**
     * Prompt the user to select a file, read it, and create a new LayoutWindow containing
     * the imported scene. parent is the Frame which should be used as the parent for
     * dialog boxes. If canImport() returns false, this method will never be called.
     */
    void importFile(BFrame parent);

    /**
     * Prompt the user for a filename and any other necessary information, and export the
     * scene. parent is the Frame which should be used as the parent for dialog boxes.
     * The user should be given the option of only exporting the objects which are
     * currently selected. If canExport() returns false, this method will never be called.
     */
    void exportFile(BFrame parent, Scene theScene);
}
