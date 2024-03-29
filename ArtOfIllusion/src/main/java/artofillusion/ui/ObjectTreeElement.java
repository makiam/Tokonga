/* Copyright (C) 2001-2008 by Peter Eastman
   Changes copyright (C) 2017-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.object.*;
import javax.swing.*;
import java.util.*;

/**
 * This class represents an object in the tree of objects in a scene.
 */
public class ObjectTreeElement extends TreeElement {

    protected final ObjectInfo info;
    private static final Icon lockIcon = ThemeManager.getIcon("lock");

    public ObjectTreeElement(ObjectInfo info, TreeList tree) {
        this(info, null, tree, true);
    }

    public ObjectTreeElement(ObjectInfo info, TreeElement parent, TreeList tree, boolean addChildren) {
        this.info = info;
        this.parent = parent;
        this.tree = tree;
        children = new Vector<>();
        if (addChildren) {
            for (int i = 0; i < info.getChildren().length; i++) {
                children.add(new ObjectTreeElement(info.getChildren()[i], this, tree, true));
            }
        }
    }

    /* Get the label to display for this element. */
    @Override
    public String getLabel() {
        return info.getName();
    }

    @Override
    public Icon getIcon() {
        return (info.isLocked() ? lockIcon : null);
    }

    /* Determine whether this element can be added as a child of another one  If el is null,
     return whether this element can be added at the root level of the tree. */
    @Override
    public boolean canAcceptAsParent(TreeElement el) {
        if (el == null) {
            return true;
        }
        if (!(el instanceof ObjectTreeElement)) {
            return false;
        }
        ObjectInfo i = ((ObjectTreeElement) el).info;
        while (i != null) {
            if (i == info) {
                return false;
            }
            i = i.getParent();
        }
        return true;
    }

    /* This returns true if this element should automatically be selected whenever its parent
     is selected. */

 /*  public boolean selectWithParent()
  {
    return true;
  }*/

 /* Add another element as a child of this one. */
    @Override
    public void addChild(TreeElement el, int position) {
        children.add(position, el);
        el.parent = this;
        if (el.getObject() instanceof ObjectInfo) {
            if (tree.undo != null) {
                tree.undo.addCommandAtBeginning(UndoRecord.REMOVE_FROM_GROUP, info, el.getObject());
            }
            info.addChild((ObjectInfo) el.getObject(), position);
        } else if (el.getObject() instanceof Track) {
            Track tr = (Track) el.getObject();
            info.addTrack(tr, position);
            tr.setParent(info);
        }
    }

    /* Remove any elements corresponding to the given object from this element's list
     of children. */
    @Override
    public void removeChild(Object object) {
        TreeElement el = null;
        int pos;

        for (pos = 0; pos < children.size(); pos++) {
            el = children.get(pos);
            if (el.getObject() == object) {
                break;
            }
        }
        if (pos == children.size()) {
            for (TreeElement child : children) {
                child.removeChild(object);
            }
            return;
        }
        el.parent = null;
        children.remove(pos);
        if (object instanceof Track) {
            info.removeTrack((Track) object);
            return;
        }
        info.removeChild((ObjectInfo) object);
        if (tree.undo != null) {
            tree.undo.addCommandAtBeginning(UndoRecord.ADD_TO_GROUP, info, object, pos);
        }
    }

    /* Get the object corresponding to this element. */
    @Override
    public Object getObject() {
        return info;
    }

    /* Get whether this element should be drawn in gray (i.e. to indicate it is deactivated). */
    @Override
    public boolean isGray() {
        return !info.isVisible();
    }

    /* Add all of the Tracks for this object as children. */
    public void addTracks() {
        for (int i = 0; i < info.getTracks().length; i++) {
            TreeElement el = new TrackTreeElement(info.getTracks()[i], this, tree);
            children.add(i, el);
        }
    }
}
