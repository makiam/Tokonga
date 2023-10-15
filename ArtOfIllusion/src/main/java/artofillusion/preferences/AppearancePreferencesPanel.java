package artofillusion.preferences;

import artofillusion.ui.Translate;
import buoy.widget.Widget;

public class AppearancePreferencesPanel extends buoy.widget.AWTWidget implements PreferencesEditor {

    
    public AppearancePreferencesPanel() {
        super(new AppearancePreferencesPanelImpl());
    }
    @Override
    public Widget getPreferencesPanel() {
        return this;
    }

    @Override
    public void savePreferences() {

    }
    
    @Override
    public String getName() {
        return Translate.text("Appearance");
    }
    
}
