/* Copyright (C) 2006-2013 by Peter Eastman
   Changes copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.keystroke;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * This class contains information about a keyboard shortcut which automates some operation.
 * A keystroke pairs a key description (key code and modifier) with a Beanshell script to execute
 * when the key is pressed.
 */
@Data
@AllArgsConstructor
public class KeystrokeRecord {

    private int keyCode;
    private int modifiers;
    private String name;
    private String script;

    /**
     * Create an exact duplicate of this record.
     */
    public KeystrokeRecord duplicate() {
        return new KeystrokeRecord(keyCode, modifiers, name, script);
    }
}
