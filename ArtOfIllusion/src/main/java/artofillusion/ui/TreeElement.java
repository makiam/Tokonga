/* Copyright (C) 2001-2008 by Peter Eastman
 * Changes copyright (C) 2017-2025 by Maksim Khramov
 * 
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * This is an abstract class representing an element in a tree.
 */
public abstract class TreeElement implements TreeNode {

    protected boolean selected;
    protected boolean expanded;
    protected boolean selectable = true;
    protected List<TreeElement> children;
    protected TreeElement parent;
    protected TreeList tree;

    /**
     * Get the label to display for this element.
     */
    public abstract String getLabel();

    /**
     * Get the icon to display for this element (may be null).
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * Determine whether this element in the tree is selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set whether this element in the tree is selected.
     */
    public void setSelected(boolean selected) {
        this.selected = (selected && selectable);
    }

    /**
     * Determine whether this element in the tree is expanded.
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * Set whether this element in the tree is expanded.
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * Determine whether this element in the tree can be selected.
     */
    public boolean isSelectable() {
        return selectable;
    }

    /**
     * Set whether this element in the tree can be selected.
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    /**
     * This returns true if this element should automatically be selected whenever its parent
     * is selected.
     */
    public boolean selectWithParent() {
        return false;
    }

    /**
     * Get the parent of this element.
     */
    public TreeElement getParent() {
        return parent;
    }

    /**
     * Get the number of children which this element has.
     */
    public int getNumChildren() {
        return children.size();
    }

    /**
     * Get a particular child of this element.
     */
    public TreeElement getChild(int which) {
        return children.get(which);
    }

    /**
     * Determine whether this element can be added as a child of another one If el is null,
     * return whether this element can be added at the root level of the tree.
     */
    public abstract boolean canAcceptAsParent(TreeElement el);

    /**
     * Add another element as a child of this one.
     */
    public abstract void addChild(TreeElement el, int position);

    /**
     * Remove any elements corresponding to the given object from this element's list
     * of children.
     */
    public abstract void removeChild(Object obj);

    /**
     * Get the object corresponding to this element.
     */
    public abstract Object getObject();

    /**
     * Get whether this element should be drawn in gray (i.e. to indicate it is deactivated).
     */
    public abstract boolean isGray();

    @Override
    public TreeNode getChildAt(int which) {
        return children.get(which);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        return Collections.enumeration(children);
    }

    @Override
    public boolean isLeaf() {
        return !children.isEmpty();
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }
}
