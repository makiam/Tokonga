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
import artofillusion.ui.ValueField;
import buoy.event.CommandEvent;
import buoy.event.ValueChangedEvent;
import buoy.event.WindowClosingEvent;
import buoy.widget.BButton;
import buoy.widget.BCheckBox;
import buoy.widget.BDialog;
import buoy.widget.BLabel;
import buoy.widget.BTextField;
import buoy.widget.BorderContainer;
import buoy.xml.WidgetDecoder;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * A dialog presenting options to find similar faces
 */
@Slf4j
class FindSimilarFacesDialog extends BDialog {

    private final boolean[] orSelection;

    private BCheckBox normalCB;
    private BCheckBox looseShapeCB;
    private BCheckBox strictShapeCB;
    private BLabel tolerance1;
    private BLabel tolerance2;
    private BLabel tolerance3;
    private BButton okButton;
    private BButton cancelButton;
    private PMValueField normalCBVF;
    private PMValueField looseShapeCBVF;
    private PMValueField strictShapeCBVF;
    private final PolyMeshEditorWindow owner;
    private final PolyMesh mesh;

    public FindSimilarFacesDialog(PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:similarFacesTitle"), true);
        this.owner = owner;
        this.mesh = (PolyMesh) owner.getObject().getObject();
        this.orSelection = owner.getSelection();
        
        BorderContainer container = null;
        try (InputStream is = getClass().getResource("interfaces/similar.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(is);
            container = (BorderContainer) decoder.getRootObject();
            BLabel titleTextLabel = (BLabel) decoder.getObject("titleTextLabel");
            titleTextLabel.setText(Translate.text("polymesh:" + titleTextLabel.getText()));
            normalCB = ((BCheckBox) decoder.getObject("normalCB"));
            normalCB.setText(Translate.text("polymesh:" + normalCB.getText()));
            looseShapeCB = ((BCheckBox) decoder.getObject("looseShapeCB"));
            looseShapeCB.setText(Translate.text("polymesh:" + looseShapeCB.getText()));
            strictShapeCB = ((BCheckBox) decoder.getObject("strictShapeCB"));
            strictShapeCB.setText(Translate.text("polymesh:" + strictShapeCB.getText()));
            tolerance1 = ((BLabel) decoder.getObject("tolerance1"));
            tolerance2 = ((BLabel) decoder.getObject("tolerance2"));
            tolerance3 = ((BLabel) decoder.getObject("tolerance3"));
            tolerance1.setText(Translate.text("polymesh:" + tolerance1.getText()));
            tolerance2.setText(Translate.text("polymesh:" + tolerance2.getText()));
            tolerance3.setText(Translate.text("polymesh:" + tolerance3.getText()));

            normalCBVF = new PMValueField(PolyMeshEditorWindow.getNormalTol(), ValueField.NONE);
            normalCBVF.setTextField((BTextField) decoder.getObject("normalCBTF"));
            looseShapeCBVF = new PMValueField(PolyMeshEditorWindow.getLooseShapeTol(), ValueField.NONE);
            looseShapeCBVF.setTextField((BTextField) decoder.getObject("looseShapeCBTF"));
            strictShapeCBVF = new PMValueField(PolyMeshEditorWindow.getStrictShapeTol(), ValueField.NONE);
            strictShapeCBVF.setTextField((BTextField) decoder.getObject("strictShapeCBTF"));
            
            okButton = ((BButton) decoder.getObject("okButton"));
            cancelButton = ((BButton) decoder.getObject("cancelButton"));
            okButton.setText(Translate.text("button.ok"));
            cancelButton.setText(Translate.text("button.cancel"));
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating FindSimilarFacesDialog due {}", ex.getLocalizedMessage());
        }
        setContent(container);
        normalCBVF.addEventLink(ValueChangedEvent.class, this, "doTolValueChanged");
        strictShapeCBVF.addEventLink(ValueChangedEvent.class, this, "doTolValueChanged");
        looseShapeCBVF.addEventLink(ValueChangedEvent.class, this, "doTolValueChanged");
        normalCB.addEventLink(ValueChangedEvent.class, this, "doCBValueChanged");
        strictShapeCB.addEventLink(ValueChangedEvent.class, this, "doCBValueChanged");
        looseShapeCB.addEventLink(ValueChangedEvent.class, this, "doCBValueChanged");
        okButton.addEventLink(CommandEvent.class, this, "doOK");
        cancelButton.addEventLink(CommandEvent.class, this, "doCancel");

        okButton.setEnabled(false);

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener action = e -> doCancel();
        this.getComponent().getRootPane().registerKeyboardAction(action, escape, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.getComponent().addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                doCancel();
            }
        });

        pack();
        UIUtilities.centerWindow(this);
    }

    private void doTolValueChanged() {
        fetchTolValues();
        double normalTol = PolyMeshEditorWindow.getNormalTol();
        double looseShapeTol = PolyMeshEditorWindow.getLooseShapeTol();
        double strictShapeTol = PolyMeshEditorWindow.getStrictShapeTol();
        owner.setSelection(mesh.findSimilarFaces(orSelection, isNormal(), normalTol, isLoose(), looseShapeTol, isStrict(), strictShapeTol));
        owner.objectChanged();
        owner.updateImage();
    }

    private void doCBValueChanged() {
        tolerance1.setEnabled(normalCB.getState());
        normalCBVF.setEnabled(normalCB.getState());
        tolerance2.setEnabled(looseShapeCB.getState());
        looseShapeCBVF.setEnabled(looseShapeCB.getState());
        tolerance3.setEnabled(strictShapeCB.getState());
        strictShapeCBVF.setEnabled(strictShapeCB.getState());
        doTolValueChanged();
        if (normalCB.getState() || looseShapeCB.getState() || strictShapeCB.getState()) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }

    private void doCancel() {
        owner.setSelection(orSelection);
        owner.objectChanged();
        owner.updateImage();
        dispose();
    }

    private void doOK() {
        fetchTolValues();
        dispose();
    }

    private void fetchTolValues() {
        if (normalCB.getState()) {
            log.info("Normal: {}", normalCBVF.getValue());
            PolyMeshEditorWindow.setNormalTol(normalCBVF.getValue());
        }
        if (looseShapeCB.getState()) {
            PolyMeshEditorWindow.setLooseShapeTol(looseShapeCBVF.getValue());
        }
        if (strictShapeCB.getState()) {
            PolyMeshEditorWindow.setStrictShapeTol(strictShapeCBVF.getValue());
        }
    }

    public boolean isNormal() {
        return normalCB.getState();
    }

    public boolean isLoose() {
        return looseShapeCB.getState();
    }

    public boolean isStrict() {
        return strictShapeCB.getState();
    }

}
