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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import javax.swing.KeyStroke;
import lombok.extern.slf4j.Slf4j;

/**
 * A dialog presenting options to find similar edges
 */
@Slf4j
class FindSimilarEdgesDialog extends JDialog {

    private final boolean[] orSelection;
    private ValueField toleranceVF;

    private final PolyMeshEditorWindow owner;
    private final PolyMesh mesh;

    public FindSimilarEdgesDialog(PolyMeshEditorWindow owner) {
        super(owner.getComponent(), Translate.text("polymesh:similarEdgesTitle"), true);

        this.owner = owner;
        this.orSelection = owner.getSelection();
        this.mesh = (PolyMesh) owner.getObject().getObject();

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
        doTolValueChanged();
    }

    private void initComponents() {
        var toleranceLabel = new JLabel(Translate.text("polymesh:toleranceEdges"));
        toleranceVF = new ValueField(PolyMeshEditorWindow.getEdgeTol(), ValueField.NONE);
        toleranceVF.addEventLink(ValueChangedEvent.class, this, "doTolValueChanged");

        var okButton = new JButton(Translate.text("button.ok"));
        var cancelButton = new JButton(Translate.text("button.cancel"));


        okButton.addActionListener(e -> doOK());
        cancelButton.addActionListener(e -> doCancel());

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(toleranceLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(toleranceVF.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(cancelButton))
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(toleranceLabel)
                    .addComponent(toleranceVF.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, okButton, cancelButton);
        getRootPane().setDefaultButton(okButton);
    }

    private void doTolValueChanged() {
        owner.setSelection(mesh.findSimilarEdges(orSelection, toleranceVF.getValue()));
        owner.objectChanged();
        owner.updateImage();
    }

    private void doCancel() {
        owner.setSelection(orSelection);
        owner.objectChanged();
        owner.updateImage();
        dispose();
    }

    private void doOK() {
        PolyMeshEditorWindow.setEdgeTol(toleranceVF.getValue());
        dispose();
    }

}
