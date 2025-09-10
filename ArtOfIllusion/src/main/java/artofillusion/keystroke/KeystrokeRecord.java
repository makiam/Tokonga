/* Copyright (C) 2006-2013 by Peter Eastman
   Changes copyright (C) 2023-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.keystroke;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * This class contains information about a keyboard shortcut which automates some operation.
 * A keystroke pairs a key description (key code and modifier) with a script to execute
 * when the key is pressed.
 */
@Getter @Setter
@XStreamAlias("keystroke")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"script"})
public class KeystrokeRecord {
    @XStreamAsAttribute
    @XStreamAlias(value = "code")
    private int keyCode;
    @XStreamAsAttribute
    private int modifiers;

    @XStreamAsAttribute
    private String name;

    public String getGroup() {
        return Optional.ofNullable(group).orElse("");
    }

    @XStreamAsAttribute
    private String group;

    private String script;

    public KeystrokeRecord(int keyCode, int modifiers, String name, String script) {
        this(keyCode, modifiers, name, "", script);
    }

    public KeystrokeRecord(int keyCode, int modifiers, String name, String group, String script) {
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.name = name;
        this.group = group;
        this.script = script;
    }

    public KeyEventContainer getKeyEventKey() {
        return new KeyEventContainer(keyCode, modifiers);
    }


}
