/* Copyright (C) 2001-2004 by Peter Eastman, 2005 by Francois Guillet
   Changes copyright (C) 2023-2026 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.ui.Translate;

import artofillusion.ui.ValueField;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import buoy.event.ValueChangedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author MaksK
 */
@Slf4j
class EdgeSmoothnessRangeDialog extends JDialog {

    private ValueField minSmoothnessVF;
    private JSlider minSmoothnessSlider;
    private JTextField minSmoothnessTF;
    private ValueField maxSmoothnessVF;
    private JSlider maxSmoothnessSlider;
    private JTextField maxSmoothnessTF;
    private final boolean[] orSel;
    private final boolean[] newSel;
    private final PolyMeshEditorWindow owner;

    public EdgeSmoothnessRangeDialog(PolyMeshEditorWindow owner) {
        super(owner.getComponent(), Translate.text("polymesh:smoothnessRange"), true);

        this.owner = owner;
        int sl = owner.getSelection().length;
        orSel = new boolean[sl];
        newSel = new boolean[sl];
        System.arraycopy(owner.getSelection(), 0, orSel, 0, sl);
        owner.setSelection(newSel);

        initComponents();

        updateSelection();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener action = e -> doCancel();
        getRootPane().registerKeyboardAction(action, escape, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                doCancel();
            }
        });
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Create components
        var minLabel = new JLabel(Translate.text("polymesh:minSmoothness"));
        minSmoothnessVF = new ValueField(0.0, ValueField.NONNEGATIVE);
        minSmoothnessTF = minSmoothnessVF.getComponent();
        minSmoothnessTF.setText("0.0");
        minSmoothnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);

        var maxLabel = new JLabel(Translate.text("polymesh:maxSmoothness"));
        maxSmoothnessVF = new ValueField(1.0, ValueField.POSITIVE);
        maxSmoothnessTF = maxSmoothnessVF.getComponent();
        maxSmoothnessTF.setText("1.0");
        maxSmoothnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);

        JButton addButton = new JButton(Translate.text("polymesh:addToSelection"));
        JButton setButton = new JButton(Translate.text("polymesh:setSelection"));
        JButton cancelButton = new JButton(Translate.text("button.cancel"));

        // Add listeners
        minSmoothnessSlider.addChangeListener(e -> doMinSliderChanged());
        maxSmoothnessSlider.addChangeListener(e -> doMaxSliderChanged());
        minSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doValuesChanged");
        maxSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doValuesChanged");
        addButton.addActionListener(e -> doAdd());
        setButton.addActionListener(e -> doSet());
        cancelButton.addActionListener(e -> doCancel());

        // Layout using GroupLayout
        JPanel contentPane = new JPanel();
        GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Horizontal group
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(minLabel)
                    .addComponent(maxLabel))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(minSmoothnessTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxSmoothnessTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
            .addComponent(minSmoothnessSlider)
            .addComponent(maxSmoothnessSlider)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(addButton)
                .addComponent(setButton)
                .addComponent(cancelButton)));

        // Vertical group
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(minLabel)
                .addComponent(minSmoothnessTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(minSmoothnessSlider)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(maxLabel)
                .addComponent(maxSmoothnessTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(maxSmoothnessSlider)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(addButton)
                .addComponent(setButton)
                .addComponent(cancelButton)));

        getRootPane().setDefaultButton(setButton);
        setContentPane(contentPane);
    }

    private void doMinSliderChanged() {
        minSmoothnessVF.setValue(minSmoothnessSlider.getValue() / 100.0);
        updateSelection();
    }

    private void doMaxSliderChanged() {
        maxSmoothnessVF.setValue(maxSmoothnessSlider.getValue() / 100.0);
        updateSelection();
    }

    private void doValuesChanged() {
        updateSelection();
    }

    private void updateSelection() {
        PolyMesh mesh = (PolyMesh) owner.getObject().getObject();
        PolyMesh.Wedge[] edges = mesh.getEdges();
        double min = minSmoothnessVF.getValue();
        double max = maxSmoothnessVF.getValue();
        for (int i = 0; i < newSel.length; i++) {
            newSel[i] = (edges[i].smoothness >= min) & (edges[i].smoothness <= max);
        }
        owner.setSelection(newSel);
    }

    private void doAdd() {
        for (int i = 0; i < orSel.length; i++) {
            orSel[i] |= newSel[i];
        }
        owner.setSelection(orSel);
        dispose();
    }

    private void doSet() {
        owner.setSelection(newSel);
        dispose();
    }

    private void doCancel() {
        owner.setSelection(orSel);
        dispose();
    }

}
