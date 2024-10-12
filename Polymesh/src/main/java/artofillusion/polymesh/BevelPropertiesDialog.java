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
import buoy.event.WindowClosingEvent;
import buoy.widget.BButton;
import buoy.widget.BCheckBox;
import buoy.widget.BDialog;
import buoy.widget.BLabel;
import buoy.widget.BTextField;
import buoy.widget.BorderContainer;
import buoy.xml.WidgetDecoder;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * A dialog for bevel properties selection
 *
 * @author Francois Guillet
 */
@Slf4j
class BevelPropertiesDialog extends BDialog {

    private BorderContainer borderContainer1;
    private PMValueField areaLimitFieldVF;
    private BCheckBox applyCB;
    private BButton okButton;
    private BButton cancelButton;

    /**
     * Constructor for the Bevel Properties dialog
     */
    public BevelPropertiesDialog(final PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:bevelPropertiesTitle"), true);
        try (InputStream is = getClass().getResource("interfaces/bevelArea.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(is);
            borderContainer1 = (BorderContainer) decoder.getRootObject();
            BLabel areaLimit = (BLabel) decoder.getObject("areaLimit");
            areaLimit.setText(Translate.text("polymesh:" + areaLimit.getText()));
            areaLimitFieldVF = new PMValueField(PolyMesh.edgeLengthLimit, ValueField.NONE);
            areaLimitFieldVF.setTextField((BTextField) decoder.getObject("areaLimitField"));
            applyCB = ((BCheckBox) decoder.getObject("applyCB"));
            applyCB.setText(Translate.text("polymesh:" + applyCB.getText()));
            applyCB.setState(PolyMesh.applyEdgeLengthLimit);
            BLabel bevelAreaLabel = (BLabel) decoder.getObject("bevelAreaLabel");
            bevelAreaLabel.setText(Translate.text("polymesh:" + bevelAreaLabel.getText()));
            okButton = ((BButton) decoder.getObject("okButton"));
            okButton.setText(Translate.text("button.ok"));
            cancelButton = ((BButton) decoder.getObject("cancelButton"));
            cancelButton.setText(Translate.text("button.cancel"));
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating BevelPropertiesDialog due {}", ex.getLocalizedMessage());
        }
        setContent(borderContainer1);
        okButton.addEventLink(CommandEvent.class, this, "doOK");
        cancelButton.addEventLink(CommandEvent.class, this, "doCancel");
        addEventLink(WindowClosingEvent.class, this, "doCancel");
        pack();
        UIUtilities.centerWindow(this);
    }

    /**
     * OK button selected
     */
    private void doOK() {
        PolyMesh.applyEdgeLengthLimit = applyCB.getState();
        if (PolyMesh.applyEdgeLengthLimit) {
            PolyMesh.edgeLengthLimit = areaLimitFieldVF.getValue();
        }
        dispose();
    }

    /**
     * Cancel button selected
     */
    private void doCancel() {
        dispose();
    }

}
