/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.preferences;


import artofillusion.ui.Translate;
import buoy.widget.Widget;

/**
 *
 * @author MaksK
 */
public class ExtraPluginsPane extends buoy.widget.AWTWidget implements PreferencesEditor {
    private final ExtraPluginsPaneImpl impl;
    
    public ExtraPluginsPane() {
        super(new ExtraPluginsPaneImpl());
        impl = (ExtraPluginsPaneImpl)this.getComponent();
    }

    public void saveChanges() {
        impl.saveChanges();
    }

    @Override
    public Widget getPreferencesPanel() {
        return this;
    }

    @Override
    public void savePreferences() {
        this.saveChanges();
    }
    
    @Override
    public String getName() {
        return Translate.text("Plugins");
    }

}
