/* Copyright (C) 2002-2004 by Peter Eastman
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
 * This dialog box allows the user to specify options for creating a tube.
 */
public class TubeDialog extends BDialog {

    final LayoutWindow window;
    final Curve theCurve;
    final ObjectInfo curveInfo;
    Tube theTube;

    final ValueField thicknessField;
    final BComboBox endsChoice;
    final ObjectPreviewCanvas preview;

    private static int counter = 1;

    public TubeDialog(LayoutWindow window, ObjectInfo curve) {
        super(window, Translate.text("Tools:tube.dialog.name"), true);
        this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());

        this.window = window;
        curveInfo = curve;
        theCurve = (Curve) curve.getObject();
        Scene scene = window.getScene();

        // Layout the window.
        FormContainer content = new FormContainer(4, 10);
        setContent(BOutline.createEmptyBorder(content, UIUtilities.getStandardDialogInsets()));
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, null, null));
        content.add(Translate.label("Tools:tube.width.label"), 0, 0);
        content.add(Translate.label("Tools:tube.cap.ends.label"), 0, 1);
        content.add(thicknessField = new ValueField(0.1, ValueField.POSITIVE, 5), 1, 0);
        thicknessField.addEventLink(ValueChangedEvent.class, this, "makeObject");
        content.add(endsChoice = new BComboBox(new String[]{Translate.text("Tools:tube.cap.end.open"), Translate.text("Tools:tube.cap.end.flat")}), 1, 1);
        endsChoice.setEnabled(!theCurve.isClosed());
        endsChoice.addEventLink(ValueChangedEvent.class, this, "makeObject");
        content.add(preview = new ObjectPreviewCanvas(null), 0, 2, 2, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
        preview.setPreferredSize(new Dimension(150, 150));
        RowContainer buttons = new RowContainer();
        content.add(buttons, 0, 3, 2, 1, new LayoutInfo());
        BButton okButton;
        buttons.add(okButton = Translate.button("ok", event -> commit()));
        buttons.add(Translate.button("cancel", event -> dispose()));

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

        makeObject();
        pack();
        UIUtilities.centerDialog(this, window);
        setVisible(true);
    }

    private void commit() {
        window.addObject(theTube, curveInfo.getCoords().duplicate(), "Tube " + (counter++), null);
        window.setSelection(window.getScene().getNumObjects() - 1);
        window.setUndoRecord(new UndoRecord(window, false, UndoRecord.DELETE_OBJECT, window.getScene().getNumObjects() - 1));
        window.updateImage();
        dispose();
    }

    // Create the Tube.
    private void makeObject() {
        MeshVertex[] vert = theCurve.getVertices();
        double[] thickness = new double[vert.length];
        for (int i = 0; i < thickness.length; i++) {
            thickness[i] = thicknessField.getValue();
        }
        int endsStyle;
        if (theCurve.isClosed()) {
            endsStyle = Tube.CLOSED_ENDS;
        } else if (endsChoice.getSelectedIndex() == 0) {
            endsStyle = Tube.OPEN_ENDS;
        } else {
            endsStyle = Tube.FLAT_ENDS;
        }
        theTube = new Tube(theCurve, thickness, endsStyle);
        ObjectInfo tubeInfo = new ObjectInfo(theTube, new CoordinateSystem(), "");
        Texture tex = window.getScene().getDefaultTexture();
        tubeInfo.setTexture(tex, tex.getDefaultMapping(theTube));
        preview.setObject(theTube);
        preview.repaint();
    }
}
