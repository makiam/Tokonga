/* Copyright (C) 2022-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion;

import artofillusion.math.RGBColor;
import artofillusion.test.util.ColorDialogOperator;
import artofillusion.test.util.ColorWidgetComponentOperator;
import artofillusion.test.util.RegisterTestResources;
import artofillusion.test.util.SetupLocale;
import artofillusion.test.util.SetupLookAndFeel;
import artofillusion.test.util.SetupTheme;
import artofillusion.texture.UniformTexture;
import artofillusion.ui.ThemeManager;
import artofillusion.ui.Translate;

import java.lang.reflect.Field;
import javax.swing.SwingUtilities;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.netbeans.jemmy.operators.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author MaksK
 */
@Slf4j
@DisplayName("Environment Dialog Test")
@ExtendWith({SetupLocale.class, SetupLookAndFeel.class, RegisterTestResources.class, SetupTheme.class})
class EnvironmentDialogTest {

    private static final ApplicationPreferences preferences = Mockito.mock(ApplicationPreferences.class);

    @BeforeAll
    public static void setUpClass() throws Exception {


//        PluginRegistry.registerResource("TranslateBundle", "artofillusion", ArtOfIllusion.class.getClassLoader(), "artofillusion", null);
//        PluginRegistry.registerResource("UITheme", "default", ArtOfIllusion.class.getClassLoader(), "artofillusion/Icons/defaultTheme.xml", null);
//        ThemeManager.initThemes();

        Mockito.when(preferences.getLocale()).thenReturn(Locale.ENGLISH);
        Mockito.when(preferences.getUseOpenGL()).thenReturn(false);
        Mockito.when(preferences.getInteractiveSurfaceError()).thenReturn(0.01);
        Mockito.when(preferences.getShowTravelCuesOnIdle()).thenReturn(false);
        Field pf = ArtOfIllusion.class.getDeclaredField("preferences");
        pf.setAccessible(true);
        pf.set(null, preferences);
        pf.setAccessible(false);
    }

    @Test
    @DisplayName("Test Environment Dialog")
    void testEnvironmentDialog() {
        LayoutWindow layout = new LayoutWindow(new Scene());
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        dialog.close();
        System.out.println("********************");
    }

    @Test
    @DisplayName("Test Environment Dialog Use Fog Check")
    void testEnvironmentDialogUseFogCheck() throws InterruptedException {
        Scene scene = new Scene();
        scene.setFog(true, scene.getFogDistance());
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JCheckBoxOperator fogCB = new JCheckBoxOperator(dialog);
        Assertions.assertTrue(fogCB.isSelected());
        fogCB.clickMouse();
        JButtonOperator ok = new JButtonOperator(dialog, Translate.text("ok"));
        ok.clickMouse();
        Assertions.assertFalse(scene.getFogState());
    }

    @Test
    @DisplayName("Test Environment Dialog Set Fog Distance")
    void testEnvironmentDialogSetFogDistance() throws InterruptedException {
        Scene scene = new Scene();
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        org.netbeans.jemmy.operators.JTextFieldOperator fogDistance = new JTextFieldOperator(dialog);
        fogDistance.setText("25.0");
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assertions.assertEquals(25.0f, scene.getFogDistance(), 0);
    }

    @Test
    @DisplayName("Test Get Environment Mode 0")
    void testGetEnvironmentMode0() {
        Scene scene = new Scene();
        scene.setEnvironmentMode(0);
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JComboBoxOperator box = new JComboBoxOperator(dialog, 0);
        Assertions.assertEquals(0, box.getSelectedIndex());
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
    }

    @Test
    @DisplayName("Test Get Environment Mode 1")
    void testGetEnvironmentMode1() {
        Scene scene = new Scene();
        scene.setEnvironmentMode(1);
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JComboBoxOperator box = new JComboBoxOperator(dialog, 0);
        Assertions.assertEquals(1, box.getSelectedIndex());
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
    }

    @Test
    @DisplayName("Test Get Environment Mode 2")
    void testGetEnvironmentMode2() {
        Scene scene = new Scene();
        scene.setEnvironmentMode(2);
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JComboBoxOperator box = new JComboBoxOperator(dialog, 0);
        Assertions.assertEquals(2, box.getSelectedIndex());
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
    }

    @Test
    @DisplayName("Test Set Environment Mode 1")
    void testSetEnvironmentMode1() {
        Scene scene = new Scene();
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JComboBoxOperator box = new JComboBoxOperator(dialog, 0);
        box.setSelectedIndex(1);
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assertions.assertEquals(1, scene.getEnvironmentMode());
    }

    @Test
    @DisplayName("Test Set Environment Mode 2")
    void testSetEnvironmentMode2() {
        Scene scene = new Scene();
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JComboBoxOperator box = new JComboBoxOperator(dialog, 0);
        box.setSelectedIndex(2);
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assertions.assertEquals(2, scene.getEnvironmentMode());
    }

    @Test
    @DisplayName("Test Invoke Ambient Color Dialog")
    void testInvokeAmbientColorDialog() throws InterruptedException {
        Scene scene = new Scene();
        scene.setAmbientColor(new RGBColor(0, 0.5, 1));
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        ColorWidgetComponentOperator cwc = new ColorWidgetComponentOperator(dialog, 0);
        SwingUtilities.invokeLater(cwc::clickMouse);
        ColorDialogOperator cdo = new ColorDialogOperator(cwc.getSource().getName());
        RGBColor newColor = new RGBColor(0.3, 0.7, 0.5);
        cdo.setColor(newColor);
        cdo.commit();
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assertions.assertEquals(scene.getAmbientColor(), newColor);
        Assertions.assertTrue(layout.isModified());
    }

    @Test
    @DisplayName("Test Invoke Env Color Dialog")
    void testInvokeEnvColorDialog() throws InterruptedException {
        Scene scene = new Scene();
        scene.setEnvironmentColor(new RGBColor(0, 0.5, 1));
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        ColorWidgetComponentOperator cwc = new ColorWidgetComponentOperator(dialog, 1);
        SwingUtilities.invokeLater(cwc::clickMouse);
        ColorDialogOperator cdo = new ColorDialogOperator(cwc.getSource().getName());
        RGBColor newColor = new RGBColor(0.3, 0.7, 0.5);
        cdo.setColor(newColor);
        cdo.commit();
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assertions.assertEquals(scene.getEnvironmentColor(), newColor);
        Assertions.assertTrue(layout.isModified());
    }

    @Test
    @DisplayName("Test Invoke Fog Color Dialog")
    void testInvokeFogColorDialog() throws InterruptedException {
        Scene scene = new Scene();
        scene.setFogColor(new RGBColor(0, 0.5, 1));
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        ColorWidgetComponentOperator cwc = new ColorWidgetComponentOperator(dialog, 2);
        SwingUtilities.invokeLater(cwc::clickMouse);
        ColorDialogOperator cdo = new ColorDialogOperator(cwc.getSource().getName());
        RGBColor newColor = new RGBColor(0.3, 0.7, 0.5);
        cdo.setColor(newColor);
        cdo.commit();
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assertions.assertEquals(scene.getFogColor(), newColor);
        Assertions.assertTrue(layout.isModified());
    }

    @Test
    @DisplayName("Test Invoke Fog Color Dialog Change And Cancel")
    void testInvokeFogColorDialogChangeAndCancel() throws InterruptedException {
        Scene scene = new Scene();
        RGBColor fogColor = new RGBColor(0, 0.5, 1);
        scene.setFogColor(fogColor);
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        ColorWidgetComponentOperator cwc = new ColorWidgetComponentOperator(dialog, 2);
        SwingUtilities.invokeLater(cwc::clickMouse);
        ColorDialogOperator cdo = new ColorDialogOperator(cwc.getSource().getName());
        RGBColor newColor = new RGBColor(0.3, 0.7, 0.5);
        cdo.setColor(newColor);
        cdo.cancel();
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assertions.assertEquals(scene.getFogColor(), fogColor);
        Assertions.assertFalse(layout.isModified());
    }

    @Test
    @DisplayName("Test Invoke Texture Dialog")
    void testInvokeTextureDialog() throws InterruptedException {
        Scene scene = new Scene();
        UniformTexture tx = new UniformTexture();
        tx.setName("TestUniform");
        scene.addTexture(tx);
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        // Now clear some Scene's data to check it pushed back on dialog close
        {
            scene.setEnvironmentMapping(null);
            scene.setEnvironmentParameterValues(null);
        }
        new JComboBoxOperator(dialog, 0).setSelectedIndex(1);
        JButtonOperator chooseButton = new JButtonOperator(dialog, "Choose:");
        SwingUtilities.invokeLater(chooseButton::clickMouse);
        JDialogOperator textureDialog = new JDialogOperator(Translate.text("objectTextureTitle"));
        JListOperator txList = new JListOperator(textureDialog);
        txList.selectItem(1);
        new JButtonOperator(textureDialog, Translate.text("ok")).clickMouse();
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assertions.assertEquals(tx, scene.getEnvironmentTexture());
        Assertions.assertNotNull(scene.getEnvironmentMapping());
        Assertions.assertNotNull(scene.getEnvironmentParameterValues());
        Assertions.assertTrue(layout.isModified());
    }

    @Test
    @DisplayName("Test Invoke Texture Dialog And Cancel")
    void testInvokeTextureDialogAndCancel() throws InterruptedException {
        Scene scene = new Scene();
        UniformTexture tx = new UniformTexture();
        tx.setName("TestUniform");
        scene.addTexture(tx);
        var mapping = scene.getEnvironmentMapping();
        var props = scene.getEnvironmentParameterValues();
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);
        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());
        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        // Now clear some Scene's data to check it pushed back on dialog close
        {
            scene.setEnvironmentMapping(null);
            scene.setEnvironmentParameterValues(null);
        }
        new JComboBoxOperator(dialog, 0).setSelectedIndex(1);
        JButtonOperator chooseButton = new JButtonOperator(dialog, "Choose:");
        SwingUtilities.invokeLater(chooseButton::clickMouse);
        JDialogOperator textureDialog = new JDialogOperator(Translate.text("objectTextureTitle"));
        JListOperator txList = new JListOperator(textureDialog);
        txList.selectItem(1);
        new JButtonOperator(textureDialog, Translate.text("ok")).clickMouse();
        dialog.close();
        Assertions.assertEquals(scene.getDefaultTexture(), scene.getEnvironmentTexture());
        Assertions.assertEquals(mapping, scene.getEnvironmentMapping());
        Assertions.assertEquals(props, scene.getEnvironmentParameterValues());
        Assertions.assertTrue(layout.isModified());
    }
}
