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
import artofillusion.PluginRegistry;
import artofillusion.Scene;
import artofillusion.ui.Translate;
import buoy.event.SelectionChangedEvent;
import buoy.widget.BButton;
import buoy.widget.BList;
import buoy.widget.BTextArea;
import buoy.widget.BTextField;
import buoy.widget.BorderContainer;
import buoy.widget.LayoutInfo;
import buoy.widget.OverlayContainer;
import buoy.widget.RowContainer;

import buoy.widget.WidgetContainer;
import buoy.widget.WindowWidget;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.Iterator;

@Slf4j
public class GenericMetadataEditor extends DockableEditor {

  public GenericMetadataEditor(LayoutWindow window) {
    super(new Editor(window), Translate.text("MetadataManager:generic"));
  }

  protected static class Editor extends BorderContainer {
    protected LayoutWindow window;

    protected Scene scene;

    protected String current;

    protected BList metadata;

    protected OverlayContainer editor;

    protected BButton okBtn;

    protected BButton cancelBtn;

    protected BButton deleteBtn;

    protected BButton importBtn;

    protected BButton exportBtn;

    protected BTextField nameField;

    protected BTextField typeField;

    protected BTextArea generic;

    public Editor(LayoutWindow window) {
      this.window = window;
      this.scene = window.getScene();
      this.metadata = new BList();
      this.generic = new BTextArea();
      RowContainer buttons = new RowContainer();
      add(this.metadata, BorderContainer.WEST);
      this.metadata.addEventLink(SelectionChangedEvent.class, this, "select");
      this.editor = new OverlayContainer();
      this.okBtn = Translate.button("ok", this, "save");
      this.cancelBtn = Translate.button("cancel", this, "undo");
      this.deleteBtn = Translate.button("delete", this, "delete");
      buttons.add(this.okBtn);
      buttons.add(this.cancelBtn);
      buttons.add(this.deleteBtn);
      add(buttons, BorderContainer.SOUTH, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE));
    }

    protected void select() {
      if (this.metadata.getSelectedIndex() < 0) {
          return;
      }
      this.current = this.metadata.getSelectedValue().toString();
      Object data = this.scene.getMetadata(this.current);
      Class<?> type = data.getClass();
      Constructor<?> ctor = null;
      try {
        ctor = type.getConstructor(type);
      } catch (NoSuchMethodException e) {
        log.atError().setCause(e).log("GenericMetadataEditor: {}", e.getMessage());
      }

      Iterator<MetadataEditor> iter = PluginRegistry.getPlugins(MetadataEditor.class).iterator();
      MetadataEditor med = null;


      while (iter.hasNext()) {
        med = iter.next();
        if (med.getObjectType() == type) {
          if (ctor != null) {
            try {
              data = ctor.newInstance(data);
            } catch (Exception e) {
              log.atError().setCause(e).log("GenericMetadataEditor: {}", e.getMessage());
            }
            this.okBtn.setEnabled(true);
            this.cancelBtn.setEnabled(true);
          } else {
            this.okBtn.setEnabled(false);
            this.cancelBtn.setEnabled(false);
          }
          add(med.getWidget(data, this.window, (ctor == null)), BorderContainer.CENTER);
          break;
        }
        med = null;
      }

      if (med == null) {
        log.info("GenericMetadataEditor: no editor found for: {}", type.getName());
        this.generic.setText(data.toString());
        add(this.generic, BorderContainer.CENTER);
      }

      WidgetContainer widgetContainer = getParent().getParent();
      if (widgetContainer != null && widgetContainer instanceof WindowWidget) {
        ((WindowWidget) widgetContainer).pack();
        widgetContainer.layoutChildren();
      } else {
        getParent().layoutChildren();
      }
    }

    public void open() {
      this.metadata.setContents(this.scene.getAllMetadataNames());
    }

    protected void save() {}

    protected void undo() {}

    protected void delete() {}
  }

  public void open(WindowWidget window) {
    ((Editor) getContent()).open();
    super.open(window);
  }
}
