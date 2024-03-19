/* Copyright (C) 2024 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.LayoutWindow;
import artofillusion.PluginRegistry;
import artofillusion.UndoRecord;
import artofillusion.ui.Translate;
import buoy.widget.BMenu;
import buoy.widget.BMenuItem;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

@Slf4j
public final class TracksMenu extends BMenu {
    private final LayoutWindow layout;
    public TracksMenu(LayoutWindow layout) {
        super(Translate.text("menu.addTrack"));
        this.layout = layout;

        for (TrackProvider provider : PluginRegistry.getPlugins(TrackProvider.class)) {
            add(new TrackMenuItem(provider));
        }
    }

    private class TrackAction extends AbstractAction {
        private final TrackProvider provider;

        public TrackAction(TrackProvider provider) {
            super(String.format("%s %s", provider.getName(), provider.getCategory()).stripTrailing());
            this.provider = provider;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LayoutWindow layout = TracksMenu.this.layout;
            UndoRecord undo = new UndoRecord(layout);
            provider.create(new ArrayList<>(layout.getSelectedObjects()), undo);
            layout.getScore().rebuildList();
            layout.setUndoRecord(undo);

        }
    }

    private class TrackMenuItem extends BMenuItem {
        public TrackMenuItem(TrackProvider provider) {
            this.getComponent().setAction(new TrackAction(provider));
        }
    }

}
