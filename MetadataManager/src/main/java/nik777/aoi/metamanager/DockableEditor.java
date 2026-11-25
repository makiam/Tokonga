/* Copyright (C) 2009 by Nik TJ
   Changes copyright (C) 2026 by Maksim Khramov


   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package nik777.aoi.metamanager;

import artofillusion.ui.DefaultDockableWidget;
import artofillusion.ui.UIUtilities;
import buoy.event.ValueChangedEvent;
import buoy.event.WindowClosingEvent;

import buoy.widget.Widget;
import buoy.widget.WidgetContainer;
import buoy.widget.WindowWidget;

import buoyx.docking.DockingContainer;
import buoyx.docking.DragManager;

public class DockableEditor extends DefaultDockableWidget {
  protected DockingContainer dock = null;

  protected int dockTab = -1;

  protected int dockIndex = -1;

  public DockableEditor(Widget content, String label) {
    super(content, label);
  }

  public void open(WindowWidget window) {
    if (this.dockTab >= 0 && this.dock != null) {
      this.dock.addDockableWidget(this, this.dockTab, this.dockIndex);
    } else {
      DragManager.DetachedDockingContainer undocked = new DragManager.DetachedDockingContainer(window);
      undocked.getDockingContainer().addDockableWidget(this);
      undocked.addEventLink(WindowClosingEvent.class, this, "close");
      undocked.pack();
      UIUtilities.centerDialog(undocked, window);
      undocked.setVisible(true);
    }
  }

  public void close() {
    WidgetContainer widgetContainer = getParent();
    if (widgetContainer != null) {
      this.dock = (DockingContainer) widgetContainer;
      this.dockTab = this.dock.getChildTabIndex(this);
      this.dockIndex = this.dock.getChildIndexInTab(this);
      this.dock.remove(this);
      if (this.dock.getChildCount() == 0) {
        widgetContainer = widgetContainer.getParent();
        if (widgetContainer != null && widgetContainer instanceof DragManager.DetachedDockingContainer) {
          ((DragManager.DetachedDockingContainer) widgetContainer).dispose();
          this.dockTab = -1;
          this.dockIndex = -1;
        }
      }
    }
  }

  public void setContent(Widget widget) {
    widget.addEventLink(ValueChangedEvent.class, this, "dispatchEvent");
    widget.addEventLink(UndockEvent.class, this, "close");
    super.setContent(widget);
  }

  public void remove(Widget widget) {
    widget.removeEventLink(ValueChangedEvent.class, this);
    widget.removeEventLink(UndockEvent.class, this);
    super.remove(widget);
  }
}
