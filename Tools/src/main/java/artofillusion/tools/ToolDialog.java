/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.ArtOfIllusion;
import artofillusion.LayoutWindow;
import artofillusion.ui.Translate;
import buoy.widget.BButton;
import buoy.widget.BDialog;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
public class ToolDialog extends BDialog {

    @Getter private final BButton okButton;
    @Getter private final BButton cancelButton;

    public ToolDialog(LayoutWindow view, String title) {
        super(view, title, true);
        this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());

        okButton = Translate.button("ok", event -> commitImpl());
        cancelButton = Translate.button("cancel", event -> cancelImpl());

        this.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelImpl();
            }
        });
        this.getComponent().getRootPane().setDefaultButton(okButton.getComponent());

        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelImpl();
            }
        });
    }

    private void cancelImpl() {
        log.info("Cancelling {} tool dialog", this.getClass().getSimpleName());
        cancel();
        this.getComponent().dispose();
    }

    public void cancel() {
    }

    private void commitImpl() {
        log.info("Committing {} tool dialog", this.getClass().getSimpleName());
        commit();
        this.getComponent().dispose();
    }

    public void commit() {
    }
}
