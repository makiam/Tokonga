/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.object.ObjectInfo;
import artofillusion.ui.AutoScroller;
import artofillusion.ui.DefaultDockableWidget;
import artofillusion.ui.ObjectTreeElement;
import artofillusion.ui.Translate;
import artofillusion.ui.TreeElement;
import artofillusion.ui.TreeList;
import buoy.event.SelectionChangedEvent;
import buoy.widget.BScrollPane;
import buoy.widget.Widget;
import java.awt.Color;
import java.awt.Dimension;
import java.util.stream.IntStream;

class SceneExplorer extends DefaultDockableWidget {

    private final TreeList itemTree;
    private final Scene scene;

    public SceneExplorer(LayoutWindow layout) {
        this.setLabel(Translate.text("Objects"));
        this.scene = layout.getScene();

        itemTree = new TreeList(layout);
        itemTree.setPopupMenuManager(layout);
        itemTree.setPreferredSize(new Dimension(130, 300));
        itemTree.addEventLink(TreeList.ElementMovedEvent.class, layout, "rebuildList");
        itemTree.addEventLink(TreeList.ElementDoubleClickedEvent.class, layout, "editObjectCommand");
        itemTree.addEventLink(SelectionChangedEvent.class, layout, "treeSelectionChanged");

        rebuildList();

        BScrollPane itemTreeScroller = new ExplorerTreeScroller(itemTree);
        this.setContent(itemTreeScroller);
    }

    public final void rebuildList() {

        int count = scene.getNumObjects();

        boolean[] expanded = new boolean[count], selected = new boolean[count];
        boolean captureState = itemTree.getElements().length != 0;

        if (captureState) {
            IntStream.range(0, count).forEach(index -> {
                TreeElement item = itemTree.findElement(scene.getObject(index));
                expanded[index] = item.isExpanded();
                selected[index] = item.isSelected();
            });
        }

        itemTree.setUpdateEnabled(false);
        itemTree.removeAllElements();
        scene.getObjects().stream().filter(info -> info.getParent() == null).forEach(item -> {
            itemTree.addElement(new ObjectTreeElement(item, itemTree));
        });

        if (captureState) {
            IntStream.range(0, count).forEach(index -> {
                TreeElement item = itemTree.findElement(scene.getObject(index));
                item.setExpanded(expanded[index]);
                item.setSelected(selected[index]);
            });
        }

        itemTree.setUpdateEnabled(true);
    }

    public void add(ObjectInfo info) {
        itemTree.addElement(new ObjectTreeElement(info, itemTree));
    }

    public void add(ObjectInfo info, int index) {
        itemTree.addElement(new ObjectTreeElement(info, itemTree), index);
    }

    public void remove(ObjectInfo info) {
        itemTree.removeObject(info);
    }

    public Object[] getSelectedObjects() {
        return itemTree.getSelectedObjects();
    }

    public void setSelected(ObjectInfo info, boolean selected) {
        itemTree.setSelected(info, selected);
    }

    public void deselectAll() {
        itemTree.deselectAll();
    }

    public void setUpdateEnabled(boolean state) {
        itemTree.setUpdateEnabled(state);
    }

    private static class ExplorerTreeScroller extends BScrollPane {

        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        public ExplorerTreeScroller(Widget contentWidget) {
            super(contentWidget);
            this.setForceWidth(true);
            this.setForceHeight(true);
            this.getVerticalScrollBar().setUnitIncrement(10);
            this.setBackground(Color.white);
            new AutoScroller(this, 0, 10);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(0, 0);
        }

    }
}
