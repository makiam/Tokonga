/* Copyright (C) 2001-2004 by Peter Eastman, 2005 by Francois Guillet
   Changes copyright (C) 2023-2025 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.ui.Translate;
import artofillusion.ui.UIUtilities;
import buoy.event.CommandEvent;
import buoy.widget.BButton;
import buoy.widget.BDialog;
import buoy.widget.BLabel;
import buoy.widget.BProgressBar;
import buoy.widget.BTextArea;
import buoy.widget.BorderContainer;
import buoy.xml.WidgetDecoder;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author MaksK
 */
@Slf4j
class UnfoldStatusDialog extends BDialog {

    private BProgressBar progressBar;
    private BTextArea textArea;
    
    public JTextArea getTextArea() {
        return textArea.getComponent();
    }
    private BButton proceedButton;
    
    private int status;
    protected boolean cancelled;


    private Thread unfoldThread;
    private final PolyMeshEditorWindow owner;

    public UnfoldStatusDialog(final PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:meshUnfolding"), true);
        this.owner = owner;

        try (InputStream is = getClass().getResource("interfaces/unfoldStatus.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(is);
            BorderContainer borderContainer = (BorderContainer) decoder.getRootObject();
            BLabel unfoldStatusLabel = (BLabel) decoder.getObject("unfoldStatusLabel");
            unfoldStatusLabel.setText(Translate.text("polymesh:unfoldStatus"));
            progressBar = (BProgressBar) decoder.getObject("progressBar");
            textArea = (BTextArea) decoder.getObject("TextArea");

            proceedButton = (BButton) decoder.getObject("proceedButton");
            proceedButton.setText(Translate.text("polymesh:proceed"));


            setContent(borderContainer);
            proceedButton.addEventLink(CommandEvent.class, this, "doProceedButton");

        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating UnfoldStatusDialog due {}", ex.getLocalizedMessage());
        }
        textArea.getComponent().setFont(UIManager.getFont("TextField.font"));
        status = 0;
        cancelled = false;
        pack();
        this.getComponent().addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                doCancel();
            }
        });
        UIUtilities.centerWindow(this);

        progressBar.setProgressText("");
        progressBar.setEnabled(false);
        progressBar.setVisible(false);
        setVisible(true);
    }

    private void doProceedButton() {
        switch (status) {
            case 0:
                proceedButton.setText(Translate.text("polymesh:abort"));
                progressBar.setProgressText(Translate.text("polymesh:unfolding"));
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
            progressBar.setProgressText("");
            progressBar.setEnabled(false);
            progressBar.setVisible(false);
            pack();
            status = 2;
        } else {
            cancelled = true;
        }
    }

}
