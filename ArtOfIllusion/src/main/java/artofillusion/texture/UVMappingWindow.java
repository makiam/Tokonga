/* Copyright (C) 2002-2012 by Peter Eastman
   Changes copyright (C) 2023-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.texture;

import artofillusion.*;
import artofillusion.animation.JointEditorDialog;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

/**
 * UVMappingWindow is a window for editing the UV texture coordinates at
 * each vertex of a mesh.
 */
public class UVMappingWindow extends BDialog implements MeshEditController, EditingWindow {

    private final ObjectInfo objInfo;
    private final Object3D oldObj;
    private final Mesh editObj;
    private final UVMapping map;
    private Vec2[] coord;
    private final UVMappingViewer mapView;
    private final MeshViewer meshView;
    private final ToolPalette tools;
    private final MaterialPreviewer preview;
    private final ValueField minuField;
    private final ValueField minvField;
    private final ValueField maxuField;
    private final ValueField maxvField;
    private final ValueField uField;
    private final ValueField vField;
    private final BComboBox componentChoice;
    private final BComboBox resChoice;
    private final BCheckBox faceBox;
    private boolean[] selectedVertices;
    private int selectMode;
    boolean[] selected;
    int[] selectionDistance;

    private static int resolution;

    public UVMappingWindow(BDialog parent, Object3D obj, UVMapping map) {
        super(parent, Translate.text("uvCoordsTitle"), true);
        oldObj = obj;
        Object3D meshObj = oldObj;
        while (meshObj instanceof ObjectWrapper) {
            meshObj = ((ObjectWrapper) meshObj).getWrappedObject();
        }
        editObj = (Mesh) meshObj.duplicate();
        this.map = map;
        objInfo = new ObjectInfo((Object3D) editObj, new CoordinateSystem(), "");
        findTextureVertices();
        selected = new boolean[editObj.getVertices().length];
        selectedVertices = new boolean[0];

        // Find the range of coordinates displayed.
        double minU = Double.MAX_VALUE;
        double maxU = -Double.MAX_VALUE;
        double minV = Double.MAX_VALUE;
        double maxV = -Double.MAX_VALUE;
        for (Vec2 vec2 : coord) {
            if (vec2.x < minU) {
                minU = vec2.x;
            }
            if (vec2.x > maxU) {
                maxU = vec2.x;
            }
            if (vec2.y < minV) {
                minV = vec2.y;
            }
            if (vec2.y > maxV) {
                maxV = vec2.y;
            }
        }
        double padU = 0.1 * (maxU - minU);
        double padV = 0.1 * (maxV - minV);
        minU -= padU;
        maxU += padU;
        minV -= padV;
        maxV += padV;

        // Determine the texture.
        Texture tex = obj.getTexture();
        if (tex instanceof LayeredTexture) {
            LayeredMapping layered = (LayeredMapping) obj.getTextureMapping();
            for (int i = 0; i < layered.getNumLayers(); i++) {
                if (layered.getLayerMapping(i) == map) {
                    tex = layered.getLayer(i);
                    break;
                }
            }
        }

        // Record the default parameter values.
        TextureParameter[] param = map.getParameters();
        double[] paramVal = null;
        if (param != null) {
            paramVal = new double[param.length];
            for (int i = 0; i < param.length; i++) {
                paramVal[i] = param[i].defaultVal;
            }
        }

        // Layout the three main canvases.
        FormContainer content = new FormContainer(new double[]{1.0, 1.0}, new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
        setContent(content);
        BorderContainer mapViewPanel = new BorderContainer();
        mapViewPanel.add(mapView = new UVMappingViewer((Texture2D) tex, this, minU, maxU, minV, maxV, 0, 1 << (2 - resolution), 0.0, paramVal), BorderContainer.CENTER);
        mapView.setPreferredSize(new Dimension(200, 200));
        tools = new ToolPalette(6, 1);
        EditingTool defaultTool;
        EditingTool metaTool;
        mapViewPanel.add(tools, BorderContainer.NORTH);
        tools.setBackground(getBackground());
        tools.addTool(defaultTool = new ReshapeMeshTool(this, mapView.getController()));
        tools.addTool(new ScaleMeshTool(this, mapView.getController()));
        tools.addTool(new RotateMeshTool(this, mapView.getController(), true));
        tools.addTool(new SkewMeshTool(this, mapView.getController()));
        tools.addTool(new TaperMeshTool(this, mapView.getController()));
        tools.addTool(metaTool = new MoveUVViewTool(this));
        mapView.setTool(defaultTool);
        mapView.setMetaTool(metaTool);
        meshView = editObj.createMeshViewer(this, new RowContainer());
        meshView.setMeshVisible(true);
        meshView.setPreferredSize(new Dimension(150, 150));
        meshView.setTool(new GenericTool(this, "movePoints", Translate.text("reshapeMeshTool.tipText")));
        meshView.setMetaTool(new MoveViewTool(this));
        meshView.setAltTool(new RotateViewTool(this));
        meshView.setScrollTool(new ScrollViewTool(this));
        BSplitPane meshViewPanel = new BSplitPane(BSplitPane.VERTICAL, meshView, preview = new MaterialPreviewer(objInfo, 150, 150));
        meshViewPanel.setResizeWeight(0.7);
        meshViewPanel.setContinuousLayout(true);
        BSplitPane div = new BSplitPane(BSplitPane.HORIZONTAL, mapViewPanel, meshViewPanel);
        div.setResizeWeight(0.5);
        div.setContinuousLayout(true);
        content.add(div, 0, 0, 2, 1);

        // Layout the text fields.
        RowContainer row;
        content.add(row = new RowContainer(), 0, 1);
        row.add(Translate.label("displayedComponent"));
        row.add(componentChoice = new BComboBox(new String[]{
            Translate.text("Diffuse"),
            Translate.text("Specular"),
            Translate.text("Transparent"),
            Translate.text("Hilight"),
            Translate.text("Emissive")
        }));
        componentChoice.addEventLink(ValueChangedEvent.class, this, "rebuildImage");
        content.add(Translate.label("selectedVertexCoords"), 0, 2);
        content.add(row = new RowContainer(), 0, 3);
        row.add(new BLabel("U:"));
        row.add(uField = new ValueField(Double.NaN, ValueField.NONE, 5));
        row.add(new BLabel(" V:"));
        row.add(vField = new ValueField(Double.NaN, ValueField.NONE, 5));
        faceBox = new BCheckBox(Translate.text("mapFacesIndependently"), false);
        if (editObj instanceof FacetedMesh) {
            faceBox.setState(map.isPerFaceVertex((FacetedMesh) editObj));
            if (faceBox.getState()) {
                selectMode = FACE_MODE;
                selected = new boolean[((FacetedMesh) editObj).getFaceCount()];
            }
            content.add(faceBox, 0, 4);
        }
        faceBox.addEventLink(ValueChangedEvent.class, this, "faceModeChanged");
        content.add(row = new RowContainer(), 1, 1);
        row.add(new BLabel(Translate.text("Resolution") + ":"));
        row.add(resChoice = new BComboBox(new String[]{
            Translate.text("Low"),
            Translate.text("Medium"),
            Translate.text("High")
        }));
        resChoice.setSelectedIndex(resolution);
        resChoice.addEventLink(ValueChangedEvent.class, this, "rebuildImage");
        content.add(Translate.label("displayedCoordRange"), 1, 2);
        content.add(row = new RowContainer(), 1, 3);
        row.add(new BLabel("U:"));
        row.add(minuField = new ValueField(minU, ValueField.NONE, 5));
        row.add(Translate.label("to"), new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(0, 5, 0, 5), null));
        row.add(maxuField = new ValueField(maxU, ValueField.NONE, 5));
        content.add(row = new RowContainer(), 1, 4);
        row.add(new BLabel("V:"));
        row.add(minvField = new ValueField(minV, ValueField.NONE, 5));
        row.add(Translate.label("to"), new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(0, 5, 0, 5), null));
        row.add(maxvField = new ValueField(maxV, ValueField.NONE, 5));
        minuField.addEventLink(ValueChangedEvent.class, this, "rebuildImage");
        minvField.addEventLink(ValueChangedEvent.class, this, "rebuildImage");
        maxuField.addEventLink(ValueChangedEvent.class, this, "rebuildImage");
        maxvField.addEventLink(ValueChangedEvent.class, this, "rebuildImage");
        uField.addEventLink(ValueChangedEvent.class, this, "coordsChanged");
        vField.addEventLink(ValueChangedEvent.class, this, "coordsChanged");

        // Layout the buttons at the bottom.
        content.add(row = new RowContainer(), 0, 5, 2, 1, new LayoutInfo());
        row.add(Translate.button("ok", event -> doOk()));
        row.add(Translate.button("cancel", event -> dispose()));
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener action = e -> dispose();
        this.getComponent().getRootPane().registerKeyboardAction(action, escape, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                UVMappingWindow.this.dispose();
            }
        });
        pack();
        UIUtilities.centerDialog(this, parent);
        setVisible(true);
    }

    /**
     * Get the object being edited in this window.
     */
    @Override
    public ObjectInfo getObject() {
        return objInfo;
    }

    /**
     * Set the mesh being edited.
     */
    @Override
    public void setMesh(Mesh mesh) {
        objInfo.setObject((Object3D) mesh);
        objInfo.clearCachedMeshes();
    }

    /**
     * Get the current selection mode.
     */
    @Override
    public int getSelectionMode() {
        return selectMode;
    }

    /**
     * Set the current selection mode.
     */
    @Override
    public void setSelectionMode(int mode) {
        selectMode = mode;
    }

    /**
     * Get an array of flags specifying which parts of the object are selected.
     */
    @Override
    public boolean[] getSelection() {
        return selected;
    }

    /**
     * Set an array of flags specifying which parts of the object are selected.
     */
    @Override
    public void setSelection(boolean[] selected) {
        this.selected = selected;
        meshView.repaint();
    }

    /**
     * Selection distance is simply 0 if the vertex is selected, and -1 otherwise.
     */
    @Override
    public int[] getSelectionDistance() {
        if (selectionDistance == null) {
            findSelectionDistance();
        }
        return selectionDistance;
    }

    /**
     * Selection distance is simply 0 if the vertex is selected, and -1 otherwise.
     */
    private void findSelectionDistance() {
        selectionDistance = new int[selected.length];
        for (int i = 0; i < selected.length; i++) {
            selectionDistance[i] = (selected[i] ? 0 : -1);
        }
    }

    @Override
    public double getMeshTension() {
        return 1.0;
    }

    @Override
    public int getTensionDistance() {
        return 0;
    }

    /**
     * Determine the list of texture vertices for the mesh.
     */
    private void findTextureVertices() {
        boolean isPerFace = false;
        if (editObj instanceof FacetedMesh) {
            isPerFace = map.isPerFaceVertex((FacetedMesh) editObj);
        }
        if (isPerFace) {
            FacetedMesh mesh = (FacetedMesh) editObj;

            List<Vec2> coordList = new ArrayList<>();
            for (Vec2[] vec2s : map.findFaceTextureCoordinates(mesh)) {
                for (int j = 0; j < vec2s.length; j++) {
                    coordList.add(vec2s[j]);
                }
            }
            coord = coordList.toArray(new Vec2[coordList.size()]);
        } else {
            coord = map.findTextureCoordinates(editObj);
        }
    }

    /**
     * Determine which texture vertices are selected in the mesh viewer. Return true if the
     * selection has changed since this was last called, false if it has not.
     */
    private boolean findSelectedVertices() {
        boolean[] newSelection;
        if (faceBox.getState()) {
            int faces = ((FacetedMesh) editObj).getFaceCount();
            newSelection = new boolean[faces * 3];
            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    newSelection[3 * i] = newSelection[3 * i + 1] = newSelection[3 * i + 2] = true;
                }
            }
        } else {
            newSelection = new boolean[selected.length];
            for (int i = 0; i < selected.length; i++) {
                newSelection[i] = selected[i];
            }
        }
        boolean changed = (selectedVertices.length != newSelection.length);
        for (int i = 0; i < newSelection.length && !changed; i++) {
            if (selectedVertices[i] != newSelection[i]) {
                changed = true;
            }
        }
        selectedVertices = newSelection;
        return changed;
    }

    /**
     * This is called whenever the mesh has changed.
     */
    @Override
    public void objectChanged() {
        mapView.objectChanged();
    }

    /**
     * Regenerate the texture image based on the current settings.
     */
    private void rebuildImage() {
        resolution = resChoice.getSelectedIndex();
        int res = 1 << (2 - resolution);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mapView.setParameters(minuField.getValue(), maxuField.getValue(), minvField.getValue(),
                maxvField.getValue(), componentChoice.getSelectedIndex(), res);
        setCursor(Cursor.getDefaultCursor());
    }

    private void doOk() {
        if (faceBox.getState()) {
            int faces = ((FacetedMesh) editObj).getFaceCount();
            Vec2[][] texCoord = new Vec2[faces][];
            int index = 0;
            for (int i = 0; i < faces; i++) {
                texCoord[i] = new Vec2[((FacetedMesh) editObj).getFaceVertexCount(i)];
                for (int j = 0; j < texCoord[i].length; j++) {
                    texCoord[i][j] = coord[index++];
                }
            }
            map.setFaceTextureCoordinates(oldObj, texCoord);
        } else {
            map.setTextureCoordinates(oldObj, coord);
        }
        dispose();
    }

    /**
     * Respond to selections.
     */
    private void faceModeChanged() {
        FacetedMesh mesh = (FacetedMesh) editObj;
        if (faceBox.getState()) {
            // Convert from per-vertex to per-face-vertex mapping.

            List<Vec2> coordList = new ArrayList<>();
            for (int i = 0; i < mesh.getFaceCount(); i++) {
                for (int j = 0; j < mesh.getFaceVertexCount(i); j++) {
                    coordList.add(new Vec2(coord[mesh.getFaceVertexIndex(i, j)]));
                }
            }
            setTextureCoords(coordList.toArray(new Vec2[coordList.size()]));
            selected = new boolean[mesh.getFaceCount()];
        } else {
            // Convert from per-face-vertex to per-vertex mapping.

            boolean consistent = true;
            Vec2[] newcoord = new Vec2[mesh.getVertices().length];
            int index = 0;
            for (int i = 0; i < mesh.getFaceCount(); i++) {
                for (int j = 0; j < mesh.getFaceVertexCount(i); j++) {
                    Vec2 v = new Vec2(coord[index++]);
                    int vertIndex = mesh.getFaceVertexIndex(i, j);
                    if (newcoord[vertIndex] != null && !newcoord[vertIndex].equals(v)) {
                        consistent = false;
                    }
                    newcoord[vertIndex] = v;
                }
            }
            if (!consistent) {
                String[] options = MessageDialog.getOptions();
                int choice = new BStandardDialog("",
                        UIUtilities.breakString("Disabling per-face mapping will cause some mapping information to be lost.  Are you sure you want to do this?"),
                        BStandardDialog.QUESTION).showOptionDialog(this, options, options[1]);
                if (choice == 1) {
                    faceBox.setState(true);
                    return;
                }
            }
            setTextureCoords(newcoord);
            selected = new boolean[newcoord.length];
        }
        mapView.setDisplayedVertices(coord, new boolean[coord.length]);
        selectMode = (faceBox.getState() ? FACE_MODE : POINT_MODE);
        findSelectedVertices();
        rebuildImage();
        meshView.repaint();
    }

    private void coordsChanged(WidgetEvent ev) {
        Widget source = ev.getWidget();
        boolean[] sel = mapView.getSelection();
        for (int i = 0; i < sel.length; i++) {
            if (sel[i]) {
                if (source == uField && !Double.isNaN(uField.getValue())) {
                    coord[i].x = uField.getValue();
                }
                if (source == vField && !Double.isNaN(vField.getValue())) {
                    coord[i].y = vField.getValue();
                }
            }
        }
        setTextureCoords(coord);
        mapView.updateVertexPositions(coord);
        mapView.repaint();
    }

    /**
     * This is called when the displayed texture range changes.
     */
    public void displayRangeChanged() {
        if (mapView == null) {
            return;
        }
        minuField.setValue(mapView.getMinU());
        minvField.setValue(mapView.getMinV());
        maxuField.setValue(mapView.getMaxU());
        maxvField.setValue(mapView.getMaxV());
    }

    /**
     * Update the texture coordinates of the mesh.
     */
    public void setTextureCoords(Vec2[] coords) {
        coord = coords;
        if (faceBox.getState()) {
            FacetedMesh mesh = (FacetedMesh) editObj;
            Vec2[][] texCoord = new Vec2[mesh.getFaceCount()][];
            int index = 0;
            for (int i = 0; i < texCoord.length; i++) {
                texCoord[i] = new Vec2[mesh.getFaceVertexCount(i)];
                for (int j = 0; j < texCoord[i].length; j++) {
                    texCoord[i][j] = coord[index++];
                }
            }
            map.setFaceTextureCoordinates((Object3D) editObj, texCoord);
            map.setFaceTextureCoordinates(preview.getObject().getObject(), texCoord);
        } else {
            map.setTextureCoordinates((Object3D) editObj, coords);
            map.setTextureCoordinates(preview.getObject().getObject(), coords);
        }
        if (!mapView.isDragInProgress()) {
            preview.getObject().clearCachedMeshes();
            preview.render();
        }
    }

    @Override
    public ToolPalette getToolPalette() {
        return tools;
    }

    /**
     * Set the currently selected EditingTool.
     */
    @Override
    public void setTool(EditingTool tool) {
        mapView.setTool(tool);
    }

    /**
     * Set the text to display at the bottom of the window.
     */
    @Override
    public void setHelpText(String text) {
    }

    /**
     * Get the Frame for this EditingWindow: either the EditingWindow itself if it is a
     * Frame, or its parent if it is a Dialog.
     */
    @Override
    public BFrame getFrame() {
        return UIUtilities.findFrame(this);
    }

    /**
     * Update the image displayed in this window.
     */
    @Override
    public void updateImage() {
        mapView.repaint();
    }

    /**
     * This will be called whenever the selection changes, so rebuild the mesh
     * and update the text fields.
     */
    @Override
    public void updateMenus() {
        boolean selChanged = findSelectedVertices();
        if (selChanged) {
            mapView.setDisplayedVertices(coord, selectedVertices);
        }
        updateTextFields();
    }

    /**
     * Update the U and V text fields to reflect the current selection.
     */
    public void updateTextFields() {
        boolean[] sel = mapView.getSelection();
        boolean any = false;
        double u = 0.0;
        double v = 0.0;
        for (int i = 0; i < sel.length; i++) {
            if (sel[i]) {
                if (!any) {
                    u = coord[i].x;
                    v = coord[i].y;
                    any = true;
                } else {
                    if (u != coord[i].x) {
                        u = Double.NaN;
                    }
                    if (v != coord[i].y) {
                        v = Double.NaN;
                    }
                }
            }
        }
        uField.setValue(any ? u : Double.NaN);
        vField.setValue(any ? v : Double.NaN);
        uField.setEnabled(any);
        vField.setEnabled(any);
    }

    /**
     * Set the current UndoRecord for this EditingWindow.
     */
    @Override
    public void setUndoRecord(UndoRecord command) {
    }

    @Override
    public void setModified() {
    }

    /**
     * Get the Scene which is being edited in this window. If it is not a window for
     * editing a scene, this should return null.
     */
    @Override
    public Scene getScene() {
        return null;
    }

    /**
     * Get the ViewerCanvas in which the UV coordinates are being edited.
     */
    @Override
    public ViewerCanvas getView() {
        return mapView;
    }

    @Override
    public ViewerCanvas[] getAllViews() {
        return new ViewerCanvas[]{mapView};
    }

    /**
     * Confirm whether this window should be closed (possibly by displaying a message to the
     * user), and then close it. If the closing is canceled, this should return false.
     */
    @Override
    public boolean confirmClose() {
        return true;
    }
}
