/* Copyright (C) 2001-2004 by Peter Eastman
   Changes copyright (C) 2017-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.widget.*;
import java.awt.*;

/**
 * This is a user interface component for selecting an object or a joint in an object.
 */
public class ObjectRefSelector extends RowContainer {

    private final BLabel nameLabel;
    private final BButton setButton;
    private ObjectRef ref;
    private final LayoutWindow window;
    private final String prompt;
    private final ObjectInfo exclude;

    /**
     * Create a new selector.
     *
     * @param obj the initially selected object reference
     * @param win the window in which the scene is being edited
     * @param prompt the string which will be used for prompting the user to select a new object
     * @param exclude an object which may not be selected (may be null)
     */
    public ObjectRefSelector(ObjectRef obj, LayoutWindow win, String prompt, ObjectInfo exclude) {
        ref = obj.duplicate();
        window = win;
        this.prompt = prompt;
        this.exclude = exclude;

        add(setButton = Translate.button("set", event -> buttonPressed()));
        add(nameLabel = new BLabel(obj.toString()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                if (dim.width < 150) {
                    dim.width = 150;
                }
                return dim;
            }
        });
    }

    /**
     * Get the selected ObjectRef.
     */
    public ObjectRef getSelection() {
        return ref;
    }

    /**
     * Set whether this component is enabled.
     */
    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        setButton.setEnabled(enable);
        nameLabel.setEnabled(enable);
    }

    /**
     * When the button is clicked, display a window allowing the user to select a new
     * object.
     */
    private void buttonPressed() {
        TreeList tree = new TreeList(window);
        Scene sc = window.getScene();
        tree.setUpdateEnabled(false);
        for (ObjectInfo info : sc.getObjects()) {
            if (info.getParent() == null) {
                tree.addElement(new ObjectRefTreeElement(new ObjectRef(info), null, tree, exclude));
            }
        }
        tree.setUpdateEnabled(true);
        tree.setPreferredSize(new Dimension(250, 100));
        tree.setAllowMultiple(false);
        tree.setBackground(Color.white);
        tree.setSelected(ref, true);
        tree.expandToShowObject(ref);
        BScrollPane p = new BScrollPane(tree) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(150, 200);
            }
        };
        p.setForceWidth(true);
        p.setForceHeight(true);
        ComponentsDialog dlg = new ComponentsDialog(UIUtilities.findWindow(this), prompt, new Widget[]{p}, new String[]{null});
        if (!dlg.clickedOk()) {
            return;
        }
        Object[] sel = tree.getSelectedObjects();
        if (sel.length > 0) {
            ref = (ObjectRef) sel[0];
        } else {
            ref = new ObjectRef();
        }
        nameLabel.setText(ref.toString());
    }
}
