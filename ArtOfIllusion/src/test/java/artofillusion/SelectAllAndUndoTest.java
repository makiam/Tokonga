/* Copyright (C) 2016-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.ObjectInfo;
import artofillusion.object.Sphere;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Locale;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Before;
import org.netbeans.jemmy.Bundle;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 *
 * @author MaksK
 */
public class SelectAllAndUndoTest {

    private static final Bundle bundle = new Bundle();
    private JMenuBarOperator appMainMenu;
    private JFrameOperator appFrame;

    private LayoutWindow layout;

    public SelectAllAndUndoTest() {
    }

    @BeforeClass
    public static void setupClass() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, URISyntaxException, IOException {

        Locale.setDefault(Locale.ENGLISH);
        new ClassReference("artofillusion.ArtOfIllusion").startApplication();
        bundle.load(ArtOfIllusion.class.getClassLoader().getResourceAsStream("artofillusion.properties"));
        JemmyProperties.setCurrentOutput(TestOut.getNullOutput());
    }

    @Before
    public void setUp() {
        appFrame = new JFrameOperator("Untitled");
        appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.closeSubmenus();

        layout = (LayoutWindow) ArtOfIllusion.getWindows()[0];
        layout.updateImage();
        layout.updateMenus();

    }

    @After
    public void done() {
        int scc = layout.getScene().getNumObjects();
        for (int i = 2; i < scc; i++) {
            layout.removeObject(2, null);
        }
        layout.updateImage();
        layout.updateMenus();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {

        }
    }

    @Test
    public void selectAllAndUndo() {
        for (int i = 0; i < 10; i++) {
            ObjectInfo test = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Test-" + i);
            layout.addObject(test, null);
        }

        layout.updateImage();
        layout.updateMenus();

        Assert.assertFalse(layout.isModified());

        appMainMenu.pushMenu("Edit|Select All");

        Assert.assertFalse(layout.isModified());

        appMainMenu.pushMenu("Edit|Undo");

        Assert.assertFalse(layout.isModified());

    }

}
