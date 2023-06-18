/*
 *  Copyright (C) 2003 by Francois Guillet
 *  Copyright (C) 2003 by Peter Eastman for original Translate.java code
 *  Changes copyright 2023 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.spmanager;

import artofillusion.ui.Translate;
import buoy.event.*;
import buoy.widget.*;
import java.text.*;
import java.util.*;

/**
 * This class extends AoI Translate Class so that i) the spmanager properties
 * file is used instead of AoI properties file ii) buoy objects are returned
 *
 * @author pims
 * @created 27 mai 2004
 */
public class SPMTranslate {

    private static ResourceBundle resources;

    /**
     * Set the locale to be used for generating text.
     *
     * @param l The new locale value
     */
    public static void setLocale(Locale l) {
        resources = ResourceBundle.getBundle("spmanager", l);
    }

    /**
     * Description of the Method
     *
     * @param name Description of the Parameter
     * @param target Description of the Parameter
     * @param method Description of the Parameter
     * @return Description of the Return Value
     */
    public static BButton bButton(String name, java.lang.Object target, String method) {
        return Translate.button("spmanager:" + name, target, method);
    }

    /**
     * Description of the Method
     *
     * @param name Description of the Parameter
     * @param state Description of the Parameter
     * @param target Description of the Parameter
     * @param method Description of the Parameter
     * @return Description of the Return Value
     */
    public static BCheckBox bCheckBox(String name, boolean state, Object target, String method) {
        String command = name;
        try {
            command = resources.getString("checkbox." + name);
        } catch (MissingResourceException ex) {
        }
        BCheckBox b = new BCheckBox(command, state);
        if (target != null) {
            b.addEventLink(ValueChangedEvent.class, target, method);
        }
        return b;
    }

    /**
     * Description of the Method
     *
     * @param name Description of the Parameter
     * @param args Description of the Parameter
     * @return Description of the Return Value
     */
    public static BLabel bLabel(String name, Object... args) {
        try {
            name = resources.getString("label." + name);
        } catch (MissingResourceException ex) {
        }
        if (args != null) {
            return new BLabel(MessageFormat.format(name, args));
        }
        return new BLabel(name);
    }

    /**
     * Get the text given by the property "name". If the property is not found,
     * this simply returns name.
     *
     * @param name Description of the Parameter
     * @return Description of the Return Value
     */
    public static String text(String name) {
        try {
            return resources.getString("text." + name);
        } catch (MissingResourceException ex) {
            return name;
        }
    }

    /**
     * Get the text given by the property "name". If the property is not found,
     * this simply uses name. Any occurrance of the pattern "{0}" in the text
     * string will be replaced with the string representation of arg.
     *
     * @param name Description of the Parameter
     * @param arg Description of the Parameter
     * @return Description of the Return Value
     */
    public static String text(String name, Object arg) {
        String pattern = name;
        try {
            pattern = resources.getString("text." + name);
        } catch (MissingResourceException ex) {
        }
        return MessageFormat.format(pattern, arg);
    }

}
