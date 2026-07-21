/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.material.UniformMaterial;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.ObjectInfo;
import artofillusion.test.util.PropertiesPaneOperator;
import buoy.widget.BScrollPane;
import buoy.widget.BTabbedPane;
import buoyx.docking.DockableWidget;
import buoyx.docking.DockingContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

class AssignMaterialUndoTest {

    private JFrameOperator appFrame;
    private JMenuBarOperator appMainMenu;

    private LayoutWindow layout;

    @BeforeAll
    static void setupClass() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        Locale.setDefault(Locale.ENGLISH);
        new ClassReference("artofillusion.ArtOfIllusion").startApplication();

        JemmyProperties.setCurrentOutput(TestOut.getNullOutput());
    }

    @BeforeEach
    void setUp() {
        appFrame = new JFrameOperator("Untitled");
        appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.closeSubmenus();
        layout = (LayoutWindow) ArtOfIllusion.getWindows()[0];
        layout.updateImage();
        layout.updateMenus();
    }

    @Test
    void testAssignMaterialUI() {
        var cube = new ObjectInfo(new Cube(1,1,1), new CoordinateSystem(), "I am Cube");
        layout.addObject(cube, null);

        layout.setSelection(2);
        layout.updateImage();

        EventQueue.invokeLater(() -> layout.dispatchEvent( new SceneChangedEvent(layout)));

        Assertions.assertNull(cube.getGeometry().getMaterial());
        Assertions.assertNull(cube.getGeometry().getMaterialMapping());

        var ppo = getPropertiesPane(layout);
        JComboBox cbo = (JComboBox)ppo.getMaterialsComboBoxOperator().getSource();


        SwingUtilities.invokeLater(() -> cbo.setSelectedIndex(1));

        new JDialogOperator(appFrame).close();



    }

    @Test
    void testAssignMaterialAndUndoAndRedo() {
        var object = new ObjectInfo(new Cube(1,1,1), new CoordinateSystem(), "I am Cube");
        var mat = new UniformMaterial();

        Assertions.assertNull(object.getGeometry().getMaterial());
        Assertions.assertNull(object.getGeometry().getMaterialMapping());
        var scene = layout.getScene();
        scene.addObject(object, null);
        scene.addMaterial(mat);

        UndoRecord undo = new UndoRecord(layout);
        undo.addCommand(UndoRecord.COPY_OBJECT, object.getObject(), object.getObject().duplicate());
        var mm = mat.getDefaultMapping(object.getObject());
        object.setMaterial(mat, mm);

        layout.setUndoRecord(undo);
        Assertions.assertEquals(mat, scene.getObject("I am Cube").getGeometry().getMaterial());

        layout.getUndoStack().executeUndo();
        Assertions.assertNull(scene.getObject("I am Cube").getGeometry().getMaterial());

        layout.getUndoStack().executeRedo();
        Assertions.assertEquals(mat, scene.getObject("I am Cube").getGeometry().getMaterial());
    }


    @Test
    void testAssignMaterialAndUndoAndRedoWithAction() {
        var object = new ObjectInfo(new Cube(1,1,1), new CoordinateSystem(), "I am Cube");
        var mat = new UniformMaterial();

        Assertions.assertNull(object.getGeometry().getMaterial());
        Assertions.assertNull(object.getGeometry().getMaterialMapping());
        var scene = layout.getScene();
        scene.addObject(object, null);
        scene.addMaterial(mat);

        CompoundUndoableEdit convert = new CompoundUndoableEdit();

        convert.add(new ChangeObjectMaterialEdit(object, mat));


        layout.setUndoRecord(new UndoRecord(layout, false, convert.execute()));

        Assertions.assertEquals(mat, scene.getObject("I am Cube").getGeometry().getMaterial());

        layout.getUndoStack().executeUndo();
        Assertions.assertNull(scene.getObject("I am Cube").getGeometry().getMaterial());

        layout.getUndoStack().executeRedo();
        Assertions.assertEquals(mat, scene.getObject("I am Cube").getGeometry().getMaterial());
    }



    PropertiesPaneOperator getPropertiesPane(LayoutWindow layout) {
        DockingContainer rightDock = layout.getDockingContainer(BTabbedPane.RIGHT);
        DockableWidget propsWidget = rightDock.getChild(0, 1);
        BScrollPane scroller = (BScrollPane) propsWidget.getContent();


        return new PropertiesPaneOperator((Container)scroller.getContent().getComponent());
    }


}
