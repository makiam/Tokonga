/* Copyright (C) 2001-2004 by Peter Eastman, 2005 by Francois Guillet
   Changes copyright (C) 2023-2026 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.ui.AXButton;
import artofillusion.ui.AXLabel;
import artofillusion.ui.AXText;
import artofillusion.ui.Translate;
import javax.swing.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


@Slf4j
class UnfoldStatusDialog extends JDialog {

    private JProgressBar progressBar;

    @Getter
    private AXText textArea;

    private AXButton proceedButton;

    private int status = 0;
    @Getter
    private boolean cancelled = false;

    private Thread unfoldThread;
    private final PolyMeshEditorWindow owner;

    public UnfoldStatusDialog(final PolyMeshEditorWindow owner) {
        super(owner.getComponent(), Translate.text("polymesh:meshUnfolding"), true);
        this.owner = owner;

        initComponents();

        proceedButton.addActionListener(e -> doProceedButton());

        // Close the dialog when Esc is pressed
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
        getRootPane().setDefaultButton(proceedButton);
        this.setLocationRelativeTo(owner.getComponent());

        setVisible(true);
    }

    private void initComponents() {
        var statusLabel = new AXLabel("polymesh:unfoldStatus");
        var scrollPane = new JScrollPane();
        progressBar = new JProgressBar();

        progressBar.setString("");
        progressBar.setEnabled(false);
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);


        textArea = new AXText();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        proceedButton = new AXButton("polymesh:proceed");
        proceedButton.setVisible(true);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                                        .addComponent(statusLabel)
                                        .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(proceedButton)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(statusLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(proceedButton)
                                .addContainerGap())
        );

    }

    private void doProceedButton() {
        switch (status) {
            case 0:
                proceedButton.setText(Translate.text("polymesh:abort"));
                progressBar.setString(Translate.text("polymesh:unfolding"));
                progressBar.setEnabled(true);
                progressBar.setVisible(true);
                pack();
                unfoldThread = new Thread(() -> owner.doUnfold(this));
                unfoldThread.start();
                status = 1;
                break;
            case 1:
                unfoldThread.stop();
                doCancel();
                break;
            case 2:
                dispose();
                break;
        }
    }

    private void doCancel() {
        cancelled = true;
        switch (status) {
            case 1:
            //cancel thread
            case 0:
            case 2:
                dispose();
                break;
        }
    }

    void unfoldFinished(boolean ok) {
        if (ok) {
            proceedButton.setText(Translate.text("polymesh:continue"));
            progressBar.setString("");
            progressBar.setEnabled(false);
            progressBar.setVisible(false);
            pack();
            status = 2;
        } else {
            cancelled = true;
        }
    }


}
