/* This class represents a Track in the TreeList which appears in the Score. */

 /* Copyright (C) 2001 by Peter Eastman
   Changes copyright (C) 2017 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.animation;

import artofillusion.ui.*;
import java.util.*;

public class TrackTreeElement extends TreeElement {

    final Track theTrack;

    public TrackTreeElement(Track tr, TreeElement parent, TreeList tree) {
        theTrack = tr;
        this.parent = parent;
        this.tree = tree;
        children = new Vector<TreeElement>();
        Track[] subtracks = tr.getSubtracks();
        for (int i = 0; i < subtracks.length; i++) {
            children.add(new TrackTreeElement(subtracks[i], this, tree));
        }
    }

    /* Get the label to display for this element. */
    @Override
    public String getLabel() {
        return theTrack.getName();
    }

    /* Determine whether this element can be added as a child of another one  If el is null,
     return whether this element can be added at the root level of the tree. */
    @Override
    public boolean canAcceptAsParent(TreeElement el) {
        if (el == null) {
            return false;
        }
        return theTrack.canAcceptAsParent(el.getObject());
    }

    /* Add another element as a child of this one. */
    @Override
    public void addChild(TreeElement el, int position) {
        children.add(position, el);
        ((TrackTreeElement) el).parent = this;
    }

    /* Remove any elements corresponding to the given object from this element's list
     of children. */
    @Override
    public void removeChild(Object object) {
        /*    ObjectInfo obj = (ObjectInfo) object, newinfo[];
    TreeElement el;
    int pos;

    for (pos = 0; pos < children.size(); pos++)
      {
        el = (TreeElement) children.elementAt(pos);
        if (el.getObject() == object)
          break;
      }
    if (pos == children.size())
      {
        for (int i = 0; i < children.size(); i++)
          ((TreeElement) children.elementAt(i)).removeChild(obj);
        return;
      }
    obj.parent = null;
    el = (TreeElement) children.elementAt(pos);
    el.parent = null;
    children.removeElementAt(pos);
    if (el.getObject() instanceof ObjectInfo)
      ((ObjectInfo) el.getObject()).parent = null;
    if (info.object instanceof ObjectGroup)
      {
        newinfo = new ObjectInfo [children.size()];
        for (int i = 0; i < newinfo.length; i++)
          {
            el = (TreeElement) children.elementAt(i);
            newinfo[i] = (ObjectInfo) el.getObject();
          }
        ((ObjectGroup) info.object).setObjects(newinfo);
        if (tree.undo != null)
          tree.undo.addCommandAtBeginning(UndoRecord.ADD_TO_GROUP, new Object []
              {info, object, new Integer(pos)});
      }*/
    }

    /* Get the object corresponding to this element. */
    @Override
    public Object getObject() {
        return theTrack;
    }

    /* Get whether this element should be drawn in gray (i.e. to indicate it is deactivated). */
    @Override
    public boolean isGray() {
        return !theTrack.isEnabled();
    }
}
