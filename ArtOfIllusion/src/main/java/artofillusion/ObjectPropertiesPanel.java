/* Copyright (C) 2006-2009 by Peter Eastman
   Changes copyright (C) 2017-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.material.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a panel which displays information about the currently selected objects, and allows them
 * to be edited.
 */
@Slf4j
public class ObjectPropertiesPanel extends ColumnContainer {

    public static final LayoutInfo CENTER_LAYOUT = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(2, 2, 2, 2), null);
    public static final LayoutInfo FILL_LAYOUT = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(2, 2, 2, 2), null);

    private final List<Material> materials = PluginRegistry.getPlugins(Material.class);
    private final List<String> matTypes = new ArrayList<>();

    private final List<Texture> textures = PluginRegistry.getPlugins(Texture.class);
    private final List<String> texTypes = new ArrayList<>();

    private final LayoutWindow window;

    private final BTextField nameField = new BTextField();

    private final ValueField xPosField = new ValueField(0.0);
    private final ValueField yPosField = new ValueField(0.0);
    private final ValueField zPosField = new ValueField(0.0);
    private final ValueField xRotField = new ValueField(0.0);
    private final ValueField yRotField = new ValueField(0.0);
    private final ValueField zRotField = new ValueField(0.0);

    private final BComboBox textureChoice = new BComboBox();
    private final BComboBox materialChoice = new BComboBox();

    //private final AWTWidget materialAppender;
    private PropertyEditor[] propEditor;
    private List<ObjectInfo> objects;
    private Property[] properties;
    private Object3D[] previousObjects;
    private boolean ignoreNextChange;
    private Widget<?> lastEventSource;
    private final ActionProcessor paramChangeProcessor = new ActionProcessor();

    public ObjectPropertiesPanel(LayoutWindow window) {
        this.window = window;

        materials.forEach(mat -> matTypes.add(Translate.text("newMaterialOfType", mat.getTypeName())));
        textures.forEach(tex -> texTypes.add(Translate.text("newTextureOfType", tex.getTypeName())));

        //materialAppender = new AWTWidget(new MenuButton(AppIcon.INSTANCE.getAppIcon()));

        rebuildContents();
        window.addEventLink(SceneChangedEvent.class, this, "rebuildContents");
        nameField.addEventLink(FocusLostEvent.class, this, "nameChanged");
        nameField.addEventLink(KeyPressedEvent.class, this, "nameChanged");
        xPosField.addEventLink(ValueChangedEvent.class, this, "coordinatesChanged");
        yPosField.addEventLink(ValueChangedEvent.class, this, "coordinatesChanged");
        zPosField.addEventLink(ValueChangedEvent.class, this, "coordinatesChanged");
        xRotField.addEventLink(ValueChangedEvent.class, this, "coordinatesChanged");
        yRotField.addEventLink(ValueChangedEvent.class, this, "coordinatesChanged");
        zRotField.addEventLink(ValueChangedEvent.class, this, "coordinatesChanged");

        textureChoice.addEventLink(ValueChangedEvent.class, this, "textureChanged");
        materialChoice.addEventLink(ValueChangedEvent.class, this, "materialChanged");

        materialChoice.getComponent().addActionListener(this::materialSelected);
        textureChoice.getComponent().addActionListener(this::textureSelected);

        ListChangeListener listener = new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
                rebuildContents();
            }

            @Override
            public void itemRemoved(int index, Object obj) {
                rebuildContents();
            }

            @Override
            public void itemChanged(int index, Object obj) {
                rebuildContents();
            }
        };
        window.getScene().addTextureListener(listener);
        window.getScene().addMaterialListener(listener);
    }

    /**
     * Rebuild the contents of the panel.
     */
    protected void rebuildContents() {
        log.info("RC");
        if (ignoreNextChange) {
            ignoreNextChange = false;
            return;
        }
        if (lastEventSource == nameField) {
            nameChanged(new FocusLostEvent(nameField, false)); // Commit name changes.
        }
        // Find the selected objects.

        objects = window.getSelectedObjects();

        boolean objectsChanged = previousObjects == null || objects.size() != previousObjects.length;
        for (int i = 0; i < objects.size() && !objectsChanged; i++) {
            objectsChanged |= (objects.get(i).getObject() != previousObjects[i]);
        }

        if (objectsChanged) {
            previousObjects = new Object3D[objects.size()];
            for (int i = 0; i < objects.size(); i++) {
                previousObjects[i] = objects.get(i).getObject();
            }
        }

        // If nothing is selected, just place a message in the panel.
        if (objects.isEmpty()) {
            if (objectsChanged) {
                removeAll();
                add(Translate.label("noObjectsSelected"));
                UIUtilities.applyDefaultBackground(this);
                UIUtilities.applyDefaultFont(this);
                if (getParent() != null) {
                    getParent().layoutChildren();
                }
                repaint();
            }
            return;
        }

        // Set the name.
        var head = objects.get(0);
        if (objects.size() == 1) {
            nameField.setText(head.getName());
        }

        // Set the position and orientation.
        Vec3 origin = head.getCoords().getOrigin();
        xPosField.setValue(origin.x);
        yPosField.setValue(origin.y);
        zPosField.setValue(origin.z);
        double[] angles = head.getCoords().getRotationAngles();
        xRotField.setValue(angles[0]);
        yRotField.setValue(angles[1]);
        zRotField.setValue(angles[2]);
        for (int i = 1; i < objects.size(); i++) {    //Starting 1'st position!!!
            origin = objects.get(i).getCoords().getOrigin();
            checkFieldValue(xPosField, origin.x);
            checkFieldValue(yPosField, origin.y);
            checkFieldValue(zPosField, origin.z);
            angles = objects.get(i).getCoords().getRotationAngles();
            checkFieldValue(xRotField, angles[0]);
            checkFieldValue(yRotField, angles[1]);
            checkFieldValue(zRotField, angles[2]);
        }

        // Set the texture.
        Texture tex = head.getObject().getTexture();
        boolean canSetTexture = head.getObject().canSetTexture();
        boolean sameTexture = true;
        for (int i = 1; i < objects.size(); i++) { //Starting 1'st position!!!
            Texture thisTex = objects.get(i).getObject().getTexture();
            if (thisTex != tex) {
                sameTexture = false;
            }
            canSetTexture &= objects.get(i).getObject().canSetTexture();
        }
        Scene scene = window.getScene();
        if (canSetTexture) {
            Vector<String> names = new Vector<>();
            int selected = -1;
            for (int i = 0; i < scene.getNumTextures(); i++) {
                names.add(scene.getTexture(i).getName());
                if (scene.getTexture(i) == tex) {
                    selected = i;
                }
            }
            if (sameTexture) {
                if (tex instanceof LayeredTexture) {
                    selected = names.size();
                    names.add(Translate.text("layeredTexture"));
                }
            } else {
                selected = names.size();
                names.add("");
            }

            names.addAll(texTypes);

            textureChoice.setModel(new DefaultComboBoxModel<>(names));
            textureChoice.setSelectedIndex(selected);
        }

        // Set the material.
        Material mat = head.getObject().getMaterial(); // Get first selection item material

        boolean canSetMaterial = head.getObject().canSetMaterial();
        boolean sameMaterial = true;
        for (int i = 1; i < objects.size(); i++) { //Starting 1'st position!!!
            Material thisMat = objects.get(i).getObject().getMaterial();
            if (thisMat != mat) {
                sameMaterial = false;
            }
            canSetMaterial &= objects.get(i).getObject().canSetMaterial();
        }
        if (canSetMaterial) {
            Vector<String> names = new Vector<>();
            int selected = -1;
            for (int i = 0; i < scene.getNumMaterials(); i++) {
                names.add(scene.getMaterial(i).getName());
                if (scene.getMaterial(i) == mat) {
                    selected = i;
                }
            }
            if (sameMaterial) {
                if (mat == null) {
                    selected = names.size();
                }
            } else {
                selected = names.size();
                names.add("");
            }
            names.add(Translate.text("none"));
            names.addAll(matTypes);

            materialChoice.setModel(new DefaultComboBoxModel<>(names));
            materialChoice.setSelectedIndex(selected);

        }

        // See whether the list of properties has changed.
        Property[] oldProperties = properties;
        findProperties();
        boolean propertiesChanged = oldProperties == null || properties.length != oldProperties.length;
        for (int i = 0; i < properties.length && !propertiesChanged; i++) {
            propertiesChanged = !properties[i].equals(oldProperties[i]);
        }

        // Rebuild the panel contents.
        if (!objectsChanged && !propertiesChanged) {
            showParameterValues();
            return;
        }
        lastEventSource = null;
        removeAll();

        if (objects.size() == 1) {
            add(Translate.label("Name"));
            add(nameField, FILL_LAYOUT);
        }
        add(Translate.label("Position"));
        FormContainer positions = new FormContainer(new double[]{0, 1, 0, 1, 0, 1}, new double[1]);
        positions.add(new BLabel("X"), 0, 0);
        positions.add(xPosField, 1, 0, FILL_LAYOUT);
        positions.add(new BLabel(" Y"), 2, 0);
        positions.add(yPosField, 3, 0, FILL_LAYOUT);
        positions.add(new BLabel(" Z"), 4, 0);
        positions.add(zPosField, 5, 0, FILL_LAYOUT);
        add(positions, FILL_LAYOUT);
        add(Translate.label("Orientation"));
        FormContainer orientation = new FormContainer(new double[]{0, 1, 0, 1, 0, 1}, new double[1]);
        orientation.add(new BLabel("X"), 0, 0);
        orientation.add(xRotField, 1, 0, FILL_LAYOUT);
        orientation.add(new BLabel(" Y"), 2, 0);
        orientation.add(yRotField, 3, 0, FILL_LAYOUT);
        orientation.add(new BLabel(" Z"), 4, 0);
        orientation.add(zRotField, 5, 0, FILL_LAYOUT);
        add(orientation, FILL_LAYOUT);
        if (canSetTexture) {
            add(Translate.label("Texture"));
            add(textureChoice, FILL_LAYOUT);

        }
        if (canSetMaterial) {
            add(Translate.label("Material"));
            add(materialChoice, FILL_LAYOUT);
            //add(materialAppender, fillLayout);
        }

        // Build widgets for object parameters.
        propEditor = new PropertyEditor[properties.length];
        for (int i = 0; i < propEditor.length; i++) {
            propEditor[i] = new PropertyEditor(properties[i], null);
            if (propEditor[i].getLabel() != null) {
                add(new BLabel(propEditor[i].getLabel()));
            }
            var widget = propEditor[i].getWidget();
            widget.addEventLink(ValueChangedEvent.class, this, "parameterChanged");
            if (widget instanceof ValueSelector || widget instanceof BCheckBox || widget instanceof ValueField) {
                add(widget, CENTER_LAYOUT);
            } else {
                add(widget, FILL_LAYOUT);
            }
        }
        showParameterValues();

        // Layout and display the panel.
        UIUtilities.applyDefaultBackground(this);
        UIUtilities.applyDefaultFont(this);
        if (getParent() != null) {
            getParent().layoutChildren();
        }
        repaint();
        log.info("RC: DONE");
    }

    private void checkFieldValue(ValueField field, double value) {
        if (field.getValue() != value) field.setValue(Double.NaN);
    }

    /**
     * Collect the list of object properties to display in the Properties panel from the list of selected objects
     */
    private void findProperties() {
        properties = objects.get(0).getObject().getProperties();

        for (int i = 1; i < objects.size(); i++) { //Starting 1'st position!!!
            Property[] otherProperty = objects.get(i).getObject().getProperties();
            boolean same = properties.length == otherProperty.length;

            for (int j = 0; j < properties.length && same; j++) {
                if (properties[j].equals(otherProperty[j])) continue;
                same = false;
            }

            if (same) continue;
            properties = new Property[0];
            return;
        }
    }

    /**
     * Update all the ValueSelectors to show the current values of object parameters.
     */
    private void showParameterValues() {
        if (propEditor == null) {
            return;
        }
        Object[] values = new Object[propEditor.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = objects.get(0).getObject().getPropertyValue(i);
        }
        for (int i = 1; i < objects.size(); i++) {
            for (int j = 0; j < values.length; j++) {
                if (values[j] != null && !values[j].equals(objects.get(i).getObject().getPropertyValue(j))) {
                    values[j] = null;
                }
            }
        }
        for (int i = 0; i < propEditor.length; i++) {
            propEditor[i].setValue(values[i]);
        }
    }

    /**
     * This is called when the value in any of the position or orientation fields is changed.
     */
    @SuppressWarnings("unused")
    private void coordinatesChanged(ValueChangedEvent ev) {
        UndoRecord undo = null;
        if (ev.getWidget() != lastEventSource) {
            ignoreNextChange = true;
            undo = new UndoRecord(window);
            for (ObjectInfo object : objects) {
                undo.addCommand(UndoRecord.COPY_COORDS, object.getCoords(), object.getCoords().duplicate());
            }
        }
        for (ObjectInfo info: objects) {
            CoordinateSystem coords = info.getCoords();
            Vec3 origin = coords.getOrigin();
            origin.x = getNewValue(origin.x, xPosField.getValue());
            origin.y = getNewValue(origin.y, yPosField.getValue());
            origin.z = getNewValue(origin.z, zPosField.getValue());
            coords.setOrigin(origin);
            double[] angles = coords.getRotationAngles();
            angles[0] = getNewValue(angles[0], xRotField.getValue());
            angles[1] = getNewValue(angles[1], yRotField.getValue());
            angles[2] = getNewValue(angles[2], zRotField.getValue());
            coords.setOrientation(angles[0], angles[1], angles[2]);
        }

        window.getScene().applyTracksAfterModification(objects);
        lastEventSource = ev.getWidget();
        if (undo == null) {
            window.setModified();
        } else {
            window.setUndoRecord(undo);
        }
        window.updateImage();
    }

    private double getNewValue(double oldValue, double newValue) {
        return Double.isNaN(newValue) ? oldValue : newValue;
    }

    /**
     * This is called when an event occurs that might result in the name being changed.
     */
    private void nameChanged(WidgetEvent ev) {
        lastEventSource = ev.getWidget();
        if (ev instanceof KeyPressedEvent event && event.getKeyCode() != KeyEvent.VK_ENTER) {
            return;
        }
        if (objects.isEmpty() || objects.get(0).getName().equals(nameField.getText())) {
            return;
        }
        int which = window.getScene().indexOf(objects.get(0));
        UndoableEdit edit = new ObjectRenameEdit(window, which, nameField.getText()).execute();
        window.setUndoRecord(new UndoRecord(window, false, edit));

        if (ev instanceof KeyPressedEvent) {
            window.getView().requestFocus(); // This is where they'll probably expect it to go
        }
    }

    /**
     * This is called when the texture is changed.
     */
    @SuppressWarnings("unused")
    private void textureChanged() {

        int index = textureChoice.getSelectedIndex();
        Scene scene = window.getScene();
        Texture tex = null;
        if (index < scene.getNumTextures()) {
            tex = scene.getTexture(index);
        } else {

            if (index < scene.getNumTextures() + textures.size()) {
                try {
                    tex = textures.get(index - scene.getNumTextures()).duplicate();
                    int j = 0;
                    String name;
                    do {
                        j++;
                        name = "Untitled " + tex.getTypeName() + " Texture " + j;
                    } while (scene.getTexture(name) != null);
                    tex.setName(name);
                    scene.addTexture(tex);
                    tex.edit(window, scene);
                } catch (SecurityException ex) {
                    log.atError().setCause(ex).log("Error changing texture: {}", ex.getMessage());
                }
            }
        }
        if (tex != null) {
            UndoRecord undo = new UndoRecord(window);
            for (ObjectInfo object : objects) {
                if (object.getObject().getTexture() != tex) {
                    undo.addCommand(UndoRecord.COPY_OBJECT, object.getObject(), object.getObject().duplicate());
                    object.setTexture(tex, tex.getDefaultMapping(object.getObject()));
                }
            }
            window.setUndoRecord(undo);
            window.updateImage();
            window.getScore().tracksModified(false);
        }
    }

    /**
     * This is called when the material is changed.
     */
    @SuppressWarnings("unused")
    private void materialChanged() {

        int index = materialChoice.getSelectedIndex();
        Scene scene = window.getScene();
        Material mat = null;
        boolean noMaterial = index == scene.getNumMaterials();
        if (index < scene.getNumMaterials()) {
            mat = scene.getMaterial(index);
        } else if (index > scene.getNumMaterials()) {

            try {
                mat = materials.get(index - scene.getNumMaterials() - 1).duplicate();
                int j = 0;
                String name = "";
                do {
                    j++;
                    name = "Untitled " + mat.getTypeName() + " Material " + j;
                } while (scene.getMaterial(name) != null);
                mat.setName(name);
                scene.addMaterial(mat);
                mat.edit(window, scene);
            } catch (SecurityException ex) {
                log.atError().setCause(ex).log("Error changing material: {}", ex.getMessage());
            }
        }
        if (noMaterial || mat != null) {
            UndoRecord undo = new UndoRecord(window);
            for (ObjectInfo object: objects) {
                if (object.getObject().getMaterial() != mat) {
                    undo.addCommand(UndoRecord.COPY_OBJECT, object.getObject(), object.getObject().duplicate());
                    object.setMaterial(mat, noMaterial ? null : mat.getDefaultMapping(object.getObject()));
                }
            }
            window.setUndoRecord(undo);
            window.updateImage();
            window.getScore().tracksModified(false);
        }
    }

    /**
     * This is called when an object parameter is changed.
     */
    @SuppressWarnings("unused")
    private void parameterChanged(final ValueChangedEvent ev) {
        Runnable r = () -> processParameterChange(ev);
        if (ev.isInProgress()) {
            paramChangeProcessor.addEvent(r);
        } else {
            EventQueue.invokeLater(r); // Ensure that it won't be discarded.
        }
    }

    private void processParameterChange(ValueChangedEvent ev) {
        UndoRecord undo = null;
        if (ev.getWidget() != lastEventSource) {
            ignoreNextChange = true;
            undo = new UndoRecord(window);
            for (ObjectInfo object : objects) {
                undo.addCommand(UndoRecord.COPY_OBJECT, object.getObject(), object.getObject().duplicate());
            }
        }
        boolean changed = false;
        for(var item: objects) {
            for(int j = 0; j < propEditor.length; j++) {
                if(propEditor[j].getWidget() == ev.getSource()) {
                    Object value = propEditor[j].getValue();
                    if(!item.getObject().getPropertyValue(j).equals(value)) {
                        item.getObject().setPropertyValue(j, value);
                        changed = true;
                    }
                }
            }
            window.getScene().objectModified(item.getObject());
        }
        lastEventSource = ev.getWidget();
        if (undo != null && changed) {
            window.setUndoRecord(undo);
        } else if (!ev.isInProgress() && changed) {
            window.setModified();
        }
        window.updateImage();
        window.getScore().tracksModified(false);
    }

    /**
     * Always use a reasonable size, regardless of the current selection.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        return new Dimension(Math.max(pref.width, 180), Math.max(pref.height, 200));
    }

    /**
     * Allow the panel to be completely hidden.
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension();
    }

    private void materialSelected(ActionEvent event) {
        // TBD: On Pure Swing Migration
    }

    private void textureSelected(ActionEvent event) {
        // TBD: On Pure Swing Migration
    }
}
