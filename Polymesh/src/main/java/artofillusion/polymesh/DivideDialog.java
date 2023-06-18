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
import buoy.event.CommandEvent;
import buoy.widget.BButton;
import buoy.widget.BDialog;
import buoy.widget.BLabel;
import buoy.widget.BSpinner;
import buoy.widget.BorderContainer;
import buoy.xml.WidgetDecoder;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * A dialog to enter the number of segments when subdividing edges
 *
 * @author Francois Guillet
 */
@Slf4j
public class DivideDialog extends BDialog {

    private BSpinner divideSpinner;
    private BButton okButton;
    private BButton cancelButton;
    private int num = -1;

    /**
     * Constructor for the DivideDialog object
     */
    public DivideDialog(final PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:subdivideEdgesTitle"), true);
        try (InputStream is = getClass().getResource("interfaces/divide.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(is);
            setContent((BorderContainer) decoder.getRootObject());
            divideSpinner = ((BSpinner) decoder.getObject("divideSpinner"));
            BLabel divideLabel = (BLabel) decoder.getObject("divideLabel");
            divideLabel.setText(Translate.text("polymesh:" + divideLabel.getText()));
            okButton = ((BButton) decoder.getObject("okButton"));
            okButton.setText(Translate.text("polymesh:ok"));
            cancelButton = ((BButton) decoder.getObject("cancelButton"));
            cancelButton.setText(Translate.text("polymesh:cancel"));
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating DivideDialog due {}", ex.getLocalizedMessage());
        }
        okButton.addEventLink(CommandEvent.class, this, "doOK");
        cancelButton.addEventLink(CommandEvent.class, this, "doCancel");
        pack();
        UIUtilities.centerWindow(this);
        setVisible(true);
    }

    /**
     * OK button selected
     */
    private void doOK() {
        num = ((Integer) divideSpinner.getValue());
        dispose();
    }

    /**
     * Cancel button selected
     */
    private void doCancel() {
        dispose();
        num = -1;
    }

    /**
     * Returns spinner valueWidget.getValue() if the user clicked on the OK
     * button else return -1.
     *
     * @return number of segments
     */
    public int getNumber() {
        return num;
    }

}
