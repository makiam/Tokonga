/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.preferences;

import artofillusion.*;
import artofillusion.ui.Translate;
import buoy.widget.BMenuItem;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

@Slf4j
public class PreferencesPlugin implements Plugin {
    protected static Map<String, DataMap> prefs = new HashMap<>();

    protected static File prefdir;

    protected static Thread updater;

    protected static final Integer semaphore = 0;

    @Override
    public void onSceneWindowCreated(LayoutWindow view) {
        log.info("Installing plugin preferences menu...");
        BMenuItem pluginPreferencesMenu = new BMenuItem();
        JMenuItem mc = pluginPreferencesMenu.getComponent();
        mc.putClientProperty("view", view);
        mc.setAction(actionShowPluginPreferences);

        mc.setText(Translate.text("PreferencesPlugin:menu.pluginPreferences"));
        mc.setEnabled(!PluginRegistry.getPlugins(PreferencesEditor.class).isEmpty());
        view.getMenuBar().getChild(1).add(pluginPreferencesMenu);

    }

    @Override
    public void onApplicationStarting() {
        updater = new UpdateThread(semaphore);
        updater.setDaemon(true);
        updater.start();
    }

    private final AbstractAction actionShowPluginPreferences = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent event) {
            final LayoutWindow owner = (LayoutWindow)((JMenuItem)event.getSource()).getClientProperty("view");
            new PreferencesWindow(owner);
        }

    };
    @Override
    public void onSceneSaved(File file, LayoutWindow view) {
        try {
            commit();
        } catch (IOException ioe) {
            log.atError().setCause(ioe).log("Unable to commit preferences: {}", ioe.getMessage());
        }
    }

    @Override
    public void onApplicationStopping() {
        updater.interrupt();
        try {
            commit();
        } catch (IOException e) {
        }
    }

    public static DataMap getPreferences(String owner) {
        log.atInfo().log("PreferencesPlugin: Getting preferences: {}", owner);

        DataMap result = prefs.get(owner);
        if (result != null) {
            return result;
        }
        PluginRegistry.PluginResource resource = PluginRegistry.getResource("Preferences", owner);

        Properties init = null;
        if (resource != null)
            try(InputStream is = resource.getInputStream()) {
                init = new Properties();
                init.load(is);
            } catch (Exception e) {
            }
        result = getPreferences(owner, init);
        return result;
    }

    public static DataMap getPreferences(String owner, String parent) {
        log.debug("PreferencesPlugin: Getting preferences: {}, {}", owner, parent);
        return owner.equals(parent) ? getPreferences(owner) : getPreferences(owner, getPreferences(parent));
    }

    @SuppressWarnings("unchecked")
    public static DataMap getPreferences(String owner, Map init) {
        log.debug("PreferencesPlugin: Getting persistent preferences: {}", owner);
        DataMap result = prefs.get(owner);
        if (result != null) {
            return result;
        }
        if (init == null) {
            result = new DataMap();
        } else {
            result = new DataMap(init);
        }
        if (prefdir == null) {

            prefdir = new File(ApplicationPreferences.getPreferencesFolderPath().toFile(), "Plugins");
            if (!prefdir.exists() && !prefdir.mkdir()) {
                log.atError().log("PreferencesPlugin: could not create dir: {}", prefdir.getAbsolutePath());
            }
        }
        File preffile = null;
        if (prefdir.exists()) {
            preffile = new File(prefdir, owner + ".properties");
            result.open(preffile, semaphore);
            try {
                result.load();
                result.commit();
            } catch (IOException e) {
                log.atError().setCause(e).log("PreferencesPlugin.getPreferences error: {}", e.getMessage());
            }
        }
        prefs.put(owner, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static void getDefaultPreferences(String owner, DataMap map) {
        PluginRegistry.PluginResource rsrc = PluginRegistry.getResource("Preferences", owner);
        if (rsrc != null) {

            try (InputStream is = rsrc.getInputStream()) {

                Properties props = new Properties();
                props.load(is);
                map.putAll((Map)props);
                log.info("PreferencesPlugin.reloadDefaults: reloaded defaults for: {}", owner);
            } catch (IOException e) {
                log.atError().setCause(e).log("PreferencesPlugin.reloadDefaults: Error reloading preferences defaults: {}: {}",owner, e.getMessage());
            }
        }
    }

    public static void commit() throws IOException {
        if (prefs == null || prefs.isEmpty()) {
            return;
        }
        final List<String> errors = new ArrayList<>();

        prefs.forEach((String k, DataMap v) -> {
            try {
                v.commit();
            } catch (IOException ioe) {
                errors.add(k);
            }
        });
        if (errors.isEmpty()) {
            return;
        }
        throw new IOException("PreferencesPlugin error comitting: " + String.join("\n", errors));

    }

    @PluginRegistry.UsedViaReflection
    @SuppressWarnings("unused")
    public static void commit(String owner) throws IOException {
        if (prefs == null || prefs.isEmpty()) {
            log.info("No preferences set");
            return;
        }
        DataMap map = prefs.get(owner);
        if (map == null) {
            log.info("No preferences for {}", owner);
            return;
        }
        map.commit();
    }

    @PluginRegistry.UsedViaReflection
    public static void putString(String owner, String name, String value) {
        getPreferences(owner).putString(name, value);
    }
    @PluginRegistry.UsedViaReflection
    public static String getString(String owner, String name) {
        return getPreferences(owner).getString(name);
    }
    @PluginRegistry.UsedViaReflection
    public static void putBoolean(String owner, String name, boolean value) {
        getPreferences(owner).putBoolean(name, value);
    }
    @PluginRegistry.UsedViaReflection
    public static boolean getBoolean(String owner, String name) {
        return getPreferences(owner).getBoolean(name);
    }
    @PluginRegistry.UsedViaReflection
    public static void putInt(String owner, String name, int value) {
        getPreferences(owner).putInt(name, value);
    }
    @PluginRegistry.UsedViaReflection
    public static int getInt(String owner, String name) {
        return getPreferences(owner).getInt(name);
    }
    @PluginRegistry.UsedViaReflection
    public static void putLong(String owner, String name, long value) {
        getPreferences(owner).putLong(name, value);
    }
    @PluginRegistry.UsedViaReflection
    public static long getLong(String owner, String name) {
        return getPreferences(owner).getLong(name);
    }
    @PluginRegistry.UsedViaReflection
    public static void putFloat(String owner, String name, float value) {
        getPreferences(owner).putFloat(name, value);
    }
    @PluginRegistry.UsedViaReflection
    public static float getFloat(String owner, String name) {
        return getPreferences(owner).getFloat(name);
    }
    @PluginRegistry.UsedViaReflection
    public static void putDouble(String owner, String name, double value) {
        getPreferences(owner).putDouble(name, value);
    }
    @PluginRegistry.UsedViaReflection
    public static double getDouble(String owner, String name) {
        return getPreferences(owner).getDouble(name);
    }
    @PluginRegistry.UsedViaReflection
    public static void putArray(String owner, String name, Object value) {
        getPreferences(owner).putArray(name, value);
    }

    @PluginRegistry.UsedViaReflection
    public static Object getArray(String owner, String name, Object result) {
        return getPreferences(owner).getArray(name, result);
    }
    @PluginRegistry.UsedViaReflection
    public static void putColor(String owner, String name, Color value) {
        getPreferences(owner).putColor(name, value);
    }
    @PluginRegistry.UsedViaReflection
    public static Color getColor(String owner, String name) {
        return getPreferences(owner).getColor(name);
    }

    public static class UpdateThread extends Thread {

        protected Object semaphore = null;

        public UpdateThread(Object semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (this.semaphore) {
                    try {
                        this.semaphore.wait(300000L);
                    } catch (InterruptedException e) {
                        log.atInfo().setCause(e).log("Wait Interrupted");
                        break;
                    } catch (Exception e) {
                    }
                }
                try {
                    sleep(30000L);
                } catch (InterruptedException e) {
                }
                try {
                    PreferencesPlugin.commit();
                } catch (Exception e) {
                    log.atError().setCause(e).log("Update error: {}", e.getMessage() );
                }
            }
            log.info("Exiting updater...");
        }
    }
}
