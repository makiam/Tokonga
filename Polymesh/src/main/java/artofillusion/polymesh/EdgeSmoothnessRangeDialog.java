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

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Top label
        JLabel titleLabel = new JLabel(Translate.text("polymesh:specifySmoothnessRange"));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Center panel with smoothness controls
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Min smoothness row
        JPanel minPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        minPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JLabel minLabel = new JLabel(Translate.text("polymesh:minSmoothness"));
        minSmoothnessVF = new ValueField(0.0, ValueField.NONNEGATIVE);
        minSmoothnessTF = minSmoothnessVF.getComponent();
        minSmoothnessTF.setText("0.0");
        minPanel.add(minLabel);
        minPanel.add(Box.createHorizontalStrut(5));
        minPanel.add(minSmoothnessTF);

        minSmoothnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        minSmoothnessSlider.setAlignmentX(LEFT_ALIGNMENT);

        // Max smoothness row
        JPanel maxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        maxPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JLabel maxLabel = new JLabel(Translate.text("polymesh:maxSmoothness"));
        maxSmoothnessVF = new ValueField(1.0, ValueField.POSITIVE);
        maxSmoothnessTF = maxSmoothnessVF.getComponent();
        maxSmoothnessTF.setText("1.0");
        maxPanel.add(maxLabel);
        maxPanel.add(Box.createHorizontalStrut(5));
        maxPanel.add(maxSmoothnessTF);

        maxSmoothnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
        maxSmoothnessSlider.setAlignmentX(LEFT_ALIGNMENT);

        centerPanel.add(minPanel);
        centerPanel.add(minSmoothnessSlider);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(maxPanel);
        centerPanel.add(maxSmoothnessSlider);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JButton addButton = new JButton(Translate.text("polymesh:addToSelection"));
        JButton setButton = new JButton(Translate.text("polymesh:setSelection"));
        JButton cancelButton = new JButton(Translate.text("button.cancel"));

        buttonPanel.add(addButton);
        buttonPanel.add(setButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        minSmoothnessSlider.addChangeListener(e -> doMinSliderChanged());

        maxSmoothnessSlider.addChangeListener(e -> doMaxSliderChanged());
        minSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doValuesChanged");

        maxSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doValuesChanged");


        addButton.addActionListener(e -> doAdd());

        setButton.addActionListener(e -> doSet());

        cancelButton.addActionListener(e -> doCancel());

        getRootPane().setDefaultButton(setButton);
        setContentPane(mainPanel);
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
