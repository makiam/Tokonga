/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.test.util;

import artofillusion.ArtOfIllusion;
import artofillusion.PluginRegistry;
import artofillusion.ui.ThemeManager;
import artofillusion.ui.Translate;
import java.util.Locale;
import org.junit.rules.ExternalResource;

/**
 *
 * @author MaksK
 */
public class RegisterTestResources extends ExternalResource {

    private static int ref = 0;

    @Override
    protected void before() throws Throwable {
        if (ref != 0) {
            return;
        }
        ref++;
        try {
            Translate.setLocale(Locale.US);
            PluginRegistry.registerResource("TranslateBundle", "artofillusion", ArtOfIllusion.class.getClassLoader(), "artofillusion", null);
            PluginRegistry.registerResource("UITheme", "default", ArtOfIllusion.class.getClassLoader(), "artofillusion/Icons/defaultTheme.xml", null);
            ThemeManager.initThemes();
        } catch (IllegalArgumentException iae) {
            ref++;
        }

    }

}
