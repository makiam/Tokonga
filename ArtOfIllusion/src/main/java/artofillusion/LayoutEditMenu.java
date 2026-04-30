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
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;

import javax.swing.*;

@Slf4j
public final class LayoutEditMenu extends BMenu {

    private final BMenuItem undoItem;
    private final BMenuItem redoItem;
    private final LayoutWindow layout;

    LayoutEditMenu(LayoutWindow layout) {
        super(Translate.text("menu.edit"));
        org.greenrobot.eventbus.EventBus.getDefault().register(this);
        this.layout = layout;
        undoItem = Translate.menuItem("undo", e -> layout.undoCommand());
        undoItem.setEnabled(false);
        redoItem = Translate.menuItem("redo", e -> layout.redoCommand());
        redoItem.setEnabled(false);
        this.add(undoItem);
        this.add(redoItem);
        this.addSeparator();
    }

    @Subscribe
    public void onUndoChangedEvent(UndoChangedEvent event) {
        if(event.getRecord().getView() != this.layout) return;
        var stack = event.stack();
        SwingUtilities.invokeLater(() -> {
           undoItem.setEnabled(stack.canUndo());
           redoItem.setEnabled(stack.canRedo());
           var ut = Translate.text("menu.undo");
           var rt = Translate.text("menu.redo");
           undoItem.setText(stack.canUndo() ? ut + " " + stack.getUndoName() : ut);
           redoItem.setText(stack.canRedo() ? rt + " " + stack.getRedoName() : rt);
        });
    }
}
