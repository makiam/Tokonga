/* Copyright (C) 1999-2011 by Peter Eastman
   Changes copyright (C) 2023-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * This class implements the dialog box which is used for the "Object Layout" and
 * "Transform Object" commands. It allows the user enter values for the position,
 * orientation, and size of an object. The initial values are passed to the constructor
 * in values[]. If this argument is omitted, all of the fields will initially be blank.
 * If transformLabels is true, the rows will be labelled "Move", "Rotate", and "Scale".
 * If it is false, they will be labelled "Position", "Orientation", and "Size".
 */
@Slf4j
public class TransformDialog extends BDialog {

    private final double[] initialValues;
    private final double[] finalValues;
    private final ValueField[] fields;
    private BCheckBox childrenBox;
    private BRadioButton selectionCenterBox;
    private RadioButtonGroup centerGroup;
    private boolean ok;

    private static boolean children = true;
    private static boolean selectionCenter = true;

    public TransformDialog(BFrame parent, String title, double[] values, boolean transformLabels, boolean extraOptions, boolean show) {
        super(parent, title, true);
        this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());
        initialValues = values;
        finalValues = values.clone();
        fields = new ValueField[9];
        layoutDialog(transformLabels, extraOptions);
        pack();
        setResizable(false);
        UIUtilities.centerDialog(this, parent);
        fields[0].requestFocus();
        if (show) {
            setVisible(true);
        }
    }

    public TransformDialog(BFrame parent, String title, double[] values, boolean transformLabels, boolean extraOptions) {
        this(parent, title, values, transformLabels, extraOptions, true);
    }

    public TransformDialog(BFrame parent, String title, boolean transformLabels, boolean extraOptions) {
        this(parent, title, new double[]{Double.NaN, Double.NaN, Double.NaN,
            Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
                transformLabels, extraOptions);
    }

    /**
     * Get the array of values the user entered.
     */
    public double[] getValues() {
        return finalValues;
    }

    /**
     * Determine whether the user clicked the OK button.
     */
    public boolean clickedOk() {
        return ok;
    }

    /**
     * Determine whether the operation should apply to unselected children of selected objects.
     */
    public boolean applyToChildren() {
        return children;
    }

    /**
     * Determine whether rotations should be around the center of each object, or the center
     * of the entire selection.
     */
    public boolean useSelectionCenter() {
        return selectionCenter;
    }

    void layoutDialog(boolean transformLabels, boolean extraOptions) {
        BorderContainer content = new BorderContainer();
        setContent(BOutline.createEmptyBorder(content, UIUtilities.getStandardDialogInsets()));
        FormContainer center = new FormContainer(4, extraOptions ? 6 : 4);
        content.add(center, BorderContainer.CENTER);
        LayoutInfo eastLayout = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(0, 0, 0, 5), null);
        if (transformLabels) {
            center.add(Translate.label("Move"), 0, 1, eastLayout);
            center.add(Translate.label("Rotate"), 0, 2, eastLayout);
            center.add(Translate.label("Scale"), 0, 3, eastLayout);
        } else {
            center.add(Translate.label("Position"), 0, 1, eastLayout);
            center.add(Translate.label("Orientation"), 0, 2, eastLayout);
            center.add(Translate.label("Size"), 0, 3, eastLayout);
        }
        LayoutInfo centerLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, null, null);
        center.add(new BLabel("X"), 1, 0, centerLayout);
        center.add(new BLabel("Y"), 2, 0, centerLayout);
        center.add(new BLabel("Z"), 3, 0, centerLayout);
        Object listener = new Object() {
            void processEvent() {
                for (int i = 0; i < finalValues.length; i++) {
                    finalValues[i] = fields[i].getValue();
                }
                if (childrenBox != null) {
                    children = childrenBox.getState();
                }
                if (centerGroup != null) {
                    selectionCenter = (centerGroup.getSelection() == selectionCenterBox);
                }
                dispatchEvent(new ValueChangedEvent(TransformDialog.this));
            }
        };
        for (int i = 0; i < 9; i++) {
            center.add(fields[i] = new ValueField(initialValues[i], ValueField.NONE), (i % 3) + 1, (i / 3) + 1);
            fields[i].addEventLink(ValueChangedEvent.class, listener);
        }
        if (extraOptions) {
            center.add(childrenBox = new BCheckBox(Translate.text("applyToUnselectedChildren"), children), 0, 4, 4, 1);
            childrenBox.addEventLink(ValueChangedEvent.class, listener);
            FormContainer extra = new FormContainer(2, 2);
            center.add(extra, 0, 5, 4, 1);
            extra.add(new BLabel(Translate.text("rotateScaleAround")), 0, 0, 1, 2, eastLayout);
            centerGroup = new RadioButtonGroup();
            centerGroup.addEventLink(SelectionChangedEvent.class, listener);
            LayoutInfo westLayout = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, null, null);
            BRadioButton objectCenterBox;
            extra.add(objectCenterBox = new BRadioButton(Translate.text("individualObjectCenters"), !selectionCenter, centerGroup), 1, 0, westLayout);
            extra.add(selectionCenterBox = new BRadioButton(Translate.text("centerOfSelection"), selectionCenter, centerGroup), 1, 1, westLayout);
        }

        RowContainer buttons = new RowContainer();
        content.add(buttons, BorderContainer.SOUTH, new LayoutInfo());
        BButton okButton;
        buttons.add(okButton = Translate.button("ok", event -> buttonOK()));
        buttons.add(Translate.button("cancel", event -> buttonCancel()));

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener action = e -> buttonCancel();
        this.getComponent().getRootPane().registerKeyboardAction(action, escape, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                buttonCancel();
            }
        });
        this.getComponent().getRootPane().setDefaultButton(okButton.getComponent());

    }

    private void buttonOK() {
        log.info("Accepting transform dialog");
        ok = true;
        dispose();
    }

    private void buttonCancel() {
        log.info("Cancelling transform dialog");
        ok = false;
        dispose();
    }

}
