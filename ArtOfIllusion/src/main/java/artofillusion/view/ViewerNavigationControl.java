/* Copyright (C) 2016-2019 by Petri Ihalainen
   Changes copyright (C) 2024 by Maksim Khramov

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
import artofillusion.ui.*;

/**
 * This is a ViewerControl for selecting the navigation mode.
 */
public class ViewerNavigationControl implements ViewerControl {

    @Override
    public Widget createWidget(final ViewerCanvas view) {
        return new NavigationChoice(view);
    }

    @Override
    public String getName() {
        return Translate.text("Navigation");
    }

    public static class NavigationChoice extends BComboBox {

        private final ViewerCanvas view;

        private NavigationChoice(ViewerCanvas view) {
            super(new String[]{Translate.text("ModelSpace"),
                Translate.text("ModelLandscape"),
                Translate.text("TravelSpace"),
                Translate.text("TravelLandscape")
            });
            this.view = view;
            setSelectedIndex(0);
            addEventLink(ValueChangedEvent.class, this, "valueChanged");
            view.addEventLink(ViewChangedEvent.class, this, "viewChanged");
            setPreferredVisibleRows(4);
        }

        private void valueChanged() {
            int selectedValue = getSelectedIndex();
            view.perspectiveControlEnabled = selectedValue <= 1;
            view.lastSetNavigation = selectedValue;
            view.setNavigationMode(selectedValue);
        }

        private void viewChanged() {
            if (view.getNavigationMode() != getSelectedIndex()) {
                setSelectedIndex(view.getNavigationMode());
            }
        }
    }
}
