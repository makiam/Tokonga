/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion.procedural;

import artofillusion.PluginRegistry;
import artofillusion.preferences.PreferencesEditor;
import buoy.widget.Widget;
import java.lang.reflect.InvocationTargetException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExperimentalProcedureEditorSettingsPage implements PreferencesEditor {

    private Widget widget = null;

    @Override
    public Widget getPreferencesPanel() {
        return widget = new buoy.widget.AWTWidget(new ExperimentalProcedureEditorPanel());
    }

    @Override
    public void savePreferences() {
        ExperimentalProcedureEditorPanel panel = (ExperimentalProcedureEditorPanel)widget.getComponent();        
        try {
            PluginRegistry.invokeExportedMethod("preferences.putBoolean", "artofillusion", "showExperimentalProcedureEditor", panel.isEditorEnabled());
            PluginRegistry.invokeExportedMethod("preferences.commit");
        } catch (InvocationTargetException | NoSuchMethodException ex) {
            log.atError().setCause(ex).log("Unable to write Preferences");
        }
    }

    @Override
    public String getName() {
        return "Experimental Procedure Editor";
    }
}
