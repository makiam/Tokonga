/* Copyright (C) 2007-2015 by Peter Eastman
   Changes copyright (C) 2018 by Petri Ihalainen

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.view;

import buoy.widget.*;
import buoy.event.*;
import artofillusion.*;
import artofillusion.object.*;
import artofillusion.ui.*;

/**
 * This is a ViewerControl for adjusting the scale of the view.
 */
public class ViewerOrientationControl implements ViewerControl {

    @Override
    public Widget createWidget(final ViewerCanvas view) {
        return new OrientationChoice(view);
    }

    @Override
    public String getName() {
        return Translate.text("ViewDirection");
    }

    public static class OrientationChoice extends BComboBox {

        private final ViewerCanvas view;

        private OrientationChoice(ViewerCanvas view) {
            super(new String[]{Translate.text("Front"),
                Translate.text("Back"),
                Translate.text("Left"),
                Translate.text("Right"),
                Translate.text("Top"),
                Translate.text("Bottom"),
                Translate.text("Other")});
            this.view = view;
            if (view instanceof SceneViewer) {
                rebuildCameraList();
            }
            setSelectedIndex(0);
            view.addEventLink(ViewChangedEvent.class, this, "viewChanged");
            addEventLink(ValueChangedEvent.class, this, "valueChanged");
            setPreferredVisibleRows(20);
        }

        private void viewChanged() {
            if (view.getOrientation() != getSelectedIndex()) {
                if (view.getOrientation() < getItemCount()) {
                    setSelectedIndex(view.getOrientation());
                } else {
                    setSelectedIndex(getItemCount() - 1);
                }
            }
        }

        private void valueChanged() {
            if (getSelectedIndex() != view.getOrientation()) {
                view.setOrientation(getSelectedIndex());
            }
        }

        /**
         * Add all SceneCameras in the scene to list of available views.
         */
        public void rebuildCameraList() {
            int i = getItemCount() - 2, selected = getSelectedIndex();

            while (i > 5) {
                remove(i--);
            }
            ObjectInfo[] cameras = ((SceneViewer) view).getCameras();
            for (i = 0; i < cameras.length; i++) {
                add(getItemCount() - 1, new listName(cameras[i].getName()));
                if (cameras[i] == view.getBoundCamera()) {
                    selected = getItemCount() - 2;
                } else if (selected == getItemCount() - 2) {
                    selected = Integer.MAX_VALUE;
                }
            }
            if (selected < getItemCount()) {
                setSelectedIndex(selected);
            } else {
                setSelectedIndex(getItemCount() - 1);
            }
            if (getParent() != null) {
                getParent().layoutChildren();
            }
        }

        /**
         * This is needed because several view cameras may have the same name.
         * Using just Strings in the ComboBox results selecting always the first
         * occurrence of a name.
         */
        private class listName {

            private final String name;

            private listName(String name) {
                this.name = name;
            }

            @Override
            public String toString() {
                return name;
            }
        }
    }
}
