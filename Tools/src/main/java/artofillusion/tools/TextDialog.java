/* Copyright (C) 2013 by Peter Eastman
   Changes copyright (C) 2022-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import java.awt.*;
import java.util.*;

/**
 * TextDialog displays the user interface for creating text.
 */
public class TextDialog extends ToolDialog {

    private final LayoutWindow window;
    private final BTextField text;
    private final BComboBox typeChoice;
    private final BList fontsList;
    private final BCheckBox boldBox, italicBox;
    private final ValueField thicknessValue;
    private final ObjectPreviewCanvas preview;

    private ArrayList<ObjectInfo> objects;

    /**
     * Create a new TextDialog.
     *
     * @param window the window the objects will be added to
     */
    public TextDialog(LayoutWindow window) {
        super(window, Translate.text("Text"));

        this.window = window;

        // Create the controls.
        text = new BTextField("Text");
        typeChoice = new BComboBox(new Object[]{Translate.text("Outline"), Translate.text("Tubes"), Translate.text("Surface"), Translate.text("Solid")});
        typeChoice.setSelectedIndex(2);
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontsList = new BList(fonts);
        fontsList.setPreferredVisibleRows(10);
        fontsList.setMultipleSelectionEnabled(false);
        int selectedFontIndex = 0;
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals("Serif")) {
                selectedFontIndex = i;
            }
        }
        fontsList.setSelected(selectedFontIndex, true);
        boldBox = new BCheckBox(Translate.text("Bold"), false);
        italicBox = new BCheckBox(Translate.text("Italic"), false);
        thicknessValue = new ValueField(0.1, ValueField.POSITIVE);
        createObjects();
        preview = new ObjectPreviewCanvas(new ObjectInfo(new TextCollection(), new CoordinateSystem(), ""));

        // Set up a listener to rebuild the objects when any control changes.
        final ActionProcessor actionProcessor = new ActionProcessor();
        Object listener = new Object() {
            void processEvent() {
                actionProcessor.addEvent(() -> createObjects());
            }
        };
        boldBox.addEventLink(ValueChangedEvent.class, listener);
        italicBox.addEventLink(ValueChangedEvent.class, listener);
        fontsList.addEventLink(SelectionChangedEvent.class, listener);
        text.addEventLink(ValueChangedEvent.class, listener);
        typeChoice.addEventLink(ValueChangedEvent.class, listener);
        thicknessValue.addEventLink(ValueChangedEvent.class, listener);

        // Layout the window.
        FormContainer content = new FormContainer(new double[]{0, 1}, new double[]{0, 0, 0, 1, 0, 0, 0});
        setContent(content);
        content.add(text, 0, 0, 2, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL));
        RowContainer typeRow = new RowContainer();
        typeRow.add(Translate.label("objectType"));
        typeRow.add(typeChoice);
        content.add(typeRow, 0, 1);
        content.add(Translate.label("Font"), 0, 2);
        content.add(UIUtilities.createScrollingList(fontsList), 0, 3, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH));
        RowContainer styleRow = new RowContainer();
        styleRow.add(boldBox);
        styleRow.add(italicBox);
        content.add(styleRow, 0, 4);
        RowContainer thicknessRow = new RowContainer();
        thicknessRow.add(Translate.label("Thickness"));
        thicknessRow.add(thicknessValue);
        content.add(thicknessRow, 0, 5);
        content.add(preview, 1, 1, 1, 5, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH));
        RowContainer buttons = new RowContainer();

        buttons.add(getOkButton());
        buttons.add(getCancelButton());


        content.add(buttons, 0, 6, 2, 1);
        pack();
        UIUtilities.centerDialog(this, window);
        updateComponents();
        fontsList.scrollToItem(selectedFontIndex);

        updatePreview();
        setVisible(true);
    }

    /**
     * Enable or disable components, based on the current selections.
     */
    private void updateComponents() {
        int typeIndex = typeChoice.getSelectedIndex();
        thicknessValue.setEnabled(typeIndex == 1 || typeIndex == 3);
        getOkButton().setEnabled(text.getText().length() > 0);
    }

    /**
     * Create the text.
     */
    private void createObjects() {
        TextTool.TextType[] types = new TextTool.TextType[]{TextTool.TextType.Outline, TextTool.TextType.Tube, TextTool.TextType.Surface, TextTool.TextType.Solid};
        TextTool.TextType type = types[typeChoice.getSelectedIndex()];
        objects = TextTool.createText(text.getText(), fontsList.getSelectedValue().toString(), type, boldBox.getState(), italicBox.getState(), thicknessValue.getValue(), window.getScene().getDefaultTexture());
        if (preview != null) {
            updatePreview();
            updateComponents();
        }
    }

    /**
     * Recenter and redraw the preview.
     */
    private void updatePreview() {
        preview.setObject(new TextCollection());
        preview.objectChanged();
        BoundingBox bounds = preview.getObject().getBounds();
        bounds.outset((bounds.maxx - bounds.minx) / 10);
        preview.frameBox(bounds);
        preview.repaint();
    }

    @Override
    public void commit() {
        if (objects.isEmpty()) return;

        UndoRecord undo = new UndoRecord(window);
        if (objects.get(0).getObject() instanceof TriangleMesh) {
            // Convert the whole string to a single mesh.

            TriangleMesh mesh = new TextCollection().convertToTriangleMesh(1);
            mesh.setSmoothingMethod(Mesh.APPROXIMATING);
            mesh.setTexture(window.getScene().getDefaultTexture(), window.getScene().getDefaultTexture().getDefaultMapping(mesh));
            window.addObject(new ObjectInfo(mesh, new CoordinateSystem(), text.getText()), undo);
            window.setSelection(window.getScene().getNumObjects() - 1);
        } else {
            // Create a Null object, then add the curves or tubes as children of it.

            ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), text.getText());
            window.clearSelection();
            window.addObject(parent, undo);
            window.addToSelection(window.getScene().getNumObjects() - 1);
            for (ObjectInfo obj : objects) {
                window.addObject(obj, undo);
                parent.addChild(obj, parent.getChildren().length);
                window.addToSelection(window.getScene().getNumObjects() - 1);
            }
            window.rebuildItemList();
        }
        window.setUndoRecord(undo);
        window.updateImage();
    }

    /**
     * This inner class is used to display the text in the preview canvas.
     */
    private class TextCollection extends ObjectCollection {

        @Override
        protected Enumeration<ObjectInfo> enumerateObjects(ObjectInfo info, boolean interactive, Scene scene) {
            return Collections.enumeration(objects);
        }

        @Override
        public TextCollection duplicate() {
            return new TextCollection();
        }

        @Override
        public void copyObject(Object3D obj) {
        }

        @Override
        public void setSize(double xsize, double ysize, double zsize) {
        }

        @Override
        public Keyframe getPoseKeyframe() {
            return null;
        }

        @Override
        public void applyPoseKeyframe(Keyframe k) {
        }
    }
}
