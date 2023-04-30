/* Copyright (C) 2001-2004 by Peter Eastman, 2005 by Francois Guillet
   Changes copyright (C) 2023 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.ui.Translate;
import artofillusion.ui.UIUtilities;
import artofillusion.ui.ValueField;
import buoy.event.CommandEvent;
import buoy.event.ValueChangedEvent;
import buoy.event.WindowClosingEvent;
import buoy.widget.BButton;
import buoy.widget.BDialog;
import buoy.widget.BLabel;
import buoy.widget.BProgressBar;
import buoy.widget.BTextArea;
import buoy.widget.BTextField;
import buoy.widget.BorderContainer;
import buoy.widget.RowContainer;
import buoy.xml.WidgetDecoder;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.UIManager;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author MaksK
 */
@Slf4j
class UnfoldStatusDialog extends BDialog {

    private BProgressBar progressBar;
    protected BTextArea textArea;
    private BButton proceedButton;
    private BButton advancedButton;
    private BLabel residualLabel;
    private RowContainer rowContainer1;
    private BTextField residualTF;
    private int status;
    protected boolean cancelled;
    protected double residual;
    private PMValueField residualVF;
    private Thread unfoldThread;
    private final PolyMeshEditorWindow owner;

    public UnfoldStatusDialog(final PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:meshUnfolding"), true);
        this.owner = owner;
        int nverts = ((PolyMesh) owner.getObject().getObject()).getVertices().length;
        if (nverts < 1000) {
            residual = 0.001;
        } else {
            residual = 1;
        }
        try (InputStream is = getClass().getResource("interfaces/unfoldStatus.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(is);
            BorderContainer borderContainer = (BorderContainer) decoder.getRootObject();
            BLabel unfoldStatusLabel = (BLabel) decoder.getObject("unfoldStatusLabel");
            unfoldStatusLabel.setText(Translate.text("polymesh:unfoldStatus"));
            progressBar = (BProgressBar) decoder.getObject("progressBar");
            textArea = (BTextArea) decoder.getObject("TextArea");
            rowContainer1 = (RowContainer) decoder.getObject("RowContainer1");
            proceedButton = (BButton) decoder.getObject("proceedButton");
            proceedButton.setText(Translate.text("polymesh:proceed"));
            advancedButton = (BButton) decoder.getObject("advancedButton");
            advancedButton.setText(Translate.text("polymesh:advanced"));
            advancedButton.addEventLink(CommandEvent.class, this, "doAdvancedButton");
            residualLabel = (BLabel) decoder.getObject("residualLabel");
            residualLabel.setText(Translate.text("polymesh:residualLabel"));
            setContent(borderContainer);
            proceedButton.addEventLink(CommandEvent.class, this, "doProceedButton");
            residualVF = new PMValueField(residual, ValueField.POSITIVE);
            residualVF.setTextField((BTextField) decoder.getObject("residualTF"));
            residualVF.setValue(residual);
            residualVF.addEventLink(ValueChangedEvent.class, this, "doResidualChanged");
            residualLabel.setVisible(false); //Invisible and never shown
            residualVF.setVisible(false); //Invisible and never shown
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating UnfoldStatusDialog due {}", ex.getLocalizedMessage());
        }
        textArea.getComponent().setFont(UIManager.getFont("TextField.font"));
        status = 0;
        cancelled = false;
        pack();
        addEventLink(WindowClosingEvent.class, this, "doCancel");
        UIUtilities.centerWindow(this);
        advancedButton.setVisible(false);
        progressBar.setProgressText("");
        progressBar.setEnabled(false);
        progressBar.setVisible(false);
        setVisible(true);
    }

    private void doAdvancedButton() {
        boolean showing = residualLabel.isVisible();
        residualLabel.setVisible(!showing);
        residualVF.setVisible(!showing);
        if (showing) {
            advancedButton.setText(Translate.text("polymesh:advanced"));
        } else {
            advancedButton.setText(Translate.text("polymesh:basic"));
        }
        rowContainer1.layoutChildren();
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

    private void doResidualChanged() {
        residual = residualVF.getValue();
    }

}
