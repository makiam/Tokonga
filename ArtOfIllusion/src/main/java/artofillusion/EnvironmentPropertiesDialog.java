/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.math.CoordinateSystem;
import artofillusion.math.RGBColor;
import artofillusion.object.ObjectInfo;
import artofillusion.object.Sphere;
import artofillusion.ui.ColorSampleWidget;
import artofillusion.ui.ComponentsDialog;
import artofillusion.ui.Translate;
import artofillusion.ui.ValueField;
import buoy.widget.BButton;
import buoy.widget.BCheckBox;
import buoy.widget.BComboBox;
import buoy.widget.BLabel;
import buoy.widget.OverlayContainer;
import buoy.widget.RowContainer;
import buoy.widget.Widget;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author MaksK
 */
@Slf4j
public class EnvironmentPropertiesDialog {

    private final LayoutWindow owner;
    private final Scene scene;

    private final RGBColor ambColor;
    private final RGBColor envColor;
    private final RGBColor fogColor;

    private final ColorSampleWidget ambPatch, envPatch, fogPatch;

    private final OverlayContainer envPanel = new OverlayContainer();
    private final BComboBox envChoice = new BComboBox(new String[] {
      Translate.text("solidColor"),
      Translate.text("textureDiffuse"),
      Translate.text("textureEmissive")
    });

    private final BLabel envLabel = new BLabel();
    private final Sphere envSphere = new Sphere(1.0, 1.0, 1.0);
    private final ObjectInfo envInfo = new ObjectInfo(envSphere, new CoordinateSystem(), "Environment");

    private final BCheckBox fogBox;
    private final ValueField fogField;

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public EnvironmentPropertiesDialog(LayoutWindow owner) {
        this.owner = owner;
        this.scene = owner.getScene();

        this.ambColor = scene.getAmbientColor().duplicate();
        this.envColor = scene.getEnvironmentColor().duplicate();
        this.fogColor = scene.getFogColor().duplicate();

        ambPatch = new ColorSampleWidget(ambColor, Translate.text("ambientColor"));
        envPatch = new ColorSampleWidget(envColor, Translate.text("environmentColor"));
        fogPatch = new ColorSampleWidget(fogColor, Translate.text("fogColor"));



        fogBox = new BCheckBox("Environment Fog", scene.getFogState());
        fogField = new ValueField(scene.getFogDistance(), ValueField.POSITIVE);

        envChoice.setSelectedIndex(scene.getEnvironmentMode());
        envChoice.getComponent().addActionListener(this::onChoiceAction);

        final BButton envButton = new BButton(Translate.text("Choose") + ":");
        envButton.getComponent().addActionListener(this::onInvokeTextureDialog);


        RowContainer row = new RowContainer();
        row.add(envButton);
        row.add(envLabel);
        envPanel.add(envPatch, 0);
        envPanel.add(row, 1);
        envPanel.setVisibleChild(scene.getEnvironmentMode() == Scene.ENVIRON_SOLID ? 0 : 1);

        envInfo.setTexture(scene.getEnvironmentTexture(), scene.getEnvironmentMapping());
        envSphere.setParameterValues(scene.getEnvironmentParameterValues());
        envLabel.setText(envSphere.getTexture().getName());



    new ComponentsDialog(owner, Translate.text("environmentTitle"),
        new Widget [] {ambPatch, envChoice, envPanel, fogBox, fogPatch, fogField},
        new String [] {Translate.text("ambientColor"), Translate.text("environment"), "", "", Translate.text("fogColor"), Translate.text("fogDistance")},
            this::apply, this::cancel);
    }

    private void cancel() {
        log.info("Reverting environment settings");
    }

    private boolean isUnchanged() {
        if(envChoice.getSelectedIndex() != scene.getEnvironmentMode()) return false;
        if(!ambPatch.getColor().equals(scene.getAmbientColor())) return false;
        if(!fogPatch.getColor().equals(scene.getFogColor())) return false;
        if(!envPatch.getColor().equals(scene.getEnvironmentColor())) return false;
        if(fogBox.getState() != scene.getFogState()) return false;
        if(fogField.getValue() != scene.getFogDistance()) return false;
        if(!envSphere.getTexture().equals(scene.getEnvironmentTexture())) return false;
        if(!envSphere.getTextureMapping().equals(scene.getEnvironmentMapping())) return false;
        return Arrays.equals(envSphere.getParameterValues(), scene.getEnvironmentParameterValues());
    }

    private void apply() {
        if(isUnchanged()) {
            log.info("Settings remains unchanged...");
            return;
        }
        log.info("Applying environment parameters");
        UndoableEdit action = new UndoableEnvironmentEdit(this::commit, this::cancel).execute();
        owner.setUndoRecord(new UndoRecord(owner, false, UndoRecord.USER_DEFINED_ACTION, action));
    }

    private void commit() {


        scene.setAmbientColor(ambPatch.getColor());
        scene.setFogColor(fogPatch.getColor());
        scene.setEnvironmentColor(envPatch.getColor());

        scene.setFog(fogBox.getState(), fogField.getValue());
        scene.setEnvironmentMode(envChoice.getSelectedIndex());

        scene.setEnvironmentTexture(envSphere.getTexture());
        scene.setEnvironmentMapping(envSphere.getTextureMapping());
        scene.setEnvironmentParameterValues(envSphere.getParameterValues());
        owner.setModified();
    }

    private void onChoiceAction(ActionEvent event) {
        envPanel.setVisibleChild((envChoice.getSelectedIndex() == Scene.ENVIRON_SOLID ? 0 : 1));
        envPanel.getParent().layoutChildren();
    }

    private void textureSelectCallback() {
        envLabel.setText(envSphere.getTexture().getName());
        envPanel.getParent().layoutChildren();
    }

    private void onInvokeTextureDialog(ActionEvent event) {
        ObjectTextureDialog otd = new ObjectTextureDialog(owner, new ObjectInfo [] {envInfo}, true, false);
        otd.setCallback(this::textureSelectCallback);
    }

    static class UndoableEnvironmentEdit implements UndoableEdit {

        private Runnable redo;
        private Runnable undo;

        public UndoableEnvironmentEdit(Runnable redo, Runnable undo) {
            this.redo = redo;
            this.undo = undo;
        }

        @Override
        public void undo() {
            Optional.ofNullable(undo).ifPresent(action -> action.run());
        }

        @Override
        public void redo() {
            Optional.ofNullable(redo).ifPresent(action -> action.run());
        }

        @Override
        public String getName() {
            return Translate.text("Change Environment Properties");
        }


    }
}
