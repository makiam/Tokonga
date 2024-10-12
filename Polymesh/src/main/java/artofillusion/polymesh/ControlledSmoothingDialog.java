/* Copyright (C) 2001-2004 by Peter Eastman, 2005 by Francois Guillet
   Changes copyright (C) 2023 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.UndoRecord;
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
import buoy.widget.BSlider;
import buoy.widget.BTextField;
import buoy.widget.ColumnContainer;
import buoy.xml.WidgetDecoder;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author MaksK
 */
@Slf4j
class ControlledSmoothingDialog extends BDialog {

    private final PolyMeshEditorWindow owner;
    private BCheckBox applyCB;
    private BLabel maxAngle;
    private BLabel minAngle;
    private BLabel angleRange;
    private BLabel smoothnessRange;
    private BLabel minSmoothness;
    private BLabel maxSmoothness;
    private PMValueField minAngleVF;
    private PMValueField maxAngleVF;
    private PMValueField minSmoothnessVF;
    private PMValueField maxSmoothnessVF;
    private final boolean backApply;
    private final double backMaxAngle;
    private final double backMinAngle;
    private final double backMinSmoothness;
    private final double backMaxSmoothness;
    private final PolyMesh mesh;
    private final PolyMesh prevMesh;
    private BSlider minAngleSlider;
    private BSlider maxAngleSlider;
    private BSlider minSmoothnessSlider;
    private BSlider maxSmoothnessSlider;

    public ControlledSmoothingDialog(PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:controlledSmoothness"), true);
        this.owner = owner;
        setTitle(Translate.text("polymesh:controlledSmoothnessDialogTitle"));
        mesh = (PolyMesh) owner.getObject().getObject();
        prevMesh = (PolyMesh) mesh.duplicate();
        backApply = mesh.isControlledSmoothing();
        backMinAngle = mesh.getMinAngle();
        backMaxAngle = mesh.getMaxAngle();
        backMinSmoothness = mesh.getMinSmoothness();
        backMaxSmoothness = mesh.getMaxSmoothness();
        try (InputStream is = getClass().getResource("interfaces/controlledSmoothing.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(is);
            ColumnContainer columnContainer = (ColumnContainer) decoder.getRootObject();
            BLabel controlledSmoothing = (BLabel) decoder.getObject("controlledSmoothing");
            controlledSmoothing.setText(Translate.text("polymesh:" + controlledSmoothing.getText()));
            applyCB = (BCheckBox) decoder.getObject("applyCB");
            applyCB.setText(Translate.text("polymesh:" + applyCB.getText()));
            applyCB.addEventLink(ValueChangedEvent.class, this, "doApplyCB");
            maxAngle = ((BLabel) decoder.getObject("maxAngle"));
            maxAngle.setText(Translate.text("polymesh:" + maxAngle.getText()));
            BTextField maxAngleValue = (BTextField) decoder.getObject("maxAngleValue");
            BTextField minAngleValue = (BTextField) decoder.getObject("minAngleValue");
            minAngle = (BLabel) decoder.getObject("minAngle");
            minAngle.setText(Translate.text("polymesh:" + minAngle.getText()));
            angleRange = (BLabel) decoder.getObject("angleRange");
            angleRange.setText(Translate.text("polymesh:" + angleRange.getText()));
            smoothnessRange = (BLabel) decoder.getObject("smoothnessRange");
            smoothnessRange.setText(Translate.text("polymesh:" + smoothnessRange.getText()));
            minSmoothness = (BLabel) decoder.getObject("minSmoothness");
            minSmoothness.setText(Translate.text("polymesh:" + minSmoothness.getText()));
            BTextField minSmoothnessValue = (BTextField) decoder.getObject("minSmoothnessValue");
            maxSmoothness = (BLabel) decoder.getObject("maxSmoothness");
            maxSmoothness.setText(Translate.text("polymesh:" + maxSmoothness.getText()));
            BTextField maxSmoothnessValue = (BTextField) decoder.getObject("maxSmoothnessValue");
            BButton okButton = (BButton) decoder.getObject("okButton");
            okButton.addEventLink(CommandEvent.class, this, "doOK");
            okButton.setText(Translate.text("button.ok"));
            BButton cancelButton = (BButton) decoder.getObject("cancelButton");
            cancelButton.addEventLink(CommandEvent.class, this, "doCancel");
            cancelButton.setText(Translate.text("button.cancel"));
            minAngleVF = new PMValueField(0.0, ValueField.NONNEGATIVE);
            minAngleVF.setTextField(minAngleValue);
            maxAngleVF = new PMValueField(180.0, ValueField.NONNEGATIVE);
            maxAngleVF.setTextField(maxAngleValue);
            minSmoothnessVF = new PMValueField(1.0, ValueField.NONNEGATIVE);
            minSmoothnessVF.setTextField(minSmoothnessValue);
            maxSmoothnessVF = new PMValueField(0.0, ValueField.NONNEGATIVE);
            maxSmoothnessVF.setTextField(maxSmoothnessValue);
            applyCB.setState(mesh.isControlledSmoothing());
            minAngleVF.setValue(mesh.getMinAngle());
            maxAngleVF.setValue(mesh.getMaxAngle());
            minSmoothnessVF.setValue(mesh.getMinSmoothness());
            maxSmoothnessVF.setValue(mesh.getMaxSmoothness());
            minAngleVF.addEventLink(ValueChangedEvent.class, this, "doApplyVF");
            maxAngleVF.addEventLink(ValueChangedEvent.class, this, "doApplyVF");
            minSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doApplyVF");
            maxSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doApplyVF");
            minAngleSlider = (BSlider) decoder.getObject("minAngleSlider");
            maxAngleSlider = (BSlider) decoder.getObject("maxAngleSlider");
            minSmoothnessSlider = (BSlider) decoder.getObject("minSmoothnessSlider");
            maxSmoothnessSlider = (BSlider) decoder.getObject("maxSmoothnessSlider");
            minAngleSlider.addEventLink(ValueChangedEvent.class, this, "doApplySL");
            maxAngleSlider.addEventLink(ValueChangedEvent.class, this, "doApplySL");
            minSmoothnessSlider.addEventLink(ValueChangedEvent.class, this, "doApplySL");
            maxSmoothnessSlider.addEventLink(ValueChangedEvent.class, this, "doApplySL");
            doApplyCB();
            setContent(columnContainer);
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating ControlledSmoothingDialog due {}", ex.getLocalizedMessage());
        }
        pack();
        addEventLink(WindowClosingEvent.class, this, "doCancel");
        UIUtilities.centerWindow(this);
    }

    private void doCancel() {
        mesh.setControlledSmoothing(backApply);
        mesh.setMinAngle(backMinAngle);
        mesh.setMaxAngle(backMaxAngle);
        mesh.setMinSmoothness((float) backMinSmoothness);
        mesh.setMaxSmoothness((float) backMaxSmoothness);
        owner.objectChanged();
        owner.updateImage();
        dispose();
    }

    private void doApplyCB() {
        boolean state = applyCB.getState();
        angleRange.setEnabled(state);
        smoothnessRange.setEnabled(state);
        minAngle.setEnabled(state);
        maxAngle.setEnabled(state);
        minAngleVF.setEnabled(state);
        maxAngleVF.setEnabled(state);
        minSmoothness.setEnabled(state);
        maxSmoothness.setEnabled(state);
        minSmoothnessVF.setEnabled(state);
        maxSmoothnessVF.setEnabled(state);
        minAngleSlider.setEnabled(state);
        maxAngleSlider.setEnabled(state);
        minSmoothnessSlider.setEnabled(state);
        maxSmoothnessSlider.setEnabled(state);
        doApplyVF();
    }

    private void doApplyVF() {
        boolean state = applyCB.getState();
        if (minAngleVF.getValue() > 180.0) {
            minAngleVF.setValue(180);
        }
        if (maxAngleVF.getValue() > 180.0) {
            maxAngleVF.setValue(180);
        }
        if (minSmoothnessVF.getValue() > 1.0) {
            minSmoothnessVF.setValue(1.0);
        }
        if (maxSmoothnessVF.getValue() > 1.0) {
            maxSmoothnessVF.setValue(1.0);
        }
        int val = (int) Math.round(minAngleVF.getValue() / 1.80);
        minAngleSlider.setValue(val);
        val = (int) Math.round(maxAngleVF.getValue() / 1.80);
        maxAngleSlider.setValue(val);
        val = (int) Math.round(minSmoothnessVF.getValue() * 100);
        minSmoothnessSlider.setValue(val);
        val = (int) Math.round(maxSmoothnessVF.getValue() * 100);
        maxSmoothnessSlider.setValue(val);
        mesh.setControlledSmoothing(state);
        mesh.setMinAngle(minAngleVF.getValue());
        mesh.setMaxAngle(maxAngleVF.getValue());
        mesh.setMinSmoothness((float) minSmoothnessVF.getValue());
        mesh.setMaxSmoothness((float) maxSmoothnessVF.getValue());
        owner.objectChanged();
        owner.updateImage();
    }

    private void doApplySL() {
        boolean state = applyCB.getState();
        mesh.setControlledSmoothing(state);
        double val = minAngleSlider.getValue() * 1.8;
        mesh.setMinAngle(val);
        minAngleVF.setValue(val);
        val = maxAngleSlider.getValue() * 1.8;
        mesh.setMaxAngle(val);
        maxAngleVF.setValue(val);
        val = ((float) minSmoothnessSlider.getValue()) / 100.0;
        mesh.setMinSmoothness((float) val);
        minSmoothnessVF.setValue(val);
        val = ((float) maxSmoothnessSlider.getValue()) / 100.0;
        mesh.setMaxSmoothness((float) val);
        maxSmoothnessVF.setValue(val);
        owner.objectChanged();
        owner.updateImage();
    }

    private void doOK() {
        doApplyVF();
        owner.setUndoRecord(new UndoRecord(owner, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        dispose();
    }

}
