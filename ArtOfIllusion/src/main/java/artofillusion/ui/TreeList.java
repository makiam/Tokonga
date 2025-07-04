/* Copyright (C) 2001-2009 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import artofillusion.*;
import static artofillusion.ui.UIUtilities.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.*;

/**
 * This is a Widget which displays a hierarchy of objects. It provides functionality
 * for opening and closing parts of the hierarchy, selecting elements, and moving elements
 * around.
 */
public class TreeList extends CustomWidget {

    private final EditingWindow window;
    private final List<TreeElement> elements = new Vector<>();
    private final List<TreeElement> showing = new Vector<>();
    private final List<TreeElement> selected = new Vector<>();
    private final List<Integer> indent = new Vector<>();
    private int yOffset;
    private int rowHeight = 15;
    private int dragStart;
    private int lastDrag;
    private int lastClickRow = -1;
    private int lastIndent;
    private int maxRowWidth;
    private boolean updateDisabled;
    private boolean moving;
    private boolean[] origSelected = new boolean[0];
    private boolean insertAbove;
    private boolean okToInsert;
    private boolean allowMultiple = true;
    private PopupMenuManager popupManager;
    protected UndoRecord undo;

    private static final Polygon openHandle;
    private static final Polygon closedHandle;
    private static final Polygon insertHandle;
    private static final int INDENT_WIDTH = 10;
    private static final int HANDLE_WIDTH = 4;
    private static final int HANDLE_HEIGHT = 8;
    private static final int INSERT_WIDTH = 3;
    private static final int INSERT_HEIGHT = 6;

    static {
        openHandle = new Polygon(new int[]{-HANDLE_HEIGHT / 2, HANDLE_HEIGHT / 2, 0},
                new int[]{-HANDLE_WIDTH / 2, -HANDLE_WIDTH / 2, HANDLE_WIDTH / 2}, 3);
        closedHandle = new Polygon(new int[]{-HANDLE_WIDTH / 2, HANDLE_WIDTH / 2, -HANDLE_WIDTH / 2},
                new int[]{-HANDLE_HEIGHT / 2, 0, HANDLE_HEIGHT / 2}, 3);
        insertHandle = new Polygon(new int[]{-INSERT_WIDTH - 2, -2, -INSERT_WIDTH - 2},
                new int[]{-INSERT_HEIGHT / 2, 0, INSERT_HEIGHT / 2}, 3);
    }

    public TreeList(EditingWindow win) {
        window = win;

        Font font = getFont();
        if (font == null) {
            font = UIUtilities.getDefaultFont();
        }
        if (font != null) {
            FontMetrics fm = getComponent().getFontMetrics(font);
            rowHeight = Math.max(fm.getMaxAscent() + fm.getMaxDescent(), HANDLE_HEIGHT) + 3;
        }

        buildState();
        addEventLink(MousePressedEvent.class, this, "mousePressed");
        addEventLink(MouseReleasedEvent.class, this, "mouseReleased");
        addEventLink(MouseDraggedEvent.class, this, "mouseDragged");
        addEventLink(MouseClickedEvent.class, this, "mouseClicked");
        addEventLink(RepaintEvent.class, this, "paint");
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension superPref = super.getPreferredSize();
        return new Dimension(Math.max(superPref.width, maxRowWidth), Math.max(superPref.height, rowHeight * showing.size()));
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(maxRowWidth, rowHeight * showing.size());
    }

    /**
     * Set whether this tree allows multiple selections (default is true).
     */
    public void setAllowMultiple(boolean allow) {
        allowMultiple = allow;
    }

    /**
     * Temporarily disable updating of the tree. This is useful when several elements are
     * going to be added or removed at once.
     */
    public void setUpdateEnabled(boolean enabled) {
        updateDisabled = !enabled;
        if (enabled) {
            buildState();
            repaint();
        }
    }

    /**
     * Add an element to the tree.
     */
    public void addElement(TreeElement el) {
        elements.add(el);
        buildState();
        if (!updateDisabled) {
            repaint();
        }
    }

    /**
     * Add an element to the tree.
     */
    public void addElement(TreeElement el, int position) {
        elements.add(position, el);
        if (!updateDisabled) {
            buildState();
            repaint();
        }
    }

    /**
     * Find the TreeElement corresponding to an object, or null if there is none.
     */
    public TreeElement findElement(Object obj) {
        for (TreeElement el : elements) {

            if (el.getObject().equals(obj)) {
                return el;
            }
            TreeElement subElement = findElement(obj, el);
            if (subElement != null) {
                return subElement;
            }
        }
        return null;
    }

    private TreeElement findElement(Object obj, TreeElement parent) {
        for (int i = 0; i < parent.getNumChildren(); i++) {
            TreeElement el = parent.getChild(i);
            if (el.getObject().equals(obj)) {
                return el;
            }
            TreeElement subElement = findElement(obj, el);
            if (subElement != null) {
                return subElement;
            }
        }
        return null;
    }

    /**
     * Remove the element from the tree which corresponds to the specified object.
     */
    public void removeObject(Object obj) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            TreeElement el = elements.get(i);
            if (el.getObject() == obj) {
                elements.remove(i);
            } else {
                el.removeChild(obj);
            }
        }
        if (!updateDisabled) {
            buildState();
            repaint();
        }
    }

    /**
     * Remove all elements from the tree.
     */
    public void removeAllElements() {
        elements.clear();
        if (!updateDisabled) {
            buildState();
            repaint();
        }
    }

    /**
     * Get an array of all the TreeElements in the tree.
     */
    public TreeElement[] getElements() {
        final List<TreeElement> v = new Vector<>();

        elements.forEach(el -> {
            v.add(el);
            addChildrenToList(el, v);
        });
        return v.toArray(new TreeElement[0]);
    }

    private void addChildrenToList(TreeElement el, List<TreeElement> v) {
        for (int i = 0; i < el.getNumChildren(); i++) {
            TreeElement child = el.getChild(i);
            v.add(child);
            addChildrenToList(child, v);
        }
    }

    /**
     * Get an array of the objects corresponding to selected TreeElements.
     */
    public Object[] getSelectedObjects() {
        return selected.stream().map(TreeElement::getObject).toArray(Object[]::new);
    }

    /**
     * Deselect all elements in the tree.
     */
    public void deselectAll() {
        elements.forEach(this::deselectRecursively);
        if (!updateDisabled) {
            buildState();
            repaint();
        }
    }

    private void deselectRecursively(TreeElement el) {
        el.setSelected(false);
        for (int i = 0; i < el.getNumChildren(); i++) {
            deselectRecursively(el.getChild(i));
        }
    }

    /**
     * Get an array of the objects corresponding to visible TreeElements, in the order that
     * they appear.
     */
    public Object[] getVisibleObjects() {
        Object[] vis = new Object[showing.size()];
        for (int i = 0; i < vis.length; i++) {
            vis[i] = showing.get(i).getObject();
        }
        return vis;
    }

    /**
     * Get the height (in pixels) of each row in the list.
     */
    public int getRowHeight() {
        return rowHeight;
    }

    /**
     * Select or deselect the element corresponding to a particular object.
     */
    public void setSelected(Object obj, boolean selected) {
        TreeElement el = (obj instanceof TreeElement ? (TreeElement) obj : findElement(obj));
        boolean wasDisabled = updateDisabled;
        updateDisabled = true;
        if (el != null) {
            el.setSelected(selected);
            for (int i = 0; i < el.getNumChildren(); i++) {
                TreeElement child = el.getChild(i);
                if (child.selectWithParent()) {
                    setSelected(child, selected);
                }
            }
        }
        updateDisabled = wasDisabled;
        if (!updateDisabled) {
            buildState();
            repaint();
        }
    }

    /**
     * Expand all parents of the specified object to make it visible.
     */
    public void expandToShowObject(Object obj) {
        TreeElement el = (obj instanceof TreeElement ? (TreeElement) obj : findElement(obj));
        if (el == null) {
            return;
        }
        while ((el = el.getParent()) != null) {
            el.setExpanded(true);
        }
        if (!updateDisabled) {
            buildState();
            repaint();
        }
    }

    /**
     * Start recording an undo record to reverse subsequent actions taken by the TreeList.
     */
    private void recordUndo() {
        undo = new UndoRecord(window);
    }

    /**
     * Finish recording the undo record, and return the completed record.
     */
    private UndoRecord finishRecording() {
        UndoRecord rec = undo;
        undo = null;
        return rec;
    }

    /**
     * Build the arrays representing the current state of the tree.
     */
    private void buildState() {
        if (updateDisabled) {
            return;
        }
        showing.clear();
        indent.clear();
        selected.clear();
        for (TreeElement el: elements) {

            showing.add(el);
            indent.add(0);
            if (el.isSelected()) {
                selected.add(el);
            }
            addChildrenToState(el, 1, el.isExpanded());
        }
        if (origSelected.length != showing.size()) {
            origSelected = new boolean[showing.size()];
        }
        invalidateSize();
        if (getComponent().isDisplayable()) {
            getParent().layoutChildren();
        }
        if (selected.isEmpty()) {
            lastClickRow = -1;
        } else if (selected.size() == 1) {
            lastClickRow = showing.indexOf(selected.get(0));
        }
    }

    private void addChildrenToState(TreeElement el, int currentIndent, boolean expanded) {
        for (int i = 0; i < el.getNumChildren(); i++) {
            TreeElement child = el.getChild(i);
            if (expanded) {
                showing.add(child);
                indent.add(currentIndent);
            }
            if (child.isSelected()) {
                selected.add(child);
            }
            addChildrenToState(child, currentIndent + 1, expanded & child.isExpanded());
        }
    }

    /**
     * Set the y offset (for vertically scrolling the panel).
     */
    public void setYOffset(int offset) {
        yOffset = offset;
    }

    /**
     * Paint the tree.
     */
    private void paint(RepaintEvent ev) {
        Graphics2D g = ev.getGraphics();
        FontMetrics fm = g.getFontMetrics();
        Rectangle dim = getBounds();
        int y = yOffset;

        rowHeight = Math.max(fm.getMaxAscent() + fm.getMaxDescent(), HANDLE_HEIGHT) + 3;
        maxRowWidth = 0;
        for (int i = 0; i < showing.size(); i++) {
            TreeElement el = showing.get(i);
            int x = indent.get(i) * INDENT_WIDTH;
            if (el.getNumChildren() > 0) {
                // Draw the handle to collapse or expand the hierarchy.

                g.setColor(Color.black);
                if (el.isExpanded()) {
                    openHandle.translate(x + INDENT_WIDTH / 2, y + rowHeight / 2);
                    g.drawPolygon(openHandle);
                    openHandle.translate(-x - INDENT_WIDTH / 2, -y - rowHeight / 2);
                } else {
                    closedHandle.translate(x + INDENT_WIDTH / 2, y + rowHeight / 2);
                    g.drawPolygon(closedHandle);
                    closedHandle.translate(-x - INDENT_WIDTH / 2, -y - rowHeight / 2);
                }
            }

            // Draw the label.
            x += INDENT_WIDTH;
            Icon icon = el.getIcon();
            if (icon != null) {
                icon.paintIcon(getComponent(), g, x, y);
                x += icon.getIconWidth();
            }
            if (el.isSelected()) {
                g.setColor(el.isGray() ? Color.gray : Color.black);
                g.fillRect(x, y, dim.width - x, rowHeight - 3);
                g.setColor(Color.white);
                g.drawString(el.getLabel(), x + 1, y + fm.getMaxAscent());
            } else {
                g.setColor(el.isGray() ? Color.gray : Color.black);
                g.drawString(el.getLabel(), x + 1, y + fm.getMaxAscent());
            }
            y += rowHeight;
            maxRowWidth = Math.max(maxRowWidth, fm.stringWidth(el.getLabel()) + x + 1);
        }
    }

    private void mousePressed(MousePressedEvent ev) {
        Point pos = ev.getPoint();
        pos.y -= yOffset;
        int row = pos.y / rowHeight;

        moving = false;
        if (row >= showing.size()) {
            // The click was below the last item in the list.

            deselectAll();
            Arrays.fill(origSelected, false);
            buildState();
            dispatchEvent(new SelectionChangedEvent(this));
            return;
        }
        dragStart = lastDrag = row;
        TreeElement el = showing.get(row);
        int i = pos.x / INDENT_WIDTH;
        int ind = indent.get(row);
        if (i == ind && el.getNumChildren() > 0) {
            // Expand or collapse this item.

            el.setExpanded(!el.isExpanded());
            buildState();
            repaint();
            dispatchEvent(new ElementExpandedEvent(el));
            showPopupIfNeeded(ev);
            return;
        }
        if (i < ind || (el.getParent() != null && el.selectWithParent() && el.getParent().isSelected())) {
            showPopupIfNeeded(ev);
            return;
        }
        if (ev.isShiftDown() || ev.isControlDown() || ev.isMetaDown()) {
            moving = false;
            if (lastClickRow == -1) {
                lastClickRow = row;
            }
        } else {
            moving = true;
            lastClickRow = row;
        }
        okToInsert = false;
        boolean selectionChanged = false;
        if (allowMultiple && (ev.isControlDown() || ev.isMetaDown()) && !ev.isPopupTrigger() && mouseButtonOne(ev)) {
            setSelected(el, !el.isSelected());
            selectionChanged = true;
        } else if (allowMultiple && ev.isShiftDown() && lastClickRow > -1) {
            int min = Math.min(lastClickRow, row);
            int max = Math.min(Math.max(lastClickRow, row), showing.size() - 1);
            updateDisabled = true;
            for (i = 0; i < showing.size(); i++) {
                TreeElement elem = showing.get(i);
                boolean sel = (origSelected[i] || (i >= min && i <= max));
                if (elem.isSelected() != sel) {
                    setSelected(elem, sel);
                    selectionChanged = true;
                }
            }
            updateDisabled = false;
        } else if (!el.isSelected()) {
            deselectAll();
            setSelected(el, true);
            selectionChanged = true;
        }
        for (i = 0; i < origSelected.length; i++) {
            el = showing.get(i);
            origSelected[i] = el.isSelected();
        }
        buildState();
        if (selectionChanged) {
            dispatchEvent(new SelectionChangedEvent(this));
        }
        repaint();
        showPopupIfNeeded(ev);
    }

    private void mouseDragged(MouseDraggedEvent ev) {
        Point pos = ev.getPoint();
        pos.y -= yOffset;
        int row = pos.y/rowHeight;
        int min;
        int max;
        int i;

        if (moving) {
            // The selected elements are being dragged.

            if (selected.isEmpty()) {
                return;
            }
            boolean above = pos.y - row * rowHeight < rowHeight / 2;
            if (row >= showing.size()) {
                row = showing.size();
                above = true;
            }
            if (row < 0) {
                row = 0;
                above = true;
            }
            if (row == lastDrag && above == insertAbove) {
                return;
            }
            Graphics g = getComponent().getGraphics();
            if (okToInsert) {
                // Erase the old insertion marker.

                g.setColor(getBackground());
                drawInsertionPoint(g, insertAbove ? lastDrag : lastDrag + 1, lastIndent);
            }

            // Determine whether the selected objects can be inserted here.
            TreeElement parent = null;
            if (row < showing.size()) {
                TreeElement el = showing.get(row);
                parent = el;
                lastIndent = indent.get(row);
                if (above) {
                    parent = el.getParent();
                    okToInsert = dragTargetOk(parent);
                } else {
                    okToInsert = dragTargetOk(parent);
                    if (okToInsert) {
                        lastIndent++;
                    } else {
                        parent = el.getParent();
                        okToInsert = dragTargetOk(parent);
                    }
                }
            } else {
                lastIndent = 0;
            }
            okToInsert = true;
            for (i = 0; okToInsert && i < selected.size(); i++) {
                TreeElement el = selected.get(i);
                if (el.getParent() != null && el.getParent().isSelected()) {
                    continue;
                }
                okToInsert &= el.canAcceptAsParent(parent);
            }
            if (okToInsert) {

                // Draw the new insertion point.
                g.setColor(Color.black);
                drawInsertionPoint(g, above ? row : row + 1, lastIndent);
            }
            g.dispose();
            lastDrag = row;
            insertAbove = above;
            return;
        }

        if (row == lastDrag || !allowMultiple) {
            return;
        }
        lastDrag = row;
        min = Math.max(Math.min(row, dragStart), 0);
        max = Math.min(Math.max(row, dragStart), showing.size() - 1);
        updateDisabled = true;
        for (i = 0; i < showing.size(); i++) {
            TreeElement el = showing.get(i);
            boolean sel = (origSelected[i] || (i >= min && i <= max)
                    || (el.getParent() != null && el.getParent().isSelected()));
            if (el.isSelected() != sel) {
                setSelected(el, sel);
            }
        }
        updateDisabled = false;
        buildState();
        repaint();
    }

    private void mouseReleased(MouseReleasedEvent ev) {
        if (moving) {
            if (okToInsert) {
                // Move the selected elements to the specified location.

                recordUndo();
                updateDisabled = true;
                TreeElement el = null, parent;
                int position = 0;

                // First figure out where to insert them.
                if (lastDrag < showing.size()) {
                    el = showing.get(lastDrag);
                    parent = el;
                    if (insertAbove || !dragTargetOk(el)) {
                        parent = el.getParent();
                        if (parent == null) {
                            for (position = 0; elements.get(position) != el; position++);
                        } else {
                            for (position = 0; parent.getChild(position) != el; position++);
                        }
                        if (!insertAbove) {
                            position++;
                        }
                    } else {
                        position = 0;
                    }
                } else {
                    parent = null;
                    position = elements.size();
                }

                // Now remove them from the tree, and insert them at the correct place.
                for (int i = 0; i < selected.size(); i++) {
                    el = selected.get(i);
                    if (el.getParent() != null && el.getParent().isSelected()) {
                        selected.remove(i);
                        i--;
                        continue;
                    }
                    if (el.getParent() == parent) {
                        int j = showing.indexOf(el);
                        if (j < lastDrag || (!insertAbove && j == lastDrag)) {
                            position--;
                        }
                    }
                    removeObject(el.getObject());
                }
                if (position < 0) {
                    position = 0;
                }
                if (parent == null) {
                    for (int i = 0; i < selected.size(); i++) {
                        el = selected.get(i);
                        if (el.getParent() == null || !el.getParent().isSelected()) {
                            addElement(el, position++);
                        }
                    }
                } else {
                    for (int i = 0; i < selected.size(); i++) {
                        el = selected.get(i);
                        if (el.getParent() == null || !el.getParent().isSelected()) {
                            parent.addChild(el, position++);
                        }
                    }
                }
                updateDisabled = false;
                buildState();
                window.setUndoRecord(finishRecording());
                repaint();
                dispatchEvent(new ElementMovedEvent(el));
            }
        } else {
            // They were selecting a range of objects.

            if (lastDrag != dragStart) {
                dispatchEvent(new SelectionChangedEvent(this));
            }
        }
        showPopupIfNeeded(ev);
    }

    /**
     * Determine whether the selected elements can be added to a particular parent.
     */
    private boolean dragTargetOk(TreeElement parent) {
        for (TreeElement el: selected) {

            if (el.getParent() != null && el.getParent().isSelected()) {
                continue;
            }
            if (!el.canAcceptAsParent(parent)) {
                return false;
            }
        }
        return true;
    }

    private void mouseClicked(MouseClickedEvent ev) {
        if (ev.getClickCount() == 1 && mouseButtonTwo(ev)) {
            window.getView().fitToObjects(((LayoutWindow) window).getSelectedObjects());

        } else if (ev.getClickCount() == 2) {
            Point pos = ev.getPoint();
            pos.y -= yOffset;
            int row = pos.y / rowHeight;
            int i = pos.x / INDENT_WIDTH;
            if (row >= showing.size()) {
                return;
            }
            int ind = indent.get(row);
            TreeElement el = showing.get(row);
            if (i < ind) {
                return;
            }
            dispatchEvent(new ElementDoubleClickedEvent(el));

        }
    }

    /**
     * Draw the insertion point to show where dragged items will be moved to.
     */
    private void drawInsertionPoint(Graphics g, int pos, int indent) {
        int x = (indent + 1) * INDENT_WIDTH;
        int y = pos * rowHeight - 2 + yOffset;
        Rectangle dim = getBounds();

        insertHandle.translate(x, y);
        g.fillPolygon(insertHandle);
        insertHandle.translate(-x, -y);
        g.drawLine(x - 2, y, dim.width, y);
    }

    /**
     * Set the PopupMenuManager for this list.
     */
    public void setPopupMenuManager(PopupMenuManager manager) {
        popupManager = manager;
    }

    /**
     * Display the popup menu when an appropriate event occurs.
     */
    private void showPopupIfNeeded(WidgetMouseEvent ev) {
        if (!ev.isPopupTrigger() || popupManager == null) {
            return;
        }
        repaint();
        Point pos = ev.getPoint();
        popupManager.showPopupMenu(this, pos.x, pos.y);
    }

    /**
     * Inner class which is the superclass of various events generated by tree.
     */
    public class TreeElementEvent implements WidgetEvent {

        final TreeElement elem;

        private TreeElementEvent(TreeElement el) {
            elem = el;
        }

        public TreeElement getElement() {
            return elem;
        }

        @Override
        public Widget getWidget() {
            return TreeList.this;
        }
    }

    /**
     * Inner class representing an event when one or more elements are moved in the tree.
     */
    public class ElementMovedEvent extends TreeElementEvent {

        private ElementMovedEvent(TreeElement el) {
            super(el);
        }
    }

    /**
     * Inner class representing an event when an element is expanded or collapsed.
     */
    public class ElementExpandedEvent extends TreeElementEvent {

        private ElementExpandedEvent(TreeElement el) {
            super(el);
        }
    }

    /**
     * Inner class representing an event when an element is double-clicked.
     */
    public class ElementDoubleClickedEvent extends TreeElementEvent {

        private ElementDoubleClickedEvent(TreeElement el) {
            super(el);
        }
    }
}
