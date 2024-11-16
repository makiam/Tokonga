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
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamConverter
import com.thoughtworks.xstream.annotations.XStreamImplicit
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter
import java.awt.event.KeyEvent

/**
 * This class contains information about a keyboard shortcut which automates some operation.
 * A keystroke pairs a key description (key code and modifier) with a script to execute
 * when the key is pressed.
 */
@XStreamAlias("keystroke")
@XStreamConverter(ToAttributedValueConverter::class, strings = ["script"])
data class KeystrokeRecord(@XStreamAlias("code") @XStreamAsAttribute val keyCode: Int,
                           @XStreamAsAttribute val modifiers: Int = 0,
                           @XStreamAsAttribute val name: String?,
                           val script: String?) {
    fun KeyEventKey(): KeyEventContainer = KeyEventContainer(keyCode, modifiers)
}

@XStreamAlias("keystrokes")
data class KeystrokesList(@XStreamImplicit(itemFieldName = "keystroke") val records: List<KeystrokeRecord>)

data class KeyEventContainer(val keyCode: Int, val modifiers: Int) {
    constructor(event: KeyEvent) : this(event.keyCode, event.modifiersEx)
}
