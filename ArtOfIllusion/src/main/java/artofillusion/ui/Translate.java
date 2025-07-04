/* Copyright (C) 2003-2009 by Peter Eastman
   Changes copyright (C) 2018-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import buoy.event.*;
import buoy.widget.*;
import java.text.*;
import java.util.*;

import artofillusion.*;
import java.awt.event.ActionListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides utilities for localizing text so that it can be translated into
 * different languages. It does this by loading strings from a resource bundle, and
 * using them to create properly localized widgets.
 * <p>
 * The resource bundle is created from a {@link artofillusion.PluginRegistry.PluginResource PluginResource}
 * of type "TranslateBundle" provided by the {@link artofillusion.PluginRegistry PluginRegistry}.
 * By default it uses the PluginResource with ID "artofillusion" which is built into the application,
 * but you can specify a
 * different one by prefixing its ID to the property name passed to any method of this class.
 * This allows plugins to provide their own ResourceBundles for localizing their strings. To do
 * this, the plugin should include a set of properties files that define the localized versions
 * of its strings, such as:
 * <p>
 * com/mycompany/myplugin.properties<br>
 * com/mycompany/myplugin_fr.properties
 * com/mycompany/myplugin_es.properties
 * <p>
 * In its extensions.xml file, it then provides a reference to these files:
 * <p>
 * &lt;resource type="TranslateBundle" id="myplugin" name="com.mycompany.myplugin"/&gt;
 * <p>
 * To look up keys from that bundle, prefix the key with the ID specified in the &lt;resource&gt;
 * tag:
 * <p>
 * BLabel instructions = Translate.label("myplugin:instructionsLabel");
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Translate {

    private static final Set<Locale> availableLocales = new LinkedHashSet<>();
    public static final String MENU_RESOURCE_PREFIX = "menu.";
    public static final String MENU_SHORTCUT_RESOURCE_SUFFIX = ".shortcut";

    static {
        availableLocales.add(new Locale("af", "ZA"));
        availableLocales.add(Locale.SIMPLIFIED_CHINESE);
        availableLocales.add(Locale.TRADITIONAL_CHINESE);
        availableLocales.add(new Locale("da", "DK"));
        availableLocales.add(new Locale("nl", "NL"));
        availableLocales.add(Locale.US);
        availableLocales.add(new Locale("fi", "FI"));
        availableLocales.add(Locale.FRENCH);
        availableLocales.add(Locale.GERMAN);
        availableLocales.add(Locale.ITALIAN);
        availableLocales.add(Locale.JAPANESE);
        availableLocales.add(new Locale("pt", "BR"));
        availableLocales.add(new Locale("es", "ES"));
        availableLocales.add(new Locale("sv", "SE"));
        availableLocales.add(new Locale("vi", "VN"));
        availableLocales.add(new Locale("ru", "RU"));
    }

    /**
     * -- GETTER --
     *  Get the locale currently used for generating text.
     */
    @Getter
    private static Locale locale = availableLocales.contains(Locale.getDefault()) ? Locale.getDefault() : Locale.US;

    private static final Map<String, ResourceBundle> bundles = new HashMap<>();

    /**
     * Set the locale to be used for generating text.
     * @param value
     */
    public static void setLocale(Locale value) {
        locale = availableLocales.contains(value) ? value : Locale.US;
        bundles.clear();
    }

    /**
     * Get a list of the locales for which we have translations.
     * @return 
     */
    public static Locale[] getAvailableLocales() {
        return availableLocales.toArray(new Locale[0]);
    }


    private static String getValue(String key) throws MissingResourceException {
        return getValue(key, null, null);
    }

    private static String getValue(String key, String prefix) throws MissingResourceException {
        return getValue(key, prefix, null);
    }

    /**
     * Look up the value corresponding to a resource key.
     *
     * @param key the key specified by the user
     * @param prefix an optional prefix to prepend to the key
     * @param suffix an optional suffix to append to the key
     */
    private static String getValue(String key, String prefix, String suffix) throws MissingResourceException {
        String bundle;
        int colon = key.indexOf(':');
        if (colon == -1) {
            bundle = "artofillusion";
        } else {
            bundle = key.substring(0, colon);
            key = key.substring(colon + 1);
        }
        if (prefix != null && suffix != null) {
            key = prefix + key + suffix;
        } else if (prefix != null) {
            key = prefix + key;
        } else if (suffix != null) {
            key = key + suffix;
        }
        ResourceBundle resources = bundles.get(bundle);
        if (resources == null) {
            PluginRegistry.PluginResource plugin = PluginRegistry.getResource("TranslateBundle", bundle);
            if (plugin == null) {
                throw new MissingResourceException("No TranslateBundle defined", bundle, key);
            }
            resources = ResourceBundle.getBundle(plugin.getName(), locale, plugin.getClassLoader());
            bundles.put(bundle, resources);
        }
        return resources.getString(key);
    }

    /**
     * Get a BMenu whose text is given by the property "menu.(name)".
     */
    public static BMenu menu(String name) {
        String title = name;
        try {
            title = getValue(name, MENU_RESOURCE_PREFIX);
        } catch (MissingResourceException ex) {
            log.info("No menu resource found for: {}", name);
        }
        BMenu menu = new BMenu(title);
        menu.setName(name);
        return menu;
    }

    /**
     * Get a BMenuItem whose text is given by the property "menu.(name)".
     * If listener is not null, the specified method of it will be added to the BMenuItem as an
     * event link for CommandEvents, and the menu item's action command will be set to
     * (name). This also checks for a property called "menu.shortcut.(name)",
     * and if it is found, sets the menu shortcut accordingly.
     */
    public static BMenuItem menuItem(String name, Object listener, String method) {
        String command = name;
        try {
            command = getValue(name, MENU_RESOURCE_PREFIX);
        } catch (MissingResourceException ex) {
            log.info("No menu item resource found for: {}", name);
        }
        BMenuItem item = new BMenuItem(command);
        item.setActionCommand(name);
        try {
            String shortcut = getValue(name, MENU_RESOURCE_PREFIX, MENU_SHORTCUT_RESOURCE_SUFFIX);
            if (shortcut.length() > 1 && shortcut.charAt(0) == '^') {
                item.setShortcut(new Shortcut(shortcut.charAt(1), Shortcut.DEFAULT_MASK | Shortcut.SHIFT_MASK));
            } else if (shortcut.length() > 0) {
                item.setShortcut(new Shortcut(shortcut.charAt(0)));
            }
        } catch (MissingResourceException ex) {
            log.info("No menu item shortcut resource found for: {}", name);
        }
        if (listener != null) {
            item.addEventLink(CommandEvent.class, listener, method);
        }
        return item;
    }
    
    public static BMenuItem menuItem(String name, ActionListener al) {
        String command = name;
        try {
            command = getValue(name, MENU_RESOURCE_PREFIX);
        } catch (MissingResourceException ex) {
            log.info("No menu item resource found for: {}", name);
        }
        BMenuItem item = new BMenuItem(command);
        item.getComponent().setActionCommand(name);
        try {
            String shortcut = getValue(name, MENU_RESOURCE_PREFIX, MENU_SHORTCUT_RESOURCE_SUFFIX);
            if (shortcut.length() > 1 && shortcut.charAt(0) == '^') {
                item.setShortcut(new Shortcut(shortcut.charAt(1), Shortcut.DEFAULT_MASK | Shortcut.SHIFT_MASK));
            } else if (shortcut.length() > 0) {
                item.setShortcut(new Shortcut(shortcut.charAt(0)));
            }
        } catch (MissingResourceException ex) {
            log.info("No menu item shortcut resource found for: {}", name);
        }
        item.getComponent().addActionListener(al);
        return item;
    }
    
    /**
     * Get a BCheckBoxMenuItem whose text is given by the property "menu.(name)".
     * If listener is not null, the specified method of it will be added to the BCheckboxMenuItem as an
     * event link for CommandEvents. state specifies the initial state of the item.
     */
    public static BCheckBoxMenuItem checkboxMenuItem(String name, Object listener, String method, boolean state) {
        String command = name;
        try {
            command = getValue(name, MENU_RESOURCE_PREFIX);
        } catch (MissingResourceException ex) {
            log.info("No menu item resource found for menu: {}", name);
        }
        BCheckBoxMenuItem item = new BCheckBoxMenuItem(command, state);
        item.setActionCommand(name);
        try {
            String shortcut = getValue(name, MENU_RESOURCE_PREFIX, MENU_SHORTCUT_RESOURCE_SUFFIX);
            if (shortcut.length() > 1 && shortcut.charAt(0) == '^') {
                item.setShortcut(new Shortcut(shortcut.charAt(1), Shortcut.DEFAULT_MASK | Shortcut.SHIFT_MASK));
            } else if (shortcut.length() > 0) {
                item.setShortcut(new Shortcut(shortcut.charAt(0)));
            }
        } catch (MissingResourceException ex) {
            log.info("No menu shortcut item resource found for menu: {}", name);
        }
        if (listener != null) {
            item.addEventLink(CommandEvent.class, listener, method);
        }
        return item;
    }

    public static BCheckBoxMenuItem checkboxMenuItem(String name, ActionListener al, boolean state) {
        String command = name;
        try {
            command = getValue(name, MENU_RESOURCE_PREFIX);
        } catch (MissingResourceException ex) {
            log.info("No menu item resource found for menu: {}", name);
        }
        BCheckBoxMenuItem item = new BCheckBoxMenuItem(command, state);
        item.setActionCommand(name);
        try {
            String shortcut = getValue(name, MENU_RESOURCE_PREFIX, MENU_SHORTCUT_RESOURCE_SUFFIX);
            if (shortcut.length() > 1 && shortcut.charAt(0) == '^') {
                item.setShortcut(new Shortcut(shortcut.charAt(1), Shortcut.DEFAULT_MASK | Shortcut.SHIFT_MASK));
            } else if (shortcut.length() > 0) {
                item.setShortcut(new Shortcut(shortcut.charAt(0)));
            }
        } catch (MissingResourceException ex) {
            log.info("No menu shortcut item resource found for menu: {}", name);
        }
        item.getComponent().addActionListener(al);
        return item;
    }

    /**
     * Get a BButton whose text is given by the property "button.(name)".
     * If listener is not null, the specified method of it will be added to the BButton as an
     * event link for CommandEvents, and the menu item's action command will be set to
     * (name).
     */
    public static BButton button(String name, Object listener, String method) {
        return button(name, null, listener, method);
    }

    public static BButton button(String name, ActionListener al) {
        var b = button(name, null, (String)null);
        b.getComponent().addActionListener(al);
        return b;
    }

    /**
     * Get a BButton whose text is given by the property "button.(name)", with a suffix
     * appended to it. If listener is not null, the specified method of it will be added
     * to the BButton as an event link for CommandEvents, and the button's action command
     * will be set to (name).
     */
    public static BButton button(String name, String suffix, Object listener, String method) {
        String command = name;
        try {
            command = getValue(name, "button.");
        } catch (MissingResourceException ex) {
            log.info("No button resource found for: {}", name);
        }
        if (suffix != null) {
            command += suffix;
        }
        BButton b = new BButton(command);
        b.setActionCommand(name);
        if (listener != null) {
            b.addEventLink(CommandEvent.class, listener, method);
        }
        return b;
    }

    public static BButton button(String name, String suffix, ActionListener al) {
        var b = button(name, suffix, null, null);
        b.getComponent().addActionListener(al);
        return b;
    }

    /**
     * Get a BLabel whose text is given by the property "name". If the
     * property is not found, this simply uses name.
     */
    public static BLabel label(String name) {
        return label(name, null);
    }

    /**
     * Get a BLabel whose text is given by the property "name", with a suffix appended
     * to it. If the property is not found, this simply uses name.
     */
    public static BLabel label(String name, String suffix) {
        try {
            name = getValue(name, null);
        } catch (MissingResourceException ex) {
            log.info("No label resource found for: {}", name);
        }
        if (suffix != null) {
            name += suffix;
        }
        return new BLabel(name);
    }

    /**
     * Get the text given by the property "name". If the property is not
     * found, this simply returns name.
     */
    public static String text(String name) {
        try {
            return getValue(name);
        } catch (MissingResourceException ex) {
            return name;
        }
    }

    /**
     * Get the text given by the property "name". If the property is not
     * found, this simply uses name. That string and the args array are
     * then passed to MessageFormat.format() so that any variable fields
     * can be replaced with the correct values.
     */
    public static String text(String name, Object... args) {
        String pattern = name;
        try {
            pattern = getValue(name);
        } catch (MissingResourceException ex) {
            log.info("No text resource found for: {}", name);
        }
        return MessageFormat.format(pattern, args);
    }
}
