/* Copyright (C) 2002-2020 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.osspecific;

import artofillusion.ArtOfIllusion;
import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.PreferencesWindow;
import artofillusion.RecentFiles;
import artofillusion.Scene;
import artofillusion.SceneChangedEvent;
import artofillusion.TitleWindow;
import artofillusion.UndoRecord;
import artofillusion.ViewerCanvas;
import artofillusion.ui.EditingTool;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.ToolPalette;
import artofillusion.ui.Translate;
import artofillusion.ui.UIUtilities;


import buoy.widget.BFrame;
import buoy.widget.BMenu;
import buoy.widget.BMenuBar;
import buoy.widget.BMenuItem;
import buoy.widget.BSeparator;
import buoy.widget.MenuWidget;
import buoy.widget.Widget;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
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
import java.util.Locale;
import java.util.prefs.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * This is a plugin to make Art of Illusion behave more like a standard Macintosh
 * application when running under Mac OS X.
 */
@Slf4j
public final class MacOSPlugin implements Plugin, AboutHandler, QuitHandler, OpenFilesHandler, PreferencesHandler {

    private boolean usingAppMenu;
    private static final String OS = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);

    @Override
    public void onSceneSaved(File file, LayoutWindow view) {
        MacOSPlugin.updateWindowProperties(view);
        view.getComponent().getRootPane().putClientProperty("Window.documentModified", false);
    }

    @Override
    public void onSceneWindowCreated(final LayoutWindow view) {
        view.addEventLink(SceneChangedEvent.class, new Object() {
            void processEvent() {
                updateWindowProperties(view);
            }
        });
        updateWindowProperties(view);
        if (usingAppMenu) {
            removeMenuItem(view.getFileMenu(), Translate.text("menu.quit"));
            removeMenuItem(view.getEditMenu(), Translate.text("menu.preferences"));
        }
    }

    @Override
    public void onApplicationStarting() {
        if (!OS.startsWith("mac os x")) {
            return;
        }
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        ArtOfIllusion.addWindow(new MacMenuBarWindow());
        UIUtilities.setDefaultFont(new Font("Application", Font.PLAIN, 11));
        UIUtilities.setStandardDialogInsets(3);

        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(this);
        desktop.setQuitHandler(this);
        desktop.setOpenFileHandler(this);
        desktop.setPreferencesHandler(this);

        usingAppMenu = true;
    }

    /**
     * Update the Mac OS X specific client properties.
     */
    private static void updateWindowProperties(final LayoutWindow win) {
        javax.swing.JRootPane rp = win.getComponent().getRootPane();
        rp.putClientProperty("Window.documentModified", win.isModified());
        Scene scene = win.getScene();
        if (scene.getName() != null) {
            File file = new File(scene.getDirectory(), scene.getName());
            rp.putClientProperty("Window.documentFile", file);
        }
    }

    private void removeMenuItem(BMenu menu, String name) {
        for (int j = 0; j < menu.getChildCount(); j++) {
            MenuWidget w = menu.getChild(j);
            if (w instanceof BMenuItem && ((BMenuItem) w).getText().equals(name)) {
                menu.remove((Widget) w);
                if (j > 0 && menu.getChild(j - 1) instanceof BSeparator) {
                    menu.remove((Widget) menu.getChild(j - 1));
                }
                return;
            }
        }
    }

    /**
     * Remove a menu item from a menu in a window. If it is immediately preceded by a separator,
     * also remove that.
     */
    private void removeMenuItem(final BFrame frame, String menu, String item) {
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

    @Override
    public void handleAbout(AboutEvent event) {
        new TitleWindow().setDisposable().show();
    }

    @Override
    public void handleQuitRequestWith(final QuitEvent event, QuitResponse response) {
        ArtOfIllusion.quit();
        response.cancelQuit();
    }

    @Override
    public void openFiles(OpenFilesEvent event) {
        for (var file : event.getFiles()) {
            try {
                ArtOfIllusion.newWindow(new Scene(file, true));
            } catch (IOException ex) {
                log.atError().setCause(ex).log("Error opening scene: {} {}", file, ex.getMessage());
            }
        }
    }

    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void handlePreferences(PreferencesEvent event) {
        final Window frontWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        boolean frontIsLayoutWindow = false;
        for (EditingWindow window : ArtOfIllusion.getWindows()) {
            if (window instanceof LayoutWindow && window.getFrame().getComponent() == frontWindow) {
                ((LayoutWindow) window).preferencesCommand();
                frontIsLayoutWindow = true;
                break;
            }
        }
        if (frontIsLayoutWindow) {
            return;
        }
        BFrame f = new BFrame();
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        f.setBounds(screenBounds);
        UIUtilities.centerWindow(f);
        new PreferencesWindow(f);
        f.dispose();
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
            file.add(Translate.menuItem("new", e -> ArtOfIllusion.newWindow()));
            file.add(Translate.menuItem("open", e -> ArtOfIllusion.openScene(this)));
            final BMenu recentMenu = Translate.menu("openRecent");
            RecentFiles.createMenu(recentMenu);
            file.add(recentMenu);
            Preferences.userNodeForPackage(RecentFiles.class).addPreferenceChangeListener((PreferenceChangeEvent ev) -> {
                RecentFiles.createMenu(recentMenu);
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

    }
}
