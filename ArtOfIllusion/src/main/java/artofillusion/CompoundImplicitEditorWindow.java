/* Copyright (C) 1999-2013 by Peter Eastman
   Modifications copyright (C) 2016-2017 Petri Ihalainen
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * The CompoundImplicitEditorWindow class represents the window for editing CompoundImplicitObjects.
 */
public class CompoundImplicitEditorWindow extends ObjectEditorWindow {

    private CompoundImplicitObject oldObject;
    private CompoundImplicitObject theObject;
    private BMenuItem undoItem;
    private BMenuItem redoItem;
    private BMenuItem[] objectMenuItem;
    private BMenuItem templateItem;
    private BMenuItem axesItem;
    private BMenuItem splitViewItem;
    private final ButtonGroup displayModesGroup = new ButtonGroup();
    private BCheckBoxMenuItem[] displayItem;
    private Scene theScene;
    private final Runnable onClose;

    public CompoundImplicitEditorWindow(EditingWindow parent, String title, CompoundImplicitObject obj, Runnable onClose) {
        super(parent, title, new ObjectInfo(obj, new CoordinateSystem(), ""));
        theScene = new Scene();
        initialize();
        oldObject = obj;
        theObject = obj.duplicate();
        this.onClose = onClose;
        for (int i = 0; i < obj.getNumObjects(); i++) {
            theScene.addObject(obj.getObject(i).duplicate(), obj.getObjectCoordinates(i).duplicate(), "", null);
        }
        FormContainer content = new FormContainer(new double[]{0, 1}, new double[]{1, 0, 0});
        setContent(content);
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
        content.add(helpText = new BLabel(), 0, 1, 2, 1);
        content.add(viewsContainer, 1, 0);
        RowContainer buttons = new RowContainer();
        buttons.add(Translate.button("ok", event -> doOk()));
        buttons.add(Translate.button("cancel", event -> doCancel()));
        content.add(buttons, 0, 2, 2, 1, new LayoutInfo());
        content.add(tools = new ToolPalette(1, 5, this), 0, 0);
        EditingTool metaTool;
        EditingTool altTool;
        tools.addTool(defaultTool = new MoveObjectTool(this));
        tools.addTool(new RotateObjectTool(this));
        tools.addTool(new ScaleObjectTool(this));
        tools.addTool(metaTool = new MoveViewTool(this));
        tools.addTool(altTool = new RotateViewTool(this));
        tools.selectTool(defaultTool);
        for (int i = 0; i < theView.length; i++) {
            theView[i].setMetaTool(metaTool);
            theView[i].setAltTool(altTool);
        }
        UIUtilities.applyDefaultFont(content);
        UIUtilities.applyDefaultBackground(content);
        createEditMenu();
        createObjectMenu();
        createViewMenu();
        recursivelyAddListeners(this);
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Dimension windowDim = new Dimension((screenBounds.width * 3) / 4, (screenBounds.height * 3) / 4);
        setBounds(new Rectangle((screenBounds.width - windowDim.width) / 2, (screenBounds.height - windowDim.height) / 2, windowDim.width, windowDim.height));
        tools.requestFocus();
        updateMenus();
        setVisible(true);
    }

    @Override
    protected ViewerCanvas createViewerCanvas(int index, RowContainer controls) {
        return new SceneViewer(theScene, controls, this);
    }

    void createEditMenu() {
        BMenu editMenu = Translate.menu("edit");
        menubar.add(editMenu);
        editMenu.add(undoItem = Translate.menuItem("undo", e -> undoCommand()));
        editMenu.add(redoItem = Translate.menuItem("redo", e -> redoCommand()));
        undoItem.setEnabled(false);
        editMenu.addSeparator();
        editMenu.add(Translate.menuItem("selectAll", e -> selectAllCommand()));
    }

    void createObjectMenu() {
        BMenu objectMenu = Translate.menu("object");
        menubar.add(objectMenu);
        objectMenuItem = new BMenuItem[5];
        objectMenu.add(objectMenuItem[0] = Translate.menuItem("editObject", e -> editObjectCommand()));
        objectMenu.add(objectMenuItem[1] = Translate.menuItem("objectLayout", e -> objectLayoutCommand()));
        objectMenu.add(objectMenuItem[2] = Translate.menuItem("transformObject", e -> transformObjectCommand()));
        objectMenu.add(objectMenuItem[3] = Translate.menuItem("alignObjects", e -> alignObjectsCommand()));
        objectMenu.add(Translate.menuItem("centerObjects", e -> centerObjectsCommand()));
        objectMenu.add(objectMenuItem[4] = Translate.menuItem("convertToTriangle", e -> convertToTriangleCommand()));
    }

    protected void createViewMenu() {
        BMenu viewMenu;
        BMenu displayMenu;
        viewMenu = Translate.menu("view");
        menubar.add(viewMenu);
        viewMenu.add(displayMenu = Translate.menu("displayMode"));
        displayItem = new BCheckBoxMenuItem[5];
        ViewerCanvas view = theView[currentView];
        displayMenu.add(displayItem[0] = Translate.checkboxMenuItem("wireframeDisplay", event -> setViewMode(ViewerCanvas.RENDER_WIREFRAME), view.getRenderMode() == ViewerCanvas.RENDER_WIREFRAME));
        displayMenu.add(displayItem[1] = Translate.checkboxMenuItem("shadedDisplay", event -> setViewMode(ViewerCanvas.RENDER_FLAT), view.getRenderMode() == ViewerCanvas.RENDER_FLAT));
        displayMenu.add(displayItem[2] = Translate.checkboxMenuItem("smoothDisplay", event -> setViewMode(ViewerCanvas.RENDER_SMOOTH), view.getRenderMode() == ViewerCanvas.RENDER_SMOOTH));
        displayMenu.add(displayItem[3] = Translate.checkboxMenuItem("texturedDisplay", event -> setViewMode(ViewerCanvas.RENDER_TEXTURED), view.getRenderMode() == ViewerCanvas.RENDER_TEXTURED));
        displayMenu.add(displayItem[4] = Translate.checkboxMenuItem("transparentDisplay", event -> setViewMode(ViewerCanvas.RENDER_TRANSPARENT), view.getRenderMode() == ViewerCanvas.RENDER_TRANSPARENT));

        for(var di: displayItem) displayModesGroup.add(di.getComponent());

        viewMenu.add(splitViewItem = Translate.menuItem(numViewsShown == 1 ? "fourViews" : "oneView", e -> toggleViewsCommand()));
        viewMenu.add(Translate.menuItem("grid", e -> setGridCommand()));
        viewMenu.add(axesItem = Translate.menuItem(view.getShowAxes() ? "hideCoordinateAxes" : "showCoordinateAxes", e -> showAxesCommand()));
        viewMenu.add(templateItem = Translate.menuItem("showTemplate", e -> showTemplateCommand()));
        viewMenu.add(Translate.menuItem("setTemplate", e -> setTemplateCommand()));
        if (ArtOfIllusion.getPreferences().getObjectPreviewRenderer() != null) {
            viewMenu.addSeparator();
            viewMenu.add(Translate.menuItem("renderPreview", e-> renderPreviewCommand()));
        }
    }

    /* EditingWindow methods. */
    @Override
    public void updateMenus() {
        ViewerCanvas view = getView();
        int[] selected = theScene.getSelection();
        undoItem.setEnabled(undoStack.canUndo());
        redoItem.setEnabled(undoStack.canRedo());
        objectMenuItem[0].setEnabled(selected.length == 1);
        objectMenuItem[1].setEnabled(selected.length > 0);
        objectMenuItem[2].setEnabled(selected.length > 0);
        objectMenuItem[3].setEnabled(selected.length > 1);
        objectMenuItem[4].setEnabled(selected.length == 1);
        templateItem.setEnabled(view.getTemplateImage() != null);
        templateItem.setText(view.getTemplateShown() ? Translate.text("menu.hideTemplate") : Translate.text("menu.showTemplate"));
        splitViewItem.setText(numViewsShown == 1 ? Translate.text("menu.fourViews") : Translate.text("menu.oneView"));
        axesItem.setText(Translate.text(view.getShowAxes() ? "menu.hideCoordinateAxes" : "menu.showCoordinateAxes"));
        displayItem[0].setState(view.getRenderMode() == ViewerCanvas.RENDER_WIREFRAME);
        displayItem[1].setState(view.getRenderMode() == ViewerCanvas.RENDER_FLAT);
        displayItem[2].setState(view.getRenderMode() == ViewerCanvas.RENDER_SMOOTH);
        displayItem[3].setState(view.getRenderMode() == ViewerCanvas.RENDER_TEXTURED);
    }

    @Override
    public Scene getScene() {
        return theScene;
    }

    @Override
    protected void doOk() {
        updateFromScene();
        oldObject.copyObject(theObject);
        oldObject = theObject = null;
        theScene = null;
        dispose();
        onClose.run();
        parentWindow.updateImage();
        parentWindow.updateMenus();
    }

    @Override
    protected void doCancel() {
        oldObject = theObject = null;
        theScene = null;
        dispose();
    }

    private void setViewMode(int mode) {
        theView[currentView].setRenderMode(mode);
        savePreferences();
    }

    /* Update the object based on the scene being used for editing it. */
    private void updateFromScene() {
        for (int i = 0; i < theObject.getNumObjects(); i++) {
            theObject.setObject(i, (ImplicitObject) theScene.getObject(i).getObject());
            theObject.setObjectCoordinates(i, theScene.getObject(i).getCoords());
        }
    }

    void selectAllCommand() {
        theScene.setSelection(new int[]{0, 1});
        updateImage();
        updateMenus();
    }

    void editObjectCommand() {
        int[] sel = theScene.getSelection();

        if (sel.length != 1) {
            return;
        }
        final Object3D obj = theScene.getObject(sel[0]).getObject();
        if (obj.isEditable()) {
            final UndoRecord undo = new UndoRecord(this, false, UndoRecord.COPY_OBJECT, obj, obj.duplicate());
            obj.edit(this, theScene.getObject(sel[0]), new Runnable() {
                @Override
                public void run() {
                    setUndoRecord(undo);
                    theScene.objectModified(obj);
                    updateImage();
                    updateMenus();
                }
            });
        }
    }

    void objectLayoutCommand() {
        int i;
        int[] sel = theScene.getSelection();
        Object3D[] obj = new Object3D[sel.length];
        CoordinateSystem[] coords = new CoordinateSystem[sel.length];
        Vec3 orig;
        Vec3 size;
        double[] angles;
        double[] values;

        if (sel.length == 0) {
            return;
        }
        UndoRecord undo = new UndoRecord(this);
        setUndoRecord(undo);
        for (i = 0; i < sel.length; i++) {
            obj[i] = theScene.getObject(sel[i]).getObject();
            coords[i] = theScene.getObject(sel[i]).getCoords();
            undo.addCommand(UndoRecord.COPY_OBJECT, obj[i], obj[i].duplicate());
            undo.addCommand(UndoRecord.COPY_COORDS, coords[i], coords[i].duplicate());
        }
        if (sel.length == 1) {
            orig = coords[0].getOrigin();
            angles = coords[0].getRotationAngles();
            size = theScene.getObject(sel[0]).getBounds().getSize();
            TransformDialog dlg = new TransformDialog(this, Translate.text("objectLayoutTitleMultiple"),
                    new double[]{orig.x, orig.y, orig.z, angles[0], angles[1], angles[2],
                        size.x, size.y, size.z}, false, false);
            values = dlg.getValues();
            if (!Double.isNaN(values[0])) {
                orig.x = values[0];
            }
            if (!Double.isNaN(values[1])) {
                orig.y = values[1];
            }
            if (!Double.isNaN(values[2])) {
                orig.z = values[2];
            }
            if (!Double.isNaN(values[3])) {
                angles[0] = values[3];
            }
            if (!Double.isNaN(values[4])) {
                angles[1] = values[4];
            }
            if (!Double.isNaN(values[5])) {
                angles[2] = values[5];
            }
            if (!Double.isNaN(values[6])) {
                size.x = values[6];
            }
            if (!Double.isNaN(values[7])) {
                size.y = values[7];
            }
            if (!Double.isNaN(values[8])) {
                size.z = values[8];
            }
            coords[0].setOrigin(orig);
            coords[0].setOrientation(angles[0], angles[1], angles[2]);
            obj[0].setSize(size.x, size.y, size.z);
        } else {
            TransformDialog dlg = new TransformDialog(this, Translate.text("objectLayoutTitleMultiple"), false, false);
            values = dlg.getValues();
            for (i = 0; i < sel.length; i++) {
                orig = coords[i].getOrigin();
                angles = coords[i].getRotationAngles();
                size = theScene.getObject(sel[i]).getBounds().getSize();
                if (!Double.isNaN(values[0])) {
                    orig.x = values[0];
                }
                if (!Double.isNaN(values[1])) {
                    orig.y = values[1];
                }
                if (!Double.isNaN(values[2])) {
                    orig.z = values[2];
                }
                if (!Double.isNaN(values[3])) {
                    angles[0] = values[3];
                }
                if (!Double.isNaN(values[4])) {
                    angles[1] = values[4];
                }
                if (!Double.isNaN(values[5])) {
                    angles[2] = values[5];
                }
                if (!Double.isNaN(values[6])) {
                    size.x = values[6];
                }
                if (!Double.isNaN(values[7])) {
                    size.y = values[7];
                }
                if (!Double.isNaN(values[8])) {
                    size.z = values[8];
                }
                coords[i].setOrigin(orig);
                coords[i].setOrientation(angles[0], angles[1], angles[2]);
                obj[i].setSize(size.x, size.y, size.z);
            }
        }
        theScene.objectModified(obj[0]);
        updateImage();
    }

    void transformObjectCommand() {
        int i;
        int[] sel = theScene.getSelection();
        TransformDialog dlg;
        Object3D obj;
        CoordinateSystem coords;
        Vec3 orig;
        Vec3 size;
        double[] values;
        Mat4 m;

        if (sel.length == 0) {
            return;
        }
        dlg = new TransformDialog(this, Translate.text("transformObjectTitleMultiple"),
                new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0}, true, true);
        values = dlg.getValues();
        UndoRecord undo = new UndoRecord(this);
        setUndoRecord(undo);
        for (i = 0; i < sel.length; i++) {
            obj = theScene.getObject(sel[i]).getObject();
            coords = theScene.getObject(sel[i]).getCoords();
            undo.addCommand(UndoRecord.COPY_OBJECT, obj, obj.duplicate());
            undo.addCommand(UndoRecord.COPY_COORDS, coords, coords.duplicate());
            orig = coords.getOrigin();
            size = obj.getBounds().getSize();
            if (!Double.isNaN(values[0])) {
                orig.x += values[0];
            }
            if (!Double.isNaN(values[1])) {
                orig.y += values[1];
            }
            if (!Double.isNaN(values[2])) {
                orig.z += values[2];
            }
            m = Mat4.identity();
            if (!Double.isNaN(values[3])) {
                m = m.times(Mat4.xrotation(values[3] * Math.PI / 180.0));
            }
            if (!Double.isNaN(values[4])) {
                m = m.times(Mat4.yrotation(values[4] * Math.PI / 180.0));
            }
            if (!Double.isNaN(values[5])) {
                m = m.times(Mat4.zrotation(values[5] * Math.PI / 180.0));
            }
            if (!Double.isNaN(values[6])) {
                size.x *= values[6];
            }
            if (!Double.isNaN(values[7])) {
                size.y *= values[7];
            }
            if (!Double.isNaN(values[8])) {
                size.z *= values[8];
            }
            coords.setOrigin(orig);
            coords.transformAxes(m);
            obj.setSize(size.x, size.y, size.z);
        }
        updateImage();
    }

    void alignObjectsCommand() {

        int[] sel = theScene.getSelection();
        ComponentsDialog dlg;
        Object3D obj;
        CoordinateSystem coords;
        Vec3 alignTo;
        Vec3 orig;
        Vec3 center;
        BComboBox xchoice;
        BComboBox ychoice;
        BComboBox zchoice;
        RowContainer px = new RowContainer();
        RowContainer py = new RowContainer();
        RowContainer pz = new RowContainer();
        ValueField vfx;
        ValueField vfy;
        ValueField vfz;
        BoundingBox bounds;

        if (sel.length == 0) {
            return;
        }
        px.add(xchoice = new BComboBox(new String[]{
            Translate.text("doNotAlign"),
            Translate.text("Right"),
            Translate.text("Center"),
            Translate.text("Left"),
            Translate.text("Origin")
        }));
        px.add(Translate.label("alignTo"));
        px.add(vfx = new ValueField(Double.NaN, ValueField.NONE, 5));
        py.add(ychoice = new BComboBox(new String[]{
            Translate.text("doNotAlign"),
            Translate.text("Top"),
            Translate.text("Center"),
            Translate.text("Bottom"),
            Translate.text("Origin")
        }));
        py.add(Translate.label("alignTo"));
        py.add(vfy = new ValueField(Double.NaN, ValueField.NONE, 5));
        pz.add(zchoice = new BComboBox(new String[]{
            Translate.text("doNotAlign"),
            Translate.text("Front"),
            Translate.text("Center"),
            Translate.text("Back"),
            Translate.text("Origin")
        }));
        pz.add(Translate.label("alignTo"));
        pz.add(vfz = new ValueField(Double.NaN, ValueField.NONE, 5));
        dlg = new ComponentsDialog(this, Translate.text("alignObjectsTitle"),
                new Widget[]{px, py, pz}, new String[]{"X", "Y", "Z"});
        if (!dlg.clickedOk()) {
            return;
        }
        UndoRecord undo = new UndoRecord(this);
        setUndoRecord(undo);

        // Determine the position to align the objects to.
        alignTo = new Vec3();
        for (int i = 0; i < sel.length; i++) {
            obj = theScene.getObject(sel[i]).getObject();
            coords = theScene.getObject(sel[i]).getCoords();
            bounds = obj.getBounds();
            bounds = bounds.transformAndOutset(coords.fromLocal());
            center = bounds.getCenter();
            orig = coords.getOrigin();
            if (!Double.isNaN(vfx.getValue())) {
                alignTo.x += vfx.getValue();
            } else if (xchoice.getSelectedIndex() == 1) {
                alignTo.x += bounds.maxx;
            } else if (xchoice.getSelectedIndex() == 2) {
                alignTo.x += center.x;
            } else if (xchoice.getSelectedIndex() == 3) {
                alignTo.x += bounds.minx;
            } else if (xchoice.getSelectedIndex() == 4) {
                alignTo.x += orig.x;
            }
            if (!Double.isNaN(vfy.getValue())) {
                alignTo.y += vfy.getValue();
            } else if (ychoice.getSelectedIndex() == 1) {
                alignTo.y += bounds.maxy;
            } else if (ychoice.getSelectedIndex() == 2) {
                alignTo.y += center.y;
            } else if (ychoice.getSelectedIndex() == 3) {
                alignTo.y += bounds.miny;
            } else if (ychoice.getSelectedIndex() == 4) {
                alignTo.y += orig.y;
            }
            if (!Double.isNaN(vfz.getValue())) {
                alignTo.z += vfz.getValue();
            } else if (zchoice.getSelectedIndex() == 1) {
                alignTo.z += bounds.maxz;
            } else if (zchoice.getSelectedIndex() == 2) {
                alignTo.z += center.z;
            } else if (zchoice.getSelectedIndex() == 3) {
                alignTo.z += bounds.minz;
            } else if (zchoice.getSelectedIndex() == 4) {
                alignTo.z += orig.z;
            }
        }
        alignTo.scale(1.0 / sel.length);

        // Now transform all of the objects.
        for (int i = 0; i < sel.length; i++) {
            obj = theScene.getObject(sel[i]).getObject();
            coords = theScene.getObject(sel[i]).getCoords();
            bounds = obj.getBounds();
            bounds = bounds.transformAndOutset(coords.fromLocal());
            center = bounds.getCenter();
            orig = coords.getOrigin();
            undo.addCommand(UndoRecord.COPY_COORDS, coords, coords.duplicate());
            if (xchoice.getSelectedIndex() == 1) {
                orig.x += alignTo.x - bounds.maxx;
            } else if (xchoice.getSelectedIndex() == 2) {
                orig.x += alignTo.x - center.x;
            } else if (xchoice.getSelectedIndex() == 3) {
                orig.x += alignTo.x - bounds.minx;
            } else if (xchoice.getSelectedIndex() == 4) {
                orig.x += alignTo.x - orig.x;
            }
            if (ychoice.getSelectedIndex() == 1) {
                orig.y += alignTo.y - bounds.maxy;
            } else if (ychoice.getSelectedIndex() == 2) {
                orig.y += alignTo.y - center.y;
            } else if (ychoice.getSelectedIndex() == 3) {
                orig.y += alignTo.y - bounds.miny;
            } else if (ychoice.getSelectedIndex() == 4) {
                orig.y += alignTo.y - orig.y;
            }
            if (zchoice.getSelectedIndex() == 1) {
                orig.z += alignTo.z - bounds.maxz;
            } else if (zchoice.getSelectedIndex() == 2) {
                orig.z += alignTo.z - center.z;
            } else if (zchoice.getSelectedIndex() == 3) {
                orig.z += alignTo.z - bounds.minz;
            } else if (zchoice.getSelectedIndex() == 4) {
                orig.z += alignTo.z - orig.z;
            }
            coords.setOrigin(orig);
        }
        updateImage();
    }

    void centerObjectsCommand() {
        BoundingBox bounds = null;

        // Determine the bounding box for all objects.
        List<ObjectInfo> objects = theScene.getObjects();
        for (ObjectInfo info : objects) {
            BoundingBox b = info.getBounds().transformAndOutset(info.getCoords().fromLocal());
            if (bounds == null) {
                bounds = b;
            } else {
                bounds = bounds.merge(b);
            }
        }
        Vec3 center = bounds.getCenter();
        UndoRecord undo = new UndoRecord(this);
        setUndoRecord(undo);

        // Center the objects.
        for (ObjectInfo info : objects) {
            undo.addCommand(UndoRecord.COPY_COORDS, info.getCoords(), info.getCoords().duplicate());
            info.getCoords().setOrigin(info.getCoords().getOrigin().minus(center));
        }
        updateImage();
    }

    void convertToTriangleCommand() {
        int[] sel = theScene.getSelection();
        Object3D obj;
        Object3D mesh;
        ObjectInfo info;

        if (sel.length != 1) {
            return;
        }
        info = theScene.getObject(sel[0]);
        obj = info.getObject();
        if (obj.canConvertToTriangleMesh() == Object3D.CANT_CONVERT) {
            return;
        }
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT_INFO, info, info.duplicate()));
        if (obj.canConvertToTriangleMesh() == Object3D.EXACTLY) {
            mesh = obj.convertToTriangleMesh(0.0);
        } else {
            ValueField errorField = new ValueField(0.1, ValueField.POSITIVE);
            ComponentsDialog dlg = new ComponentsDialog(this, Translate.text("selectToleranceForMesh"),
                    new Widget[]{errorField}, new String[]{Translate.text("maxError")});
            if (!dlg.clickedOk()) {
                return;
            }
            mesh = obj.convertToTriangleMesh(errorField.getValue());
        }
        if (mesh == null) {
            MessageDialog.create().withOwner(this.getComponent()).error("cannotTriangulate");
            return;
        }
        mesh.setTexture(obj.getTexture(), obj.getTextureMapping());
        mesh.setMaterial(obj.getMaterial(), obj.getMaterialMapping());
        theScene.getObject(sel[0]).setObject(mesh);
        updateImage();
        updateMenus();
    }

    public void renderPreviewCommand() {
        Scene sc = new Scene();
        Camera theCamera = getView().getCamera();
        Renderer rend = ArtOfIllusion.getPreferences().getObjectPreviewRenderer();

        if (rend == null) {
            return;
        }
        updateFromScene();
        sc.addObject(new DirectionalLight(new RGBColor(1.0f, 1.0f, 1.0f), 0.8f), theCamera.getCameraCoordinates(), "", null);
        sc.addObject(theObject, new CoordinateSystem(new Vec3(), Vec3.vz(), Vec3.vy()), "", null);
        getView().adjustCamera(true);
        rend.configurePreview();
        ObjectInfo cameraInfo = new ObjectInfo(new SceneCamera(), theCamera.getCameraCoordinates(), "");
        new RenderingDialog(this, rend, sc, theCamera, cameraInfo);
    }
}
