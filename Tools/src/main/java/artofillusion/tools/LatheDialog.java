/* Copyright (C) 2001-2007 by Peter Eastman
   Changes copyright (C) 2020-2022 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This dialog box allows the user to specify options for creating lathed objects.
 */
public class LatheDialog extends BDialog {

    final LayoutWindow window;
    final Curve theCurve;
    final ObjectInfo curveInfo;
    final RadioButtonGroup axisGroup;
    final BRadioButton xBox;
    final BRadioButton yBox;
    final BRadioButton zBox;
    final BRadioButton endsBox;
    final ValueField radiusField;
    final ValueField segmentsField;
    final ValueSlider angleSlider;

    final ObjectPreviewCanvas preview;

    private static int counter = 1;

    public LatheDialog(LayoutWindow window, ObjectInfo curve) {
        super(window, Translate.text("Tools:lathe.dialog.name"), true);
        this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());

        this.window = window;
        theCurve = (Curve) curve.getObject();
        curveInfo = curve;

        // Layout the window.
        FormContainer content = new FormContainer(3, 10);
        setContent(BOutline.createEmptyBorder(content, UIUtilities.getStandardDialogInsets()));
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, new Insets(0, 0, 0, 5), null));
        content.add(Translate.label("Tools:lathe.axis.select.label"), 0, 0, 2, 1);
        axisGroup = new RadioButtonGroup();
        content.add(xBox = new BRadioButton(Translate.text("Tools:lathe.rotate.xaxis"), false, axisGroup), 0, 1, 2, 1);
        content.add(yBox = new BRadioButton(Translate.text("Tools:lathe.rotate.yaxis"), true, axisGroup), 0, 2, 2, 1);
        content.add(zBox = new BRadioButton(Translate.text("Tools:lathe.rotate.zaxis"), false, axisGroup), 0, 3, 2, 1);
        content.add(endsBox = new BRadioButton(Translate.text("Tools:lathe.rotate.endpoints"), false, axisGroup), 0, 4, 2, 1);
        axisGroup.addEventLink(SelectionChangedEvent.class, this, "makeObject");
        content.add(Translate.label("Tools:lathe.rotation.angle.label"), 0, 5, 2, 1);
        content.add(angleSlider = new ValueSlider(0.0, 360.0, 180, 360.0), 0, 6, 2, 1);
        angleSlider.addEventLink(ValueChangedEvent.class, this, "makeObject");
        content.add(Translate.label("Tools:lathe.radius.label"), 0, 7);
        content.add(Translate.label("Tools:lathe.segments.label"), 0, 8);
        content.add(radiusField = new ValueField(0.0, ValueField.NONE), 1, 7);
        content.add(segmentsField = new ValueField(8.0, ValueField.POSITIVE + ValueField.INTEGER), 1, 8);
        radiusField.addEventLink(ValueChangedEvent.class, this, "makeObject");
        segmentsField.addEventLink(ValueChangedEvent.class, this, "makeObject");

        // Add the preview canvas.
        content.add(preview = new ObjectPreviewCanvas(null), 2, 0, 1, 9);
        preview.setPreferredSize(new Dimension(150, 150));

        // Add the buttons at the bottom.
        RowContainer buttons = new RowContainer();
        BButton okButton;
        buttons.add(okButton = Translate.button("ok", event -> commit()));
        buttons.add(Translate.button("cancel", event -> dispose()));
        content.add(buttons, 0, 9, 3, 1, new LayoutInfo());

        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        this.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        this.getComponent().getRootPane().setDefaultButton(okButton.getComponent());

        selectDefaults();
        makeObject();
        pack();
        UIUtilities.centerDialog(this, window);
        setVisible(true);
    }

    private void commit() {
        CoordinateSystem coords = curveInfo.getCoords().duplicate();
        Vec3 offset = curveInfo.getCoords().fromLocal().times(theCurve.getVertices()[0].r).minus(coords.fromLocal().times(((Mesh) preview.getObject().getObject()).getVertices()[0].r));
        coords.setOrigin(coords.getOrigin().plus(offset));
        window.addObject(preview.getObject().getObject(), coords, "Lathed Object " + (counter++), null);
        window.setSelection(window.getScene().getNumObjects() - 1);
        window.setUndoRecord(new UndoRecord(window, false, UndoRecord.DELETE_OBJECT, window.getScene().getNumObjects() - 1));
        window.updateImage();
        dispose();
    }

    // Select default values for the various options.
    private void selectDefaults() {
        MeshVertex[] vert = theCurve.getVertices();

        if (!theCurve.isClosed() && vert[0].r.distance(vert[vert.length - 1].r) > 0.0) {
            axisGroup.setSelection(endsBox);
            return;
        }
        endsBox.setEnabled(false);
        double minx = Double.MAX_VALUE;
        for (int i = 0; i < vert.length; i++) {
            if (vert[i].r.x < minx) {
                minx = vert[i].r.x;
            }
        }
        minx = Math.max(-minx, 0.0);
        radiusField.setValue(Math.ceil(minx));
    }

    // Create the extruded object.
    private void makeObject() {
        int segments = (int) segmentsField.getValue();
        double angle = angleSlider.getValue(), radius = radiusField.getValue();
        int axis;
        if (axisGroup.getSelection() == xBox) {
            axis = LatheTool.X_AXIS;
        } else if (axisGroup.getSelection() == yBox) {
            axis = LatheTool.Y_AXIS;
        } else if (axisGroup.getSelection() == zBox) {
            axis = LatheTool.Z_AXIS;
        } else {
            axis = LatheTool.AXIS_THROUGH_ENDS;
        }
        SplineMesh mesh = (SplineMesh) LatheTool.latheCurve(theCurve, axis, segments, angle, radius);
        Texture tex = window.getScene().getDefaultTexture();
        mesh.setTexture(tex, tex.getDefaultMapping(mesh));
        preview.setObject(mesh);
        preview.repaint();
    }

}
