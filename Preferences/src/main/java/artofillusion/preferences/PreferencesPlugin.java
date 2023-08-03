/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.preferences;
import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import lombok.extern.slf4j.Slf4j;
import buoy.widget.BMenuItem;
import javax.swing.JMenuItem;
import artofillusion.PluginRegistry;
import artofillusion.ui.Translate;

/**
 *
 * @author MaksK
 */
@Slf4j
public class PreferencesPlugin implements Plugin {

    @Override
    public void onApplicationStarting() {

    }

    @Override
    public void onSceneWindowCreated(LayoutWindow view) {
        log.info("Installing plugin preferences menu...");
        BMenuItem pluginPreferencesMenu = new BMenuItem();
        JMenuItem mc = pluginPreferencesMenu.getComponent();
        mc.putClientProperty("view", view.getComponent());
        // mc.setAction(actionShowPluginPreferences);
        mc.setText(Translate.text("PreferencesPlugin:menu.pluginPreferences"));
        mc.setEnabled(!PluginRegistry.getPlugins(PreferencesEditor.class).isEmpty());
        view.getMenuBar().getChild(1).add(pluginPreferencesMenu);
    }

    @Override
    void onApplicationStopping() {
    }

    @Override
    protected void onSceneSaved(File file, LayoutWindow view) {
        try {
            //commit();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Unable to commit preferences", ioe);
        }
    }

    public static void putString(String owner, String name, String value) {
        getPreferences(owner).putString(name, value);
    }

    public static String getString(String owner, String name) {
        return getPreferences(owner).getString(name);
    }

    public static void putBoolean(String owner, String name, boolean value) {
        getPreferences(owner).putBoolean(name, value);
    }

    public static boolean getBoolean(String owner, String name) {
        return getPreferences(owner).getBoolean(name);
    }

    public static void putInt(String owner, String name, int value) {
        getPreferences(owner).putInt(name, value);
    }

    public static int getInt(String owner, String name) {
        return getPreferences(owner).getInt(name);
    }

    public static void putLong(String owner, String name, long value) {
        getPreferences(owner).putLong(name, value);
    }

    public static long getLong(String owner, String name) {
        return getPreferences(owner).getLong(name);
    }

    public static void putFloat(String owner, String name, float value) {
        getPreferences(owner).putFloat(name, value);
    }

    public static float getFloat(String owner, String name) {
        return getPreferences(owner).getFloat(name);
    }

    public static void putDouble(String owner, String name, double value) {
        getPreferences(owner).putDouble(name, value);
    }

    public static double getDouble(String owner, String name) {
        return getPreferences(owner).getDouble(name);
    }

    public static void putArray(String owner, String name, Object value) {
        getPreferences(owner).putArray(name, value);
    }

    public static Object getArray(String owner, String name, Object result) {
        return getPreferences(owner).getArray(name, result);
    }

    public static void putColor(String owner, String name, Color value) {
        getPreferences(owner).putColor(name, value);
    }

    public static Color getColor(String owner, String name) {
        return getPreferences(owner).getColor(name);
    }
}
