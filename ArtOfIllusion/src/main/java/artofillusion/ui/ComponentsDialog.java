/* Copyright (C) 1999-2011 by Peter Eastman
   Changes copyright (C) 2023-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import artofillusion.ArtOfIllusion;
import buoy.event.*;
import buoy.widget.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.text.html.Option;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

/**
 * A ComponentsDialog is a modal dialog containing a line of text, and one or more Widgets
 * for the user to edit. Each Widget has a label next to it. At the bottom are two
 * buttons labeled OK and Cancel.
 */
@Slf4j
public class ComponentsDialog extends BDialog {

    private final Widget[] comp;
    private boolean ok;
    private Runnable okCallback;
    private Runnable cancelCallback;
    private final BButton okButton;

    /**
     * Create a modal dialog containing a set of labeled components.
     *
     * @param parent the parent of the dialog
     * @param prompt a text string to appear at the top of the dialog
     * @param components the list of components to display
     * @param labels the list of labels for each component
     */
    public ComponentsDialog(WindowWidget parent, String prompt, Widget[] components, String[] labels) {
        this(parent, prompt, components, labels, null, null);
    }

    /**
     * Create a non-modal dialog containing a set of labeled components.
     *
     * @param parent the parent of the dialog
     * @param prompt a text string to appear at the top of the dialog
     * @param components the list of components to display
     * @param labels the list of labels for each component
     * @param onOK a callback to execute when the user clicks OK
     * @param onCancel a callback to execute when the user clicks Cancel
     */
    public ComponentsDialog(WindowWidget parent, String prompt, Widget[] components, String[] labels, Runnable onOK, Runnable onCancel) {
        super(parent, (onOK == null && onCancel == null));

        this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());
        comp = components;
        this.okCallback = onOK;
        this.cancelCallback = onCancel;

        BorderContainer content = new BorderContainer();
        setContent(content);
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(10, 10, 10, 10), null));
        content.add(new BLabel(prompt), BorderContainer.NORTH);

        // Add the Widgets.
        FormContainer center = new FormContainer(new double[]{0.0, 1.0}, new double[components.length]);
        content.add(center, BorderContainer.CENTER);
        for (int i = 0; i < components.length; i++) {
            if (labels[i] == null) {
                center.add(components[i], 0, i, 2, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(2, 0, 2, 0), null));
            } else {
                center.add(new BLabel(labels[i]), 0, i, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 0, 2, 5), null));
                center.add(components[i], 1, i, new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH, new Insets(2, 0, 2, 0), null));
            }
        }

        // Add the buttons at the bottom.
        RowContainer buttons = new RowContainer();
        content.add(buttons, BorderContainer.SOUTH);
        buttons.add(okButton = Translate.button("ok", event -> buttonOK()));
        buttons.add(Translate.button("cancel", event -> buttonCancel()));

        String cancelName = "cancel";
        InputMap inputMap = this.getComponent().getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = this.getComponent().getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonCancel();
            }
        });


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

    /**
     * Set whether the OK button is enabled.
     */
    public void setOkEnabled(boolean enabled) {
        okButton.setEnabled(enabled);
    }

    private void buttonOK() {
        ok = true;
        Optional.ofNullable(okCallback).ifPresent(action -> action.run());
        dispose();
    }

    private void buttonCancel() {
        ok = false;
        Optional.ofNullable(cancelCallback).ifPresent(action -> action.run());
        dispose();
    }

}
