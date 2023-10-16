package artofillusion.preferences;

import artofillusion.ArtOfIllusion;
import artofillusion.ui.MessageDialog;
import artofillusion.ui.Translate;
import artofillusion.ui.UIUtilities;
import buoy.widget.Widget;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppearancePreferencesPanel extends buoy.widget.AWTWidget implements PreferencesEditor {

    private final AppearancePreferencesPanelImpl impl;
    
    public AppearancePreferencesPanel() {
        super(new AppearancePreferencesPanelImpl());
        impl = (AppearancePreferencesPanelImpl)this.getComponent();
    }
    @Override
    public Widget getPreferencesPanel() {
        return this;
    }

    @Override
    public void savePreferences() {
        var preferences = ArtOfIllusion.getPreferences();
        var sl = impl.getSelectedLocale();
        if (preferences.getLocale().equals(sl)) {
            return;
        }
        MessageDialog.create().info(UIUtilities.breakString(Translate.text("languageChangedWarning")));
        preferences.setLocale(sl);
        preferences.savePreferences();
    }
    
    @Override
    public String getName() {
        return Translate.text("Appearance");
    }
    
}
