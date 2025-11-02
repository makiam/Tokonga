/* Copyright (C) 2000-2006 by Peter Eastman
   Changes copyright (C) 2023-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import artofillusion.ArtOfIllusion;

import buoy.widget.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



/**
 * A PanelDialog is a modal dialog containing a line of text at the top, and a single
 * Widget (usually a container with other Widgets). At the bottom are two buttons labeled OK
 * and Cancel.
 */
public class PanelDialog extends BDialog {

    private boolean ok;

    /**
     * Create a modal dialog containing a panel.
     *
     * @param parent the parent of the dialog
     * @param prompt a text string to appear at the top of the dialog (prompt may be null)
     * @param thePanel the panel to display
     */
    public PanelDialog(WindowWidget parent, String prompt, Widget thePanel) {
        super(parent, true);

        this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());

        BorderContainer content = new BorderContainer();
        setContent(content);
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(10, 10, 10, 10), null));
        if (prompt != null) {
            content.add(new BLabel(prompt), BorderContainer.NORTH);
        }
        content.add(thePanel, BorderContainer.CENTER);

        // Add the buttons at the bottom.
        RowContainer buttons = new RowContainer();
        content.add(buttons, BorderContainer.SOUTH);
        var okButton = Translate.button("ok", event -> buttonOK());
        buttons.add(okButton);
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
        pack();
        setResizable(false);
        UIUtilities.centerDialog(this, parent);
        setVisible(true);
    }

    /**
     * Return true if the user clicked OK, false if they clicked Cancel.
     */
    public boolean clickedOk() {
        return ok;
    }

    private void buttonOK() {
        ok = true;
        dispose();
    }

    private void buttonCancel() {
        ok = false;
        dispose();
    }


}
