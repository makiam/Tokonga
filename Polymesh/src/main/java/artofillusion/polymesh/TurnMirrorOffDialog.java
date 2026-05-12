/*
   Copyright (C) 2026 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.UndoableEdit;
import artofillusion.ui.Translate;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
public class TurnMirrorOffDialog extends JDialog {

    private final PolyMeshEditorWindow owner;
    private final PolyMesh mesh;

    public TurnMirrorOffDialog(PolyMeshEditorWindow owner) {
        super(owner.getComponent(), Translate.text("polymesh:removeMeshMirror"), true);


        this.owner = owner;
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
    }

    private void initComponents() {
        var keepButton = new JButton(Translate.text("polymesh:keep"));
        var dropButton = new JButton(Translate.text("polymesh:discard"));
        var cancelButton = new JButton(Translate.text("button.cancel"));

        var label = new JLabel(Translate.text("polymesh:keepMirroredMesh"));
        Icon questionIcon = UIManager.getIcon("OptionPane.questionIcon");
        label.setIcon(questionIcon);

        keepButton.addActionListener(e -> doKeep());
        dropButton.addActionListener(e -> doDiscard());
        cancelButton.addActionListener(e -> doCancel());

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(label)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(keepButton)
                                .addComponent(dropButton)
                                .addComponent(cancelButton))
        );

        // 4. Vertical Group (how components are arranged top-to-bottom)
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(label)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(keepButton)
                                .addComponent(dropButton)
                                .addComponent(cancelButton))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, keepButton, dropButton, cancelButton);
        getRootPane().setDefaultButton(keepButton);
    }

    private void doCancel() {
        dispose();
    }

    private void doKeep() {
        new KeepMirrorAction().execute();
        dispose();
    }

    private void doDiscard() {
        new DiscardMirrorAction().execute();
        dispose();
    }

    private void turnOffMirrors() {
        owner.mirrorItem[1].setState(false);
        owner.mirrorItem[2].setState(false);
        owner.mirrorItem[3].setState(false);
    }

    class KeepMirrorAction implements UndoableEdit {
        @Override
        public void undo() {
            //Undo Action not yet implemented
        }

        @Override
        public void redo() {
            mesh.copyObject(mesh.getMirroredMesh());
            TurnMirrorOffDialog.this.turnOffMirrors();
            owner.objectChanged();
            owner.updateImage();
        }

        @Override
        public String getName() {
            return "Keep mesh mirror";
        }
    }

    class DiscardMirrorAction implements UndoableEdit {

        @Override
        public void undo() {
            //Undo Action not yet implemented
        }

        @Override
        public void redo() {
            TurnMirrorOffDialog.this.turnOffMirrors();
            mesh.setMirrorState(PolyMesh.NO_MIRROR);
            owner.objectChanged();
            owner.setSelection(owner.getSelection());
        }

        @Override
        public String getName() {
            return "Discard mesh mirror";
        }
    }




}
