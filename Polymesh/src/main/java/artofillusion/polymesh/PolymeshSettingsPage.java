/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package artofillusion.polymesh;

import artofillusion.preferences.PreferencesEditor;
import buoy.widget.Widget;

/**
 *
 * @author MaksK
 */
public class PolymeshSettingsPage implements PreferencesEditor {
    private Widget widget = null;

    @Override
    public Widget getPreferencesPanel() {
        return widget = new buoy.widget.AWTWidget(new PolymeshSettingsPanel());
    }

    @Override
    public void savePreferences() {

    }

    @Override
    public String getName() {
        return "Polymesh Editor";
    }
}
