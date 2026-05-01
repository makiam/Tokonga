/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.ui.Translate;
import buoy.widget.BMenu;
import buoy.widget.BMenuItem;

import java.util.Comparator;


final class LayoutToolsMenu extends BMenu {
    private final LayoutWindow layout;
    public LayoutToolsMenu(LayoutWindow layout) {
        super(Translate.text("menu.tools"));
        this.layout = layout;
        PluginRegistry.getPlugins(ModellingTool.class).stream().
                sorted(Comparator.comparing(ModellingTool::getName)).map(ToolsActionMenu::new).forEach(this::add);
    }

    private class ToolsActionMenu extends BMenuItem {
        ToolsActionMenu(ModellingTool tool) {
            super(tool.getName());
            this.getComponent().addActionListener(e -> tool.commandSelected(LayoutToolsMenu.this.layout));
        }
    }
}
