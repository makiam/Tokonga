/* Copyright (C) 2006-2013 by Peter Eastman
   Changes copyright (C) 2023-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.keystroke

import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamImplicit
import java.awt.event.KeyEvent

@XStreamAlias("keystrokes")
data class KeystrokesList(@XStreamImplicit(itemFieldName = "keystroke") val records: List<KeystrokeRecord>)

data class KeyEventContainer(val keyCode: Int, val modifiers: Int) {
    constructor(event: KeyEvent) : this(event.keyCode, event.modifiersEx)
}
