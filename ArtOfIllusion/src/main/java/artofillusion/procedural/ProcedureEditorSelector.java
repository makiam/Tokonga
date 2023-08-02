package artofillusion.procedural;

import artofillusion.preferences.PreferencesEditor;
import buoy.widget.Widget;

public class ProcedureEditorSelector implements PreferencesEditor {

    @Override
    public String getName() {
        return "Procedure Editor Selector";
    }

    @Override
    public Widget getPreferencesPanel() {
        return new buoy.widget.ColumnContainer();
    }

    @Override
    public void savePreferences() {

    }
}
