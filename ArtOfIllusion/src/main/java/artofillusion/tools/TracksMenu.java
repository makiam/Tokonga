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
            super(provider.getName());
            this.provider = provider;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LayoutWindow layout = TracksMenu.this.layout;
            UndoRecord undo = new UndoRecord(layout);
            provider.create(layout.getSelectedObjects(), undo);
            layout.getScore().rebuildList();
            layout.setUndoRecord(undo);

        }
    }

    private class TrackMenuItem extends BMenuItem {
        public TrackMenuItem(TrackProvider provider) {
            super(provider.getName());
            this.getComponent().setAction(new TrackAction(provider));
        }
    }

}
