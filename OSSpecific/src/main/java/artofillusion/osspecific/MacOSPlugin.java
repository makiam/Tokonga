/* Copyright (C) 2002-2020 by Peter Eastman
   Changes copyright (C) 2017-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.osspecific;

import artofillusion.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.io.*;
import java.lang.reflect.*;
import java.util.Locale;
import java.util.prefs.*;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a plugin to make Art of Illusion behave more like a standard Macintosh
 * application when running under Mac OS X.
 */
@Slf4j
public class MacOSPlugin implements Plugin, AboutHandler, QuitHandler, OpenFilesHandler, PreferencesHandler {

    private boolean usingAppMenu, appleApi;
    private static final String OS = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);

    @Override
    public void onSceneSaved(File file, LayoutWindow view) {
        MacOSPlugin.updateWindowProperties(view);
        view.getComponent().getRootPane().putClientProperty("Window.documentModified", false);
    }

    @Override
    public void onSceneWindowCreated(LayoutWindow view) {

    }

    @Override
    public void onApplicationStarting() {
        if (!OS.startsWith("mac os x")) return;
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        ArtOfIllusion.addWindow(new MacMenuBarWindow());
        UIUtilities.setDefaultFont(new Font("Application", Font.PLAIN, 11));
        UIUtilities.setStandardDialogInsets(3);

        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(this);
        desktop.setQuitHandler(this);
        desktop.setOpenFileHandler(this);
        desktop.setPreferencesHandler(this);
    }

    @Override
    public void processMessage(int message, Object... args) {
        if (message == APPLICATION_STARTING) {
            String os = ((String) System.getProperties().get("os.name")).toLowerCase();
            if (!os.startsWith("mac os x")) {
                return;
            }

            try {
                if (System.getProperty("java.version").startsWith("1.8.")) {
                    // Use the old Apple specific API.

                    appleApi = true;
                    Class<?> appClass = Class.forName("com.apple.eawt.Application");
                    Object app = appClass.getMethod("getApplication").invoke(null);
                    appClass.getMethod("setEnabledAboutMenu", Boolean.TYPE).invoke(app, Boolean.TRUE);
                    appClass.getMethod("setEnabledPreferencesMenu", Boolean.TYPE).invoke(app, Boolean.TRUE);
                    Class<?> listenerClass = Class.forName("com.apple.eawt.ApplicationListener");
                    Object proxy = Proxy.newProxyInstance(appClass.getClassLoader(), new Class<?>[]{listenerClass}, this);
                    appClass.getMethod("addApplicationListener", listenerClass).invoke(app, proxy);
                } else {
                    // Use the Desktop API introduced in Java 9.

                    appleApi = false;
                    Class<?> aboutHandlerClass = Class.forName("java.awt.desktop.AboutHandler");
                    Class<?> openHandlerClass = Class.forName("java.awt.desktop.OpenFilesHandler");
                    Class<?> preferencesHandlerClass = Class.forName("java.awt.desktop.PreferencesHandler");
                    Class<?> quiteHandlerClass = Class.forName("java.awt.desktop.QuitHandler");
                    Object proxy = Proxy.newProxyInstance(Desktop.class.getClassLoader(), new Class<?>[]{aboutHandlerClass, openHandlerClass, preferencesHandlerClass, quiteHandlerClass}, this);
                    Desktop desktop = Desktop.getDesktop();
                    Desktop.class.getMethod("setAboutHandler", aboutHandlerClass).invoke(desktop, proxy);
                    Desktop.class.getMethod("setOpenFileHandler", openHandlerClass).invoke(desktop, proxy);
                    Desktop.class.getMethod("setPreferencesHandler", preferencesHandlerClass).invoke(desktop, proxy);
                    Desktop.class.getMethod("setQuitHandler", quiteHandlerClass).invoke(desktop, proxy);
                }
            } catch (ReflectiveOperationException | SecurityException ex) {
                // An error occured trying to set up the application menu, so just stick with the standard
                // Quit and Preferences menu items in the File and Edit menus.
                log.atError().setCause(ex).log("Unable to start plugin: {}", ex.getMessage());
            }
            usingAppMenu = true;
        } else if (message == SCENE_WINDOW_CREATED) {
            final LayoutWindow win = (LayoutWindow) args[0];
            win.addEventLink(SceneChangedEvent.class, new Object() {
                void processEvent() {
                    updateWindowProperties(win);
                }
            });
            updateWindowProperties(win);
            if (!usingAppMenu) {
                return;
            }

            // Remove the Quit and Preferences menu items, since we are using the ones in the application
            // menu instead.
            removeMenuItem(win, Translate.text("menu.file"), Translate.text("menu.quit"));
            removeMenuItem(win, Translate.text("menu.edit"), Translate.text("menu.preferences"));
        } else if (message == SCENE_SAVED) {
            LayoutWindow win = (LayoutWindow) args[1];
            updateWindowProperties(win);
            win.getComponent().getRootPane().putClientProperty("Window.documentModified", false);
        }
    }

    /**
     * Update the Mac OS X specific client properties.
     */
    private static void updateWindowProperties(LayoutWindow win) {
        javax.swing.JRootPane rp = win.getComponent().getRootPane();
        rp.putClientProperty("Window.documentModified", win.isModified());
        Scene scene = win.getScene();
        if (scene.getName() != null) {
            File file = new File(scene.getDirectory(), scene.getName());
            rp.putClientProperty("Window.documentFile", file);
        }
    }

    /**
     * Remove a menu item from a menu in a window. If it is immediately preceded by a separator,
     * also remove that.
     */
    private void removeMenuItem(BFrame frame, String menu, String item) {
        BMenuBar bar = frame.getMenuBar();
        for (int i = 0; i < bar.getChildCount(); i++) {
            BMenu m = bar.getChild(i);
            if (!m.getText().equals(menu)) {
                continue;
            }
            for (int j = 0; j < m.getChildCount(); j++) {
                MenuWidget w = m.getChild(j);
                if (w instanceof BMenuItem && ((BMenuItem) w).getText().equals(item)) {
                    m.remove((Widget) w);
                    if (j > 0 && m.getChild(j - 1) instanceof BSeparator) {
                        m.remove((Widget) m.getChild(j - 1));
                    }
                    return;
                }
            }
            return;
        }
    }

    /**
     * Handle ApplicationListener methods.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        boolean handled = true;
        if ("handlePreferences".equals(method.getName())) {
            Window frontWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
            boolean frontIsLayoutWindow = false;
            for (EditingWindow window : ArtOfIllusion.getWindows()) {
                if (window instanceof LayoutWindow && window.getFrame().getComponent() == frontWindow) {
                    ((LayoutWindow) window).preferencesCommand();
                    frontIsLayoutWindow = true;
                    break;
                }
            }
            if (!frontIsLayoutWindow) {
                BFrame f = new BFrame();
                Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                f.setBounds(screenBounds);
                UIUtilities.centerWindow(f);
                new PreferencesWindow(f);
                f.dispose();
            }
        } else if ("handleQuitRequestWith".equals(method.getName())) {
            ArtOfIllusion.quit();
            handled = false;
            try {
                Method cancelQuit = args[1].getClass().getMethod("cancelQuit");
                cancelQuit.invoke(args[1]);
            } catch (ReflectiveOperationException | SecurityException ex) {
                // Nothing we can really do about it...

                log.atError().setCause(ex).log("Unable to handle Quit command: {}", ex.getMessage());
            }
        } else if ("openFiles".equals(method.getName())) {
            try {
                Method getFiles = args[0].getClass().getMethod("getFiles");
                java.util.List<File> files = (java.util.List<File>) getFiles.invoke(args[0]);
                for (File file : files) {
                    ArtOfIllusion.newWindow(new Scene(file, true));
                }
            } catch (IOException | ReflectiveOperationException | SecurityException ex) {
                // Nothing we can really do about it...

                log.atError().setCause(ex).log("Unable to load scenes: {}", ex.getMessage());
            }
        } else {
            return null;
        }

        // Call setHandled(true) on the event to show we have handled it.
        if (appleApi) {
            try {
                Method setHandled = args[0].getClass().getMethod("setHandled", Boolean.TYPE);
                setHandled.invoke(args[0], handled);
            } catch (ReflectiveOperationException | SecurityException ex) {
                // Nothing we can really do about it...

                log.atError().setCause(ex).log("Unable to set handled: {}", ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public void handleAbout(AboutEvent e) {
        TitleWindow win = new TitleWindow();
        win.addEventLink(MouseClickedEvent.class, win, "dispose");
    }

    @Override
    public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
        response.cancelQuit();
    }

    @Override
    public void openFiles(OpenFilesEvent event) {
        for(var file: event.getFiles()) {
            try {
                ArtOfIllusion.newWindow(new Scene(file, true));
            } catch (IOException ex) {
                log.atError().setCause(ex).log("Error opening scene: {} {}", file, ex.getMessage());
            }
        }
    }

    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void handlePreferences(PreferencesEvent e) {

    }

    /**
     * This is an inner class used to provide a minimal menu bar when all windows are
     * closed.
     */
    private class MacMenuBarWindow extends BFrame implements EditingWindow {

        public MacMenuBarWindow() {
            super();
            getComponent().setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));
            BMenuBar menubar = new BMenuBar();
            setMenuBar(menubar);
            BMenu file = Translate.menu("file");
            menubar.add(file);
            file.add(Translate.menuItem("new", this, "actionPerformed"));
            file.add(Translate.menuItem("open", this, "actionPerformed"));
            final BMenu recentMenu = Translate.menu("openRecent");
            RecentFiles.createMenu(recentMenu);
            file.add(recentMenu);
            Preferences.userNodeForPackage(RecentFiles.class).addPreferenceChangeListener(new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent ev) {
                    RecentFiles.createMenu(recentMenu);
                }
            });
            pack();
            setBounds(new Rectangle(-1000, -1000, 0, 0));
            setVisible(true);
        }

        @Override
        public ToolPalette getToolPalette() {
            return null;
        }

        @Override
        public void setTool(EditingTool tool) {
        }

        @Override
        public void setHelpText(String text) {
        }

        @Override
        public BFrame getFrame() {
            return this;
        }

        @Override
        public void updateImage() {
        }

        @Override
        public void updateMenus() {
        }

        @Override
        public void setUndoRecord(UndoRecord command) {
        }

        @Override
        public void setModified() {
        }

        @Override
        public Scene getScene() {
            return null;
        }

        @Override
        public ViewerCanvas getView() {
            return null;
        }

        @Override
        public ViewerCanvas[] getAllViews() {
            return null;
        }

        @Override
        public boolean confirmClose() {
            dispose();
            return true;
        }

        private void actionPerformed(CommandEvent ev) {
            String command = ev.getActionCommand();
            if (command.equals("new")) {
                ArtOfIllusion.newWindow();
            } else if (command.equals("open")) {
                ArtOfIllusion.openScene(this);
            } else if (command.equals("quit")) {
                ArtOfIllusion.quit();
            }
        }
    }
}
