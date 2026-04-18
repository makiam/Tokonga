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
import buoy.event.ValueChangedEvent;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import lombok.extern.slf4j.Slf4j;

/**
 * A dialog presenting options to find similar faces
 */
@Slf4j
class FindSimilarFacesDialog extends JDialog {

    private final boolean[] orSelection;

    private JCheckBox normalCB;
    private JCheckBox looseShapeCB;
    private JCheckBox strictShapeCB;
    private JLabel tolerance1Label;
    private JLabel tolerance2Label;
    private JLabel tolerance3Label;
    private ValueField normalCBVF;
    private ValueField looseShapeCBVF;
    private ValueField strictShapeCBVF;
    private JTextField normalCBTF;
    private JTextField looseShapeCBTF;
    private JTextField strictShapeCBTF;
    private JButton okButton;
    private final PolyMeshEditorWindow owner;
    private final PolyMesh mesh;

    public FindSimilarFacesDialog(PolyMeshEditorWindow owner) {
        super(owner.getComponent(), Translate.text("polymesh:similarFacesTitle"), true);
        this.owner = owner;
        this.mesh = (PolyMesh) owner.getObject().getObject();
        this.orSelection = owner.getSelection();

        initComponents();

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
        setLocationRelativeTo(owner.getComponent());
    }

    private void initComponents() {
        var titleLabel = new JLabel(Translate.text("polymesh:chooseSelectionCriterion"));

        normalCB = new JCheckBox(Translate.text("polymesh:normalCBText"));
        looseShapeCB = new JCheckBox(Translate.text("polymesh:looseShapeCBText"));
        strictShapeCB = new JCheckBox(Translate.text("polymesh:strictShapeCBText"));

        tolerance1Label = new JLabel(Translate.text("polymesh:tolerance"));
        tolerance2Label = new JLabel(Translate.text("polymesh:tolerance"));
        tolerance3Label = new JLabel(Translate.text("polymesh:tolerance"));

        normalCBVF = new ValueField(PolyMeshEditorWindow.getNormalTol(), ValueField.NONE);
        normalCBTF = normalCBVF.getComponent();
        looseShapeCBVF = new ValueField(PolyMeshEditorWindow.getLooseShapeTol(), ValueField.NONE);
        looseShapeCBTF = looseShapeCBVF.getComponent();
        strictShapeCBVF = new ValueField(PolyMeshEditorWindow.getStrictShapeTol(), ValueField.NONE);
        strictShapeCBTF = strictShapeCBVF.getComponent();

        okButton = new JButton(Translate.text("button.ok"));
        var cancelButton = new JButton(Translate.text("button.cancel"));

        // Initial state: text fields disabled until checkbox selected
        tolerance1Label.setEnabled(false);
        normalCBTF.setEnabled(false);
        tolerance2Label.setEnabled(false);
        looseShapeCBTF.setEnabled(false);
        tolerance3Label.setEnabled(false);
        strictShapeCBTF.setEnabled(false);
        okButton.setEnabled(false);

        // Event listeners
        normalCBVF.addEventLink(ValueChangedEvent.class, this, "doTolValueChanged");
        looseShapeCBVF.addEventLink(ValueChangedEvent.class, this, "doTolValueChanged");
        strictShapeCBVF.addEventLink(ValueChangedEvent.class, this, "doTolValueChanged");
        normalCB.addActionListener(e -> doCBValueChanged());
        looseShapeCB.addActionListener(e -> doCBValueChanged());
        strictShapeCB.addActionListener(e -> doCBValueChanged());
        okButton.addActionListener(e -> doOK());
        cancelButton.addActionListener(e -> doCancel());

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(titleLabel)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addComponent(normalCB, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tolerance1Label)
                    .addComponent(normalCBTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addComponent(looseShapeCB, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tolerance2Label)
                    .addComponent(looseShapeCBTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addComponent(strictShapeCB, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tolerance3Label)
                    .addComponent(strictShapeCBTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton))
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(titleLabel)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(normalCB)
                    .addComponent(tolerance1Label)
                    .addComponent(normalCBTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(looseShapeCB)
                    .addComponent(tolerance2Label)
                    .addComponent(looseShapeCBTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(strictShapeCB)
                    .addComponent(tolerance3Label)
                    .addComponent(strictShapeCBTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, okButton, cancelButton);
        getRootPane().setDefaultButton(okButton);
    }

    private void doTolValueChanged() {
        fetchTolValues();
        double normalTol = PolyMeshEditorWindow.getNormalTol();
        double looseShapeTol = PolyMeshEditorWindow.getLooseShapeTol();
        double strictShapeTol = PolyMeshEditorWindow.getStrictShapeTol();
        owner.setSelection(mesh.findSimilarFaces(orSelection, isNormal(), normalTol, isLoose(), looseShapeTol, isStrict(), strictShapeTol));
        owner.objectChanged();
        owner.updateImage();
    }

    private void doCBValueChanged() {
        tolerance1Label.setEnabled(normalCB.isSelected());
        normalCBTF.setEnabled(normalCB.isSelected());
        tolerance2Label.setEnabled(looseShapeCB.isSelected());
        looseShapeCBTF.setEnabled(looseShapeCB.isSelected());
        tolerance3Label.setEnabled(strictShapeCB.isSelected());
        strictShapeCBTF.setEnabled(strictShapeCB.isSelected());
        doTolValueChanged();
        okButton.setEnabled(normalCB.isSelected() || looseShapeCB.isSelected() || strictShapeCB.isSelected());
    }

    private void doCancel() {
        owner.setSelection(orSelection);
        owner.objectChanged();
        owner.updateImage();
        dispose();
    }

    private void doOK() {
        fetchTolValues();
        dispose();
    }

    private void fetchTolValues() {
        if (normalCB.isSelected()) {
            log.info("Normal: {}", normalCBVF.getValue());
            PolyMeshEditorWindow.setNormalTol(normalCBVF.getValue());
        }
        if (looseShapeCB.isSelected()) {
            PolyMeshEditorWindow.setLooseShapeTol(looseShapeCBVF.getValue());
        }
        if (strictShapeCB.isSelected()) {
            PolyMeshEditorWindow.setStrictShapeTol(strictShapeCBVF.getValue());
        }
    }

    public boolean isNormal() {
        return normalCB.isSelected();
    }

    public boolean isLoose() {
        return looseShapeCB.isSelected();
    }

    public boolean isStrict() {
        return strictShapeCB.isSelected();
    }

}
