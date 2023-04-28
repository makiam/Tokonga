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
import buoy.widget.BTextField;
import buoy.widget.BorderContainer;
import buoy.xml.WidgetDecoder;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * A dialog presenting options to find similar edges
 */
@Slf4j
class FindSimilarEdgesDialog extends BDialog {

    private final boolean[] orSelection;
    private BorderContainer borderContainer;
    private BButton okButton;
    private BButton cancelButton;
    private PMValueField toleranceVF;
    private final PolyMeshEditorWindow owner;
    private PolyMesh mesh;

    public FindSimilarEdgesDialog(PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:similarEdgesTitle"), true);

        this.owner = owner;
        this.orSelection = owner.getSelection();
        this.mesh = (PolyMesh) owner.getObject().getObject();

        try (InputStream is = getClass().getResource("interfaces/similaredges.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(is);
            borderContainer = (BorderContainer) decoder.getRootObject();
            BLabel tolerance1 = (BLabel) decoder.getObject("tolerance1");
            tolerance1.setText(Translate.text("polymesh:" + tolerance1.getText()));
            okButton = ((BButton) decoder.getObject("okButton"));
            cancelButton = ((BButton) decoder.getObject("cancelButton"));
            BTextField toleranceTF = (BTextField) decoder.getObject("toleranceTF");
            toleranceVF = new PMValueField(PolyMeshEditorWindow.getEdgeTol(), ValueField.NONE);
            toleranceVF.setTextField((BTextField) decoder.getObject("toleranceTF"));
            okButton = ((BButton) decoder.getObject("okButton"));
            cancelButton = ((BButton) decoder.getObject("cancelButton"));
            okButton.setText(Translate.text("polymesh:ok"));
            cancelButton.setText(Translate.text("polymesh:cancel"));
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating FindSimilarEdgesDialog due{}", ex.getLocalizedMessage());
        }
        setContent(borderContainer);
        toleranceVF.addEventLink(ValueChangedEvent.class, this, "doTolValueChanged");
        okButton.addEventLink(CommandEvent.class, this, "doOK");
        cancelButton.addEventLink(CommandEvent.class, this, "doCancel");
        addEventLink(WindowClosingEvent.class, this, "doCancel");
        pack();
        UIUtilities.centerWindow(this);
        doTolValueChanged();
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
