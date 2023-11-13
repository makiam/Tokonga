/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.spmanager;

import artofillusion.preferences.PreferencesEditor;
import buoy.widget.Widget;

/**
 *
 * @author MaksK
 */
public class SPManagerSettingsPage implements PreferencesEditor {
    private Widget widget = null;

    @Override
    public String getName() {
        return "Scripts And Plugins Manager";
    }

    @Override
    public Widget getPreferencesPanel() {
        return widget = new buoy.widget.AWTWidget(new SPManagerSettingsPanel());
    }

    @Override
    public void savePreferences() {

    }
}
