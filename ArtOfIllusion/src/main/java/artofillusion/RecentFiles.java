/* Copyright (C) 2004 by Peter Eastman
   Changes copyright (C) 2017-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;

/**
 * This class maintains a list of recently accessed scene files, and generates menus allowing
 * them to be opened easily.
 */
public class RecentFiles {

    static final int MAX_RECENT = 10;

    /**
     * Given a BMenu, fill it in with a list of items for recent files.
     */
    public static void createMenu(BMenu menu) {
        menu.removeAll();
        for (String recentFile : Preferences.userNodeForPackage(RecentFiles.class).get(RECENT_FILES, "").split(File.pathSeparator)) {
            final File file = new File(recentFile);
            BMenuItem item = new BMenuItem(file.getName());
            menu.add(item);
            item.addEventLink(CommandEvent.class, new Object() {
                void processEvent(CommandEvent ev) {
                    ArtOfIllusion.openScene(file, UIUtilities.findFrame(ev.getWidget()));
                }
            });
        }
    }
    private static final String RECENT_FILES = "recentFiles";

    /**
     * Add a File to the list of recent files.
     */
    public static void addRecentFile(File file) {
        // Find the new list of recent files.

        String newPath = file.getAbsolutePath();
        Preferences pref = Preferences.userNodeForPackage(RecentFiles.class);
        String[] recent = pref.get(RECENT_FILES, "").split(File.pathSeparator);
        List<String> newFiles = new ArrayList<>();
        newFiles.add(newPath);
        for (int i = 0; i < recent.length && newFiles.size() < MAX_RECENT; i++) {
            if (!newPath.equals(recent[i])) {
                newFiles.add(recent[i]);
            }
        }
        StringBuilder fileList = new StringBuilder();
        for (int i = 0; i < newFiles.size(); i++) {
            if (i > 0) {
                fileList.append(File.pathSeparator);
            }
            fileList.append(newFiles.get(i));
        }
        pref.put(RECENT_FILES, fileList.toString());

        // Rebuild the menus in all open windows.
        for (EditingWindow window : ArtOfIllusion.getWindows()) {
            if (window instanceof LayoutWindow) {
                createMenu(((LayoutWindow) window).getRecentFilesMenu());
            }
        }
    }
}
