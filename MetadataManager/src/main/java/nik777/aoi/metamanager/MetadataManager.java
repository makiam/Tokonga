/* Copyright (C) 2009 by Nik TJ
   Changes copyright (C) 2026 by Maksim Khramov


   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package nik777.aoi.metamanager;

import artofillusion.LayoutWindow;
import artofillusion.Plugin;
import artofillusion.PluginRegistry;
import artofillusion.ui.Translate;
import buoy.event.CommandEvent;
import buoy.event.ValueChangedEvent;
import buoy.widget.BMenu;
import buoy.widget.BMenuItem;

import buoy.widget.Widget;
import buoy.widget.WidgetContainer;
import buoy.widget.WindowWidget;

import java.util.HashMap;

import java.util.Map;

public class MetadataManager implements Plugin {
    protected static Map<String, MetadataEditor> plugins = new HashMap<>();

    protected static Map<Widget, DockableEditor> editors = new HashMap<>();

    @Override
    public void onApplicationStarting() {
      PluginRegistry.getPlugins(MetadataEditor.class).forEach(med -> plugins.put(med.getName(), med));
    }

  /**
   * @param view
   */
  @Override
  public void onSceneWindowCreated(LayoutWindow view) {
    var mi = Translate.menu("MetadataManager:name");
    if (plugins.isEmpty()) {
        return;
    }
    plugins.keySet().forEach(name -> mi.add(Translate.menuItem(name, this, "edit")));
    view.getSceneMenu().add(mi);
  }

  /**
   * @param layoutWindow
   */
  @Override
  public void onSceneWindowClosing(LayoutWindow layoutWindow) {
      var name = "MetaDataManager:metadata";
      layoutWindow.getSceneMenu().getChildren().forEach(w -> {
        if (w.getName().equals(name)) {
            BMenu mm = (BMenu) w;
            mm.getChildren().forEach(wc -> editors.remove(wc));
        }
      });
  }


    protected void edit(CommandEvent ev) {
        BMenuItem menuItem = (BMenuItem) ev.getWidget();
        String name = menuItem.getActionCommand();
        DockableEditor ed = editors.get(menuItem);
        if (ed != null && ed.getParent() != null) {
            return;
        }
        WidgetContainer widgetContainer = menuItem.getParent();
        for (; widgetContainer != null && !(widgetContainer instanceof WindowWidget); widgetContainer = widgetContainer.getParent()) {
            ;
        }
        if (widgetContainer == null) {
            throw new RuntimeException("MetaDatamanager: no containing Window: " + ev);
        }
        LayoutWindow window = (LayoutWindow) widgetContainer;
        if (ed == null) {
            ed = createEditor(name, window, menuItem.getText());
            editors.put(menuItem, ed);
        }
        ed.open(window);
    }

    public static void edit(String name, LayoutWindow window) {
        String label = Translate.text(name);
        createEditor(name, window, label).open(window);
    }

    protected static DockableEditor createEditor(String name, LayoutWindow window, String label) {
        MetadataEditor med = plugins.get(name);
        if (med == null) {
            throw new RuntimeException("MetadataManager: could not find editor for: " + name);
        }
        Object data = window.getScene().getMetadata(name);
        DockableEditor ed = new DockableEditor(med.getWidget(data, window, true), label);
        ed.addEventLink(ValueChangedEvent.class, window, "setModified");
        return ed;
    }
}
