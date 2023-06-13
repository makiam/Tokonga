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
import artofillusion.ui.ValueField;
import buoy.event.CommandEvent;
import buoy.event.ValueChangedEvent;
import buoy.event.WindowClosingEvent;
import buoy.widget.BButton;
import buoy.widget.BDialog;
import buoy.widget.BLabel;
import buoy.widget.BSlider;
import buoy.widget.BTextField;
import buoy.widget.BorderContainer;
import buoy.xml.WidgetDecoder;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author MaksK
 */
@Slf4j
class EdgeSmoothnessRangeDialog extends BDialog {

    private PMValueField minSmoothnessVF;
    private BSlider minSmoothnessSlider;
    private PMValueField maxSmoothnessVF;
    private BSlider maxSmoothnessSlider;
    private final boolean[] orSel;
    private final boolean[] newSel;
    private final PolyMeshEditorWindow owner;

    public EdgeSmoothnessRangeDialog(PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:smoothnessRange"), true);

        this.owner = owner;
        int sl = owner.getSelection().length;
        orSel = new boolean[sl];
        newSel = new boolean[sl];
        System.arraycopy(owner.getSelection(), 0, orSel, 0, sl);
        owner.setSelection(newSel);
        try (InputStream inputStream = getClass().getResource("interfaces/smoothnessRange.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(inputStream);
            BorderContainer borderContainer = (BorderContainer) decoder.getRootObject();
            BLabel label = (BLabel) decoder.getObject("Label1");
            label.setText(Translate.text("polymesh:specifySmoothnessRange"));
            label = (BLabel) decoder.getObject("Label2");
            label.setText(Translate.text("polymesh:minSmoothness"));
            label = (BLabel) decoder.getObject("Label3");
            label.setText(Translate.text("polymesh:maxSmoothness"));
            BTextField minSmoothnessTF = ((BTextField) decoder.getObject("minSmoothnessTF"));
            minSmoothnessSlider = ((BSlider) decoder.getObject("minSmoothnessSlider"));
            BTextField maxSmoothnessTF = ((BTextField) decoder.getObject("maxSmoothnessTF"));
            maxSmoothnessSlider = ((BSlider) decoder.getObject("maxSmoothnessSlider"));
            minSmoothnessSlider.addEventLink(ValueChangedEvent.class, this, "doMinSliderChanged");
            maxSmoothnessSlider.addEventLink(ValueChangedEvent.class, this, "doMaxSliderChanged");
            BButton addButton = (BButton) decoder.getObject("addButton");
            addButton.setText(Translate.text("polymesh:addToSelection"));
            addButton.addEventLink(CommandEvent.class, this, "doAdd");
            BButton setButton = (BButton) decoder.getObject("setButton");
            setButton.addEventLink(CommandEvent.class, this, "doSet");
            setButton.setText(Translate.text("polymesh:setSelection"));
            BButton cancelButton = (BButton) decoder.getObject("cancelButton");
            cancelButton.addEventLink(CommandEvent.class, this, "doCancel");
            cancelButton.setText(Translate.text("polymesh:cancel"));
            minSmoothnessVF = new PMValueField(0.0, ValueField.NONNEGATIVE);
            minSmoothnessVF.setTextField(minSmoothnessTF);
            minSmoothnessVF.setValue(0.0);
            maxSmoothnessVF = new PMValueField(1.0, ValueField.POSITIVE);
            maxSmoothnessVF.setTextField(maxSmoothnessTF);
            maxSmoothnessVF.setValue(1.0);
            maxSmoothnessSlider.setValue(100);
            minSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doValuesChanged");
            maxSmoothnessVF.addEventLink(ValueChangedEvent.class, this, "doValuesChanged");
            setContent(borderContainer);
            addEventLink(WindowClosingEvent.class, this, "doCancel");
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating EdgeSmoothnessRangeDialog due {}", ex.getLocalizedMessage());
        }
        updateSelection();
        pack();
    }

    private void doMinSliderChanged() {
        minSmoothnessVF.setValue(((Integer) minSmoothnessSlider.getValue()) / 100.0);
        updateSelection();
    }

    private void doMaxSliderChanged() {
        maxSmoothnessVF.setValue(((Integer) maxSmoothnessSlider.getValue()) / 100.0);
        updateSelection();
    }

    private void doValuesChanged() {
        updateSelection();
    }

    private void updateSelection() {
        PolyMesh mesh = (PolyMesh) owner.getObject().getObject();
        PolyMesh.Wedge[] edges = mesh.getEdges();
        double min = minSmoothnessVF.getValue();
        double max = maxSmoothnessVF.getValue();
        for (int i = 0; i < newSel.length; i++) {
            newSel[i] = (edges[i].smoothness >= min) & (edges[i].smoothness <= max);
        }
        owner.setSelection(newSel);
    }

    private void doAdd() {
        for (int i = 0; i < orSel.length; i++) {
            orSel[i] |= newSel[i];
        }
        owner.setSelection(orSel);
        dispose();
    }

    private void doSet() {
        owner.setSelection(newSel);
        dispose();
    }

    private void doCancel() {
        owner.setSelection(orSel);
        dispose();
    }

}
