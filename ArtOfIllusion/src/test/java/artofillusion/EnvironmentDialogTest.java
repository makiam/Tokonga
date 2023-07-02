/* Copyright (C) 2022 by Maksim Khramov

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
import artofillusion.texture.UniformTexture;
import artofillusion.ui.ThemeManager;
import artofillusion.ui.Translate;
import java.lang.reflect.Field;
import java.util.Locale;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author MaksK
 */
@Slf4j
public class EnvironmentDialogTest {

    private static final ApplicationPreferences preferences = Mockito.mock(ApplicationPreferences.class);

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        PluginRegistry.registerResource("TranslateBundle", "artofillusion", ArtOfIllusion.class.getClassLoader(), "artofillusion", null);
        PluginRegistry.registerResource("UITheme", "default", ArtOfIllusion.class.getClassLoader(), "artofillusion/Icons/defaultTheme.xml", null);
        ThemeManager.initThemes();

        Mockito.when(preferences.getUseOpenGL()).thenReturn(false);
        Mockito.when(preferences.getInteractiveSurfaceError()).thenReturn(0.01);
        Mockito.when(preferences.getShowTravelCuesOnIdle()).thenReturn(false);

        Field pf = ArtOfIllusion.class.getDeclaredField("preferences");
        pf.setAccessible(true);
        pf.set(null, preferences);
        pf.setAccessible(false);

    }

    @Test
    public void testEnvironmentDialog() {
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
    public void testEnvironmentDialogUseFogCheck() throws InterruptedException {
        Scene scene = new Scene();
        scene.setFog(true, scene.getFogDistance());
        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);

        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());

        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);

        JCheckBoxOperator fogCB = new JCheckBoxOperator(dialog);
        Assert.assertTrue(fogCB.isSelected());

        fogCB.clickMouse();

        JButtonOperator ok = new JButtonOperator(dialog, Translate.text("ok"));
        ok.clickMouse();
        Assert.assertFalse(scene.getFogState());
    }

    @Test
    public void testEnvironmentDialogSetFogDistance() throws InterruptedException {
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

        Assert.assertEquals(25.0f, scene.getFogDistance(), 0);
    }

    @Test
    public void testGetEnvironmentMode0() {
        Scene scene = new Scene();
        scene.setEnvironmentMode(0);

        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);

        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());

        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JComboBoxOperator box = new JComboBoxOperator(dialog, 0);

        Assert.assertEquals(0, box.getSelectedIndex());
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
    }

    @Test
    public void testGetEnvironmentMode1() {
        Scene scene = new Scene();
        scene.setEnvironmentMode(1);

        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);

        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());

        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JComboBoxOperator box = new JComboBoxOperator(dialog, 0);

        Assert.assertEquals(1, box.getSelectedIndex());
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
    }

    @Test
    public void testGetEnvironmentMode2() {
        Scene scene = new Scene();
        scene.setEnvironmentMode(2);

        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);

        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());

        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        JComboBoxOperator box = new JComboBoxOperator(dialog, 0);

        Assert.assertEquals(2, box.getSelectedIndex());
        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
    }

    @Test
    public void testSetEnvironmentMode1() {
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

        Assert.assertEquals(1, scene.getEnvironmentMode());
    }

    @Test
    public void testSetEnvironmentMode2() {
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

        Assert.assertEquals(2, scene.getEnvironmentMode());
    }

    @Test
    public void testInvokeAmbientColorDialog() throws InterruptedException {
        Scene scene = new Scene();
        scene.setAmbientColor(new RGBColor(0, 0.5, 1));

        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);

        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());

        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);

        ColorWidgetComponentOperator cwc = new ColorWidgetComponentOperator(dialog, 0);

        SwingUtilities.invokeLater(() -> cwc.clickMouse());

        ColorDialogOperator cdo = new ColorDialogOperator(cwc.getSource().getName());
        RGBColor newColor = new RGBColor(0.3, 0.7, 0.5);
        cdo.setColor(newColor);
        cdo.commit();

        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();

        Assert.assertEquals(scene.getAmbientColor(), newColor);
        Assert.assertTrue(layout.isModified());
    }

    @Test
    public void testInvokeEnvColorDialog() throws InterruptedException {
        Scene scene = new Scene();
        scene.setEnvironmentColor(new RGBColor(0, 0.5, 1));

        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);

        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());

        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);

        ColorWidgetComponentOperator cwc = new ColorWidgetComponentOperator(dialog, 1);

        SwingUtilities.invokeLater(() -> cwc.clickMouse());

        ColorDialogOperator cdo = new ColorDialogOperator(cwc.getSource().getName());
        RGBColor newColor = new RGBColor(0.3, 0.7, 0.5);
        cdo.setColor(newColor);
        cdo.commit();

        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();

        Assert.assertEquals(scene.getEnvironmentColor(), newColor);
        Assert.assertTrue(layout.isModified());
    }

    @Test
    public void testInvokeFogColorDialog() throws InterruptedException {
        Scene scene = new Scene();
        scene.setFogColor(new RGBColor(0, 0.5, 1));

        LayoutWindow layout = new LayoutWindow(scene);
        layout.setVisible(true);

        JFrameOperator appFrame = new JFrameOperator(layout.getComponent());

        JMenuBarOperator appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);

        ColorWidgetComponentOperator cwc = new ColorWidgetComponentOperator(dialog, 2);

        SwingUtilities.invokeLater(() -> cwc.clickMouse());

        ColorDialogOperator cdo = new ColorDialogOperator(cwc.getSource().getName());
        RGBColor newColor = new RGBColor(0.3, 0.7, 0.5);
        cdo.setColor(newColor);
        cdo.commit();

        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();

        Assert.assertEquals(scene.getFogColor(), newColor);
        Assert.assertTrue(layout.isModified());
    }

    @Test
    public void testInvokeFogColorDialogChangeAndCancel() throws InterruptedException {
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

        SwingUtilities.invokeLater(() -> cwc.clickMouse());

        ColorDialogOperator cdo = new ColorDialogOperator(cwc.getSource().getName());
        RGBColor newColor = new RGBColor(0.3, 0.7, 0.5);
        cdo.setColor(newColor);
        cdo.cancel();

        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();

        Assert.assertEquals(scene.getFogColor(), fogColor);
        Assert.assertFalse(layout.isModified());
    }

    @Test
    public void testInvokeTextureDialog() throws InterruptedException {
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
        SwingUtilities.invokeLater(() -> chooseButton.clickMouse());

        JDialogOperator textureDialog = new JDialogOperator(Translate.text("objectTextureTitle"));
        JListOperator txList = new JListOperator(textureDialog);
        txList.selectItem(1);

        new JButtonOperator(textureDialog, Translate.text("ok")).clickMouse();

        new JButtonOperator(dialog, Translate.text("ok")).clickMouse();
        Assert.assertEquals(tx, scene.getEnvironmentTexture());
        Assert.assertNotNull(scene.getEnvironmentMapping());
        Assert.assertNotNull(scene.getEnvironmentParameterValues());
        Assert.assertTrue(layout.isModified());
    }

    @Test
    public void testInvokeTextureDialogAndCancel() throws InterruptedException {
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
        SwingUtilities.invokeLater(() -> chooseButton.clickMouse());

        JDialogOperator textureDialog = new JDialogOperator(Translate.text("objectTextureTitle"));
        JListOperator txList = new JListOperator(textureDialog);
        txList.selectItem(1);

        new JButtonOperator(textureDialog, Translate.text("ok")).clickMouse();

        dialog.close();

        Assert.assertEquals(scene.getDefaultTexture(), scene.getEnvironmentTexture());
        Assert.assertEquals(mapping, scene.getEnvironmentMapping());
        Assert.assertEquals(props, scene.getEnvironmentParameterValues());

        Assert.assertTrue(layout.isModified());
    }
}
