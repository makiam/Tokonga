/* Copyright (C) 2001-2004 by Peter Eastman, 2005 by Francois Guillet
   Changes copyright (C) 2023-2026 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.UndoRecord;
import artofillusion.ui.Translate;
import artofillusion.ui.ValueField;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import buoy.event.ValueChangedEvent;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 *
 * @author MaksK
 */
@Slf4j
class ControlledSmoothingDialog extends JDialog {

    private final PolyMeshEditorWindow owner;
    private JCheckBox applyCB;
    private JLabel maxAngle;
    private JLabel minAngle;
    private JLabel angleRange;
    private JLabel smoothnessRange;
    private JLabel minSmoothness;
    private JLabel maxSmoothness;
    private ValueField minAngleVF;
    private ValueField maxAngleVF;
    private ValueField minSmoothnessVF;
    private ValueField maxSmoothnessVF;
    private final boolean backApply;
    private final double backMaxAngle;
    private final double backMinAngle;
    private final double backMinSmoothness;
    private final double backMaxSmoothness;
    private final PolyMesh mesh;
    private final PolyMesh prevMesh;
    private JSlider minAngleSlider;
    private JSlider maxAngleSlider;
    private JSlider minSmoothnessSlider;
    private JSlider maxSmoothnessSlider;

    public ControlledSmoothingDialog(PolyMeshEditorWindow owner) {
        super(owner.getComponent(), Translate.text("polymesh:controlledSmoothnessDialogTitle"), true);
        this.owner = owner;

        mesh = (PolyMesh) owner.getObject().getObject();
        prevMesh = mesh.duplicate();
        backApply = mesh.isControlledSmoothing();
        backMinAngle = mesh.getMinAngle();
        backMaxAngle = mesh.getMaxAngle();
        backMinSmoothness = mesh.getMinSmoothness();
        backMaxSmoothness = mesh.getMaxSmoothness();
        try {
            initComponents();
            applyCB.setSelected(mesh.isControlledSmoothing());
            minAngleVF.setValue(mesh.getMinAngle());
            maxAngleVF.setValue(mesh.getMaxAngle());
            minSmoothnessVF.setValue(mesh.getMinSmoothness());
            maxSmoothnessVF.setValue(mesh.getMaxSmoothness());
            doApplyCB();
        } catch (Exception ex) {
            log.atError().setCause(ex).log("Error creating ControlledSmoothingDialog due {}", ex.getLocalizedMessage());
        }
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener action = e -> doCancel();
        this.getRootPane().registerKeyboardAction(action, escape, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                doCancel();
            }
        });
        pack();
        setLocationRelativeTo(owner.getComponent());

    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        var okButton = new JButton();
        var cancelButton = new JButton();
        minAngleVF = new ValueField(0.0, ValueField.NONNEGATIVE);
        maxAngleVF = new ValueField(180.0, ValueField.NONNEGATIVE);
        minSmoothnessVF = new ValueField(1.0, ValueField.NONNEGATIVE);
        maxSmoothnessVF = new ValueField(0.0, ValueField.NONNEGATIVE);
        minAngleSlider = new JSlider();
        maxAngleSlider = new JSlider();
        minSmoothnessSlider = new JSlider();
        maxSmoothnessSlider = new JSlider();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


        // Apply checkbox
        applyCB = new JCheckBox();
        applyCB.setText(Translate.text("polymesh:applyCB"));
        applyCB.addActionListener(e -> doApplyCB());

        // Angle labels
        angleRange = new JLabel();
        angleRange.setText(Translate.text("polymesh:angleRange"));

        minAngle = new JLabel();
        minAngle.setText(Translate.text("polymesh:minAngle"));

        maxAngle = new JLabel();
        maxAngle.setText(Translate.text("polymesh:maxAngle"));

        // Smoothness labels
        smoothnessRange = new JLabel();
        smoothnessRange.setText(Translate.text("polymesh:smoothnessRange"));

        minSmoothness = new JLabel();
        minSmoothness.setText(Translate.text("polymesh:minSmoothness"));

        maxSmoothness = new JLabel();
        maxSmoothness.setText(Translate.text("polymesh:maxSmoothness"));

        // Configure sliders
        minAngleSlider.setOrientation(JSlider.HORIZONTAL);
        minAngleSlider.setMinimum(0);
        minAngleSlider.setMaximum(100);
        minAngleSlider.addChangeListener(e -> doApplySL());

        maxAngleSlider.setOrientation(JSlider.HORIZONTAL);
        maxAngleSlider.setMinimum(0);
        maxAngleSlider.setMaximum(100);
        maxAngleSlider.addChangeListener(e -> doApplySL());

        minSmoothnessSlider.setOrientation(JSlider.HORIZONTAL);
        minSmoothnessSlider.setMinimum(0);
        minSmoothnessSlider.setMaximum(100);
        minSmoothnessSlider.addChangeListener(e -> doApplySL());

        maxSmoothnessSlider.setOrientation(JSlider.HORIZONTAL);
        maxSmoothnessSlider.setMinimum(0);
        maxSmoothnessSlider.setMaximum(100);
        maxSmoothnessSlider.addChangeListener(e -> doApplySL());

        // Configure text fields
        JTextField minAngleField = minAngleVF.getComponent();
        minAngleField.setColumns(5);
        minAngleVF.addEventLink(ValueChangedEvent.class, this, "doApplyVF");

        JTextField maxAngleField = maxAngleVF.getComponent();
        maxAngleField.setColumns(5);
        maxAngleVF.addEventLink(ValueChangedEvent.class, this, "doApplyVF");

        JTextField minSmoothnessField = minSmoothnessVF.getComponent();
        minSmoothnessField.setColumns(5);
        minSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doApplyVF");

        JTextField maxSmoothnessField = maxSmoothnessVF.getComponent();
        maxSmoothnessField.setColumns(5);
        maxSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doApplyVF");

        // OK/Cancel buttons
        okButton.setText(Translate.text("button.ok"));
        okButton.addActionListener(e -> doOK());

        cancelButton.setText(Translate.text("button.cancel"));
        cancelButton.addActionListener(e -> doCancel());

        // Titled border panel
        JPanel paramsPanel = new JPanel();
        paramsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            Translate.text("polymesh:smoothingParameters")));

        GroupLayout paramsLayout = new GroupLayout(paramsPanel);
        paramsPanel.setLayout(paramsLayout);
        paramsLayout.setHorizontalGroup(
            paramsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(paramsLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(paramsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(angleRange)
                        .addGroup(paramsLayout.createSequentialGroup()
                            .addComponent(minAngle)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(minAngleField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addComponent(minAngleSlider, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(paramsLayout.createSequentialGroup()
                            .addComponent(maxAngle)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(maxAngleField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addComponent(maxAngleSlider, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(smoothnessRange)
                        .addGroup(paramsLayout.createSequentialGroup()
                            .addComponent(minSmoothness)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(minSmoothnessField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addComponent(minSmoothnessSlider, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(paramsLayout.createSequentialGroup()
                            .addComponent(maxSmoothness)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(maxSmoothnessField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addComponent(maxSmoothnessSlider, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap()));

        paramsLayout.setVerticalGroup(
            paramsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(paramsLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(angleRange)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(paramsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(minAngle)
                        .addComponent(minAngleField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(minAngleSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(paramsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(maxAngle)
                        .addComponent(maxAngleField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(maxAngleSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(smoothnessRange)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(paramsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(minSmoothness)
                        .addComponent(minSmoothnessField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(minSmoothnessSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(paramsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(maxSmoothness)
                        .addComponent(maxSmoothnessField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(maxSmoothnessSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));


        // Main layout
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(applyCB)
                        .addComponent(paramsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(okButton)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cancelButton)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(applyCB)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(paramsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton)
                        .addComponent(okButton))
                    .addContainerGap())
        );

        getRootPane().setDefaultButton(okButton);
    }

    private void doCancel() {
        mesh.setControlledSmoothing(backApply);
        mesh.setMinAngle(backMinAngle);
        mesh.setMaxAngle(backMaxAngle);
        mesh.setMinSmoothness((float) backMinSmoothness);
        mesh.setMaxSmoothness((float) backMaxSmoothness);
        owner.objectChanged();
        owner.updateImage();
        dispose();
    }

    private void doApplyCB() {
        boolean state = applyCB.isSelected();
        angleRange.setEnabled(state);
        smoothnessRange.setEnabled(state);
        minAngle.setEnabled(state);
        maxAngle.setEnabled(state);
        minAngleVF.setEnabled(state);
        maxAngleVF.setEnabled(state);
        minSmoothness.setEnabled(state);
        maxSmoothness.setEnabled(state);
        minSmoothnessVF.setEnabled(state);
        maxSmoothnessVF.setEnabled(state);
        minAngleSlider.setEnabled(state);
        maxAngleSlider.setEnabled(state);
        minSmoothnessSlider.setEnabled(state);
        maxSmoothnessSlider.setEnabled(state);
        doApplyVF();
    }

    private void doApplyVF() {
        boolean state = applyCB.isSelected();
        if (minAngleVF.getValue() > 180.0) {
            minAngleVF.setValue(180);
        }
        if (maxAngleVF.getValue() > 180.0) {
            maxAngleVF.setValue(180);
        }
        if (minSmoothnessVF.getValue() > 1.0) {
            minSmoothnessVF.setValue(1.0);
        }
        if (maxSmoothnessVF.getValue() > 1.0) {
            maxSmoothnessVF.setValue(1.0);
        }
        int val = (int) Math.round(minAngleVF.getValue() / 1.80);
        minAngleSlider.setValue(val);
        val = (int) Math.round(maxAngleVF.getValue() / 1.80);
        maxAngleSlider.setValue(val);
        val = (int) Math.round(minSmoothnessVF.getValue() * 100);
        minSmoothnessSlider.setValue(val);
        val = (int) Math.round(maxSmoothnessVF.getValue() * 100);
        maxSmoothnessSlider.setValue(val);
        mesh.setControlledSmoothing(state);
        mesh.setMinAngle(minAngleVF.getValue());
        mesh.setMaxAngle(maxAngleVF.getValue());
        mesh.setMinSmoothness((float) minSmoothnessVF.getValue());
        mesh.setMaxSmoothness((float) maxSmoothnessVF.getValue());
        owner.objectChanged();
        owner.updateImage();
    }

    private void doApplySL() {
        boolean state = applyCB.isSelected();
        mesh.setControlledSmoothing(state);
        double val = minAngleSlider.getValue() * 1.8;
        mesh.setMinAngle(val);
        minAngleVF.setValue(val);
        val = maxAngleSlider.getValue() * 1.8;
        mesh.setMaxAngle(val);
        maxAngleVF.setValue(val);
        val = ((float) minSmoothnessSlider.getValue()) / 100.0;
        mesh.setMinSmoothness((float) val);
        minSmoothnessVF.setValue(val);
        val = ((float) maxSmoothnessSlider.getValue()) / 100.0;
        mesh.setMaxSmoothness((float) val);
        maxSmoothnessVF.setValue(val);
        owner.objectChanged();
        owner.updateImage();
    }

    private void doOK() {
        doApplyVF();
        owner.setUndoRecord(new UndoRecord(owner, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        dispose();
    }

}
