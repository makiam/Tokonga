/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion;

import artofillusion.ui.Translate;

import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;

import javax.swing.*;
import java.util.Objects;

@Slf4j
public final class LayoutFileMenu extends LayoutMenu {

    LayoutFileMenu(LayoutWindow layout) {
        super(layout, "menu.file");

        var mi = Translate.menuItem("new", event -> {
            layout.savePreferences();
            SwingUtilities.invokeLater(ArtOfIllusion::newWindow);
        });
        mi.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/artofillusion/layout-icons/icons8-new-16.png"))));
        this.add(mi);
        this.add(Translate.menuItem("open", event -> {
            layout.savePreferences();
            SwingUtilities.invokeLater(() -> ArtOfIllusion.openScene(layout));
        }));
    }

    @Subscribe
    public void onSceneChangedEvent(SceneChangedEvent event) {
        log.info("On Scene changed {} ", this.getLayout() == event.window() ? "this" : "other");
    }
}
