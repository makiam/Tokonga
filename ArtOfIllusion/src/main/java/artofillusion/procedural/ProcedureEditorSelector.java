package artofillusion.procedural;

import artofillusion.preferences.PreferencesEditor;
import buoy.widget.Widget;

public class ProcedureEditorSelector implements PreferencesEditor {

    private Widget widget = null;

    @Override
    public String getName() {
        return "Procedure Editor Selector";
    }

    @Override
    public Widget getPreferencesPanel() {
        return widget = new buoy.widget.AWTWidget(new EditorSelectorPanel());
    }

    @Override
    public void savePreferences() {

    }
}
