/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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
