/* Copyright (C) 1999-2011 by Peter Eastman
   Changes copyright (C) 2016-2025 by Maksim Khramov
   Changes copyright (C) 2017-2019 by Petri Ihalainen

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.image.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import static artofillusion.ui.UIUtilities.*;
import artofillusion.view.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.util.*;

/**
 * The SceneViewer class is a component which displays a view of a Scene.
 */
public class SceneViewer extends ViewerCanvas {

    final Scene theScene;
    final EditingWindow parentFrame;
    private final Vector<ObjectInfo> cameras;
    boolean draggingBox, draggingSelectionBox, squareBox, sentClick, dragging;
    Point clickPoint, dragPoint;
    ObjectInfo clickedObject;
    int deselect;

    public SceneViewer(Scene s, RowContainer p, EditingWindow fr) {
        this(s, p, fr, false);
    }

    public SceneViewer(Scene s, RowContainer p, EditingWindow fr, boolean forceSoftwareRendering) {
        super(ArtOfIllusion.getPreferences().getUseOpenGL() && isOpenGLAvailable() && !forceSoftwareRendering);
        theScene = s;
        parentFrame = fr;
        addEventLink(MouseClickedEvent.class, this, "mouseClicked");
        draggingBox = draggingSelectionBox = false;
        cameras = new Vector<>();
        buildChoices(p);
        rebuildCameraList();
        setRenderMode(ArtOfIllusion.getPreferences().getDefaultDisplayMode());
    }

    /**
     * Get the EditingWindow in which this canvas is displayed.
     */
    public EditingWindow getEditingWindow() {
        return parentFrame;
    }

    /**
     * Get the Scene displayed in this canvas.
     */
    @Override
    public Scene getScene() {
        return theScene;
    }

    /**
     * Add all SceneCameras in the scene to list of available views.
     */
    public void rebuildCameraList() {
        cameras.clear();
        cameras.addAll(theScene.getCameras());

        for (ObjectInfo info : theScene.getObjects()) {
            Object3D obj = info.getObject();
            if (obj instanceof DirectionalLight || obj instanceof SpotLight) {
                cameras.add(info);
            }
        }
        for (Iterator<Widget> iter = getViewerControlWidgets().values().iterator(); iter.hasNext();) {
            Widget w = iter.next();
            if (w instanceof ViewerOrientationControl.OrientationChoice) {
                ((ViewerOrientationControl.OrientationChoice) w).rebuildCameraList();
            }
        }
    }

    /**
     * Get the list of cameras in the scene which can be used as predefined orientations.
     */
    public ObjectInfo[] getCameras() {
        return cameras.toArray(new ObjectInfo[cameras.size()]);
    }

    /**
     * Deal with selecting a SceneCamera from the choice menu.
     */
    @Override
    public void setOrientation(int which) {
        super.setOrientation(which);
        if (which > 5 && which < 6 + cameras.size()) {
            ObjectInfo nextCamera = cameras.elementAt(which - 6);
            CoordinateSystem nextCoords = nextCamera.coords.duplicate();
            double workingDepth, projectionDist, nextScale;
            Vec3 nextCenter;

            // We seriously need a common interface for ViewControlledObjects
            if (nextCamera.getObject() instanceof SceneCamera) {
                workingDepth = ((SceneCamera) nextCamera.getObject()).getDistToPlane();
            } else if (nextCamera.getObject() instanceof SpotLight) {
                workingDepth = ((SpotLight) nextCamera.getObject()).getDistToPlane();
            } else if (nextCamera.getObject() instanceof DirectionalLight) {
                workingDepth = ((DirectionalLight) nextCamera.getObject()).getDistToPlane();
            } else {
                return;
            }
            nextCenter = nextCoords.getOrigin().plus(nextCoords.getZDirection().times(workingDepth));

            // SceneCamera does not obey the normal rules here. For now it is assumed to be always in perspective mode
            if (boundCamera != null && boundCamera.getObject() instanceof SceneCamera) {
                double innerAngle = (Math.PI - Math.toRadians(((SceneCamera) boundCamera.getObject()).getFieldOfView())) / 2.0;
                projectionDist = Math.tan(innerAngle) * getBounds().height / 2.0 / 100;
            } else {
                projectionDist = theCamera.getDistToScreen();
            }
            if (perspective) {
                nextScale = 100.0;
            } else {
                nextScale = 100.0 * projectionDist / workingDepth;
            }

            animation.start(nextCoords, nextCenter, nextScale, which, navigation);
        } else {
            boundCamera = null;
            if (which > 5) // Would have thought that the super takes care of this but not always.
            {
                orientation = VIEW_OTHER;
            }
            viewChanged(false);
        }
    }

    /**
     * ViewAnimation calls this when the animation is finished,
     * so the menu will be up to date.
     */
    @Override
    public void finishAnimation(int which, boolean persp, int navi) {
        if (which > 5 && which < 6 + cameras.size()) {
            boundCamera = cameras.elementAt(which - 6);
        }
        orientation = which;
        perspective = persp;
        navigation = navi;
    }

    /**
     * Estimate the range of depth values that the camera will need to render. This need not be exact,
     * but should err on the side of returning bounds that are slightly too large.
     *
     * @return the two element array {minDepth, maxDepth}
     */
    @Override
    public double[] estimateDepthRange() {
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        Mat4 toView = theCamera.getWorldToView();
        for (ObjectInfo info : theScene.getObjects()) {
            BoundingBox bounds = info.getBounds().transformAndOutset(toView.times(info.getCoords().fromLocal()));
            if (bounds.minz < min) {
                min = bounds.minz;
            }
            if (bounds.maxz > max) {
                max = bounds.maxz;
            }
        }
        return new double[]{min, max};
    }

    @Override
    public Vec3 getDefaultRotationCenter() {
        int[] selection = null;
        if (parentFrame instanceof LayoutWindow) {
            selection = ((LayoutWindow) parentFrame).getSelectedIndices();
        }
        if (selection == null || selection.length == 0) {
            CoordinateSystem coords = theCamera.getCameraCoordinates();
            double distToCenter = -coords.getZDirection().dot(coords.getOrigin());
            return coords.getOrigin().plus(coords.getZDirection().times(distToCenter));
        }
        BoundingBox bounds = null;
        for (int i = 0; i < selection.length; i++) {
            ObjectInfo info = theScene.getObject(selection[i]);
            BoundingBox objBounds = info.getBounds().transformAndOutset(info.getCoords().fromLocal());
            bounds = (i == 0 ? objBounds : bounds.merge(objBounds));
        }
        return bounds.getCenter();
    }

    @Override
    public void viewChanged(boolean selectionOnly) {
        super.viewChanged(selectionOnly);
        if (renderMode == RENDER_RENDERED && !selectionOnly) {
            // Re-render the image.

            Renderer rend = ArtOfIllusion.getPreferences().getObjectPreviewRenderer();
            if (rend == null) {
                return;
            }
            adjustCamera(true);
            Camera cam = theCamera.duplicate();
            rend.configurePreview();
            Rectangle bounds = getBounds();
            SceneCamera sceneCamera = new SceneCamera();
            sceneCamera.setFieldOfView(Math.atan(0.5 * bounds.height / cam.getViewToScreen().m33) * 360.0 / Math.PI);
            adjustCamera(isPerspective());
            RenderListener listener = new RenderListener() {
                @Override
                public void imageUpdated(Image image) {
                    renderedImage = image;
                    getCanvasDrawer().imageChanged(renderedImage);
                    repaint();
                }

                @Override
                public void imageComplete(ComplexImage image) {
                    renderedImage = image.getImage();
                    getCanvasDrawer().imageChanged(renderedImage);
                    repaint();
                }
            };
            rend.renderScene(theScene, cam, listener, sceneCamera);
        }
    }

    @Override
    public synchronized void updateImage() {
        if (renderMode == RENDER_RENDERED) {
            if (renderedImage != null && renderedImage.getWidth(null) > 0) {
                drawImage(renderedImage, 0, 0);
            } else {
                viewChanged(false);
            }
        } else {
            super.updateImage();

            // Draw the objects.
            Vec3 viewdir = theCamera.getViewToWorld().timesDirection(Vec3.vz());
            for (ObjectInfo obj : theScene.getObjects()) {
                if (obj == boundCamera || !obj.isVisible()) {
                    continue;
                }
                theCamera.setObjectTransform(obj.getCoords().fromLocal());
                obj.getObject().renderObject(obj, this, viewdir);
            }
        }

        // Hilight the selection.
        if (currentTool.hilightSelection())// && !animation.changingPerspective())
        {
            ArrayList<Rectangle> selectedBoxes = new ArrayList<>();
            ArrayList<Rectangle> parentSelectedBoxes = new ArrayList<>();
            for (ObjectInfo obj : theScene.getObjects()) {
                int hsize;
                ArrayList<Rectangle> boxes;

                if (obj.isLocked()) {
                    continue;
                }
                if (obj.isSelected()) {
                    hsize = Scene.HANDLE_SIZE;
                    boxes = selectedBoxes;
                } else if (obj.isParentSelected()) {
                    hsize = Scene.HANDLE_SIZE / 2;
                    boxes = parentSelectedBoxes;
                } else {
                    continue;
                }
                theCamera.setObjectTransform(obj.getCoords().fromLocal());
                Rectangle bounds = theCamera.findScreenBounds(obj.getBounds());
                if (bounds != null) {
                    boxes.add(new Rectangle(bounds.x, bounds.y, hsize, hsize));
                    boxes.add(new Rectangle(bounds.x + bounds.width - hsize + 1, bounds.y, hsize, hsize));
                    boxes.add(new Rectangle(bounds.x, bounds.y + bounds.height - hsize + 1, hsize, hsize));
                    boxes.add(new Rectangle(bounds.x + bounds.width - hsize + 1, bounds.y + bounds.height - hsize + 1, hsize, hsize));
                    boxes.add(new Rectangle(bounds.x + (bounds.width - hsize) / 2, bounds.y, hsize, hsize));
                    boxes.add(new Rectangle(bounds.x, bounds.y + (bounds.height - hsize) / 2, hsize, hsize));
                    boxes.add(new Rectangle(bounds.x + (bounds.width - hsize) / 2, bounds.y + bounds.height - hsize + 1, hsize, hsize));
                    boxes.add(new Rectangle(bounds.x + bounds.width - hsize + 1, bounds.y + (bounds.height - hsize) / 2, hsize, hsize));
                }
            }
            drawBoxes(selectedBoxes, handleColor);
            drawBoxes(parentSelectedBoxes, highlightColor);
        }

        // Finish up.
        currentTool.drawOverlay(this);
        if (activeTool != null) {
            activeTool.drawOverlay(this);
        }
        for (ViewerCanvas v : parentFrame.getAllViews()) {
            v.drawOverlay(this);
        }
        if (showAxes) {
            drawCoordinateAxes();
        }
        drawBorder();
    }

    /**
     * Begin dragging a box. The variable square determines whether the box should be
     * constrained to be square.
     */
    public void beginDraggingBox(Point p, boolean square) {
        draggingBox = true;
        clickPoint = p;
        squareBox = square;
        dragPoint = null;
    }

    /**
     * When the user presses the mouse, forward events to the current tool as appropriate.
     * If this is an object based tool, allow them to select or deselect objects.
     */
    @Override
    protected void mousePressed(WidgetMouseEvent e) {
        int i;
        int j;
        int k;
        int[] sel;
        int minarea;
        Rectangle bounds = null;
        ObjectInfo info;
        Point p;

        requestFocus();
        sentClick = false;
        deselect = -1;
        dragging = true;
        clickPoint = e.getPoint();
        clickedObject = null;

        // Determine which tool is active.
        if (metaTool != null && mouseButtonThree(e)) {
            activeTool = metaTool;
        } else if (altTool != null && mouseButtonTwo(e)) {
            activeTool = altTool;
        } else {
            activeTool = currentTool;
        }

        // If the current tool wants all clicks, just forward the event.
        if ((activeTool.whichClicks() & EditingTool.ALL_CLICKS) != 0) {
            moveToGrid(e);
            activeTool.mousePressed(e, this);
            sentClick = true;
        }
        boolean allowSelectionChange = activeTool.allowSelectionChanges();
        boolean wantHandleClicks = ((activeTool.whichClicks() & EditingTool.HANDLE_CLICKS) != 0);
        if (!allowSelectionChange && !wantHandleClicks) {
            return;
        }

        // See whether the click was on a currently selected object.
        p = e.getPoint();
        sel = theScene.getSelection();
        for (i = 0; i < sel.length; i++) {
            info = theScene.getObject(sel[i]);
            theCamera.setObjectTransform(info.getCoords().fromLocal());
            bounds = theCamera.findScreenBounds(info.getBounds());
            if (!info.isLocked() && bounds != null && pointInRectangle(p, bounds)) {
                clickedObject = info;
                break;
            }
        }
        if (i < sel.length) {
            // The click was on a selected object.  If it was a shift-click, the user may want
            // to deselect it, so set a flag.

            if (e.isShiftDown() && allowSelectionChange) {
                deselect = sel[i];
            }

            // If the current tool wants handle clicks, then check to see whether the click
            // was on a handle.
            if ((activeTool.whichClicks() & EditingTool.HANDLE_CLICKS) != 0) {
                if (p.x <= bounds.x + Scene.HANDLE_SIZE) {
                    j = 0;
                } else if (p.x >= bounds.x + (bounds.width - Scene.HANDLE_SIZE) / 2
                        && p.x <= bounds.x + (bounds.width - Scene.HANDLE_SIZE) / 2 + Scene.HANDLE_SIZE) {
                    j = 1;
                } else if (p.x >= bounds.x + bounds.width - Scene.HANDLE_SIZE) {
                    j = 2;
                } else {
                    j = -1;
                }
                if (p.y <= bounds.y + Scene.HANDLE_SIZE) {
                    k = 0;
                } else if (p.y >= bounds.y + (bounds.height - Scene.HANDLE_SIZE) / 2
                        && p.y <= bounds.y + (bounds.height - Scene.HANDLE_SIZE) / 2 + Scene.HANDLE_SIZE) {
                    k = 1;
                } else if (p.y >= bounds.y + bounds.height - Scene.HANDLE_SIZE) {
                    k = 2;
                } else {
                    k = -1;
                }
                if (k == 0) {
                    moveToGrid(e);
                    activeTool.mousePressedOnHandle(e, this, sel[i], j);
                    sentClick = true;
                    return;
                }
                if (j == 0 && k == 1) {
                    moveToGrid(e);
                    activeTool.mousePressedOnHandle(e, this, sel[i], 3);
                    sentClick = true;
                    return;
                }
                if (j == 2 && k == 1) {
                    moveToGrid(e);
                    activeTool.mousePressedOnHandle(e, this, sel[i], 4);
                    sentClick = true;
                    return;
                }
                if (k == 2) {
                    moveToGrid(e);
                    activeTool.mousePressedOnHandle(e, this, sel[i], j + 5);
                    sentClick = true;
                    return;
                }
            }
            moveToGrid(e);
            dragging = false;
            if ((activeTool.whichClicks() & EditingTool.OBJECT_CLICKS) != 0) {
                activeTool.mousePressedOnObject(e, this, sel[i]);
                sentClick = true;
            }
            return;
        }
        if (!allowSelectionChange) {
            return;
        }

        // The click was not on a selected object.  See whether it was on an unselected object.
        // If so, select it.  If appropriate, send an event to the current tool.
        // If the click was on top of multiple objects, the conventional thing to do is to select
        // the closest one.  I'm trying something different: select the smallest one.  This
        // should make it easier to select small objects which are surrounded by larger objects.
        // I may decide to change this, but it seemed like a good idea at the time...
        j = -1;
        minarea = Integer.MAX_VALUE;
        Vec3 cameraPosition = theCamera.getCameraCoordinates().getOrigin();
        Vec3 cameraAxis = theCamera.getCameraCoordinates().getZDirection();
        for (i = 0; i < theScene.getNumObjects(); i++) {
            info = theScene.getObject(i);
            if (info.isVisible() && !info.isLocked() && inFront(info, cameraPosition, cameraAxis)) {
                theCamera.setObjectTransform(info.getCoords().fromLocal());
                bounds = theCamera.findScreenBounds(info.getBounds());
                if (bounds != null && pointInRectangle(p, bounds)) {
                    if (bounds.width * bounds.height < minarea) {
                        j = i;
                        minarea = bounds.width * bounds.height;
                    }
                }
            }
        }
        if (j > -1) {
            info = theScene.getObject(j);
            if (!e.isShiftDown()) {
                if (parentFrame instanceof LayoutWindow) {
                    ((LayoutWindow) parentFrame).clearSelection();
                } else {
                    theScene.clearSelection();
                }
            }
            if (parentFrame instanceof LayoutWindow) {
                parentFrame.setUndoRecord(new UndoRecord(parentFrame, false, UndoRecord.SET_SCENE_SELECTION, sel));
                ((LayoutWindow) parentFrame).addToSelection(j);
            } else {
                theScene.addToSelection(j);
            }
            parentFrame.updateMenus();
            parentFrame.updateImage();
            moveToGrid(e);
            if ((activeTool.whichClicks() & EditingTool.OBJECT_CLICKS) != 0 && !e.isShiftDown()) {
                sentClick = true;
                activeTool.mousePressedOnObject(e, this, j);
            }
            clickedObject = info;
            return;
        }

        // The click was not on any object.  Start dragging a selection box.
        if (allowSelectionChange) {
            moveToGrid(e);
            draggingSelectionBox = true;
            beginDraggingBox(p, false);
        }
        sentClick = false;
    }

    /**
     * Determine whether a Point falls inside a Rectangle. This method allows a 1 pixel tolerance
     * to make it easier to click on very small objects.
     */
    private boolean pointInRectangle(Point p, Rectangle r) {
        return (r.x - 1 <= p.x && r.y - 1 <= p.y && r.x + r.width + 1 >= p.x && r.y + r.height + 1 >= p.y);
    }

    /**
     * Check if an object can be selected in perspective mode. An object, whose origin is on the "camera plane"
     * or behind it, should not be selected, when the view is set to perspective.
     */
    private boolean inFront(ObjectInfo info, Vec3 cameraPosition, Vec3 cameraAxis) {
        if (isPerspective()) {
            Vec3 difference = info.getCoords().getOrigin().minus(cameraPosition);
            double projectionDist = difference.dot(cameraAxis);
            return (projectionDist > 0.0);
        } else {
            return true;
        }
    }

    @Override
    protected void mouseDragged(WidgetMouseEvent e) {
        mousePoint = e.getPoint();
        moveToGrid(e);
        if (!dragging) {
            Point p = e.getPoint();
            if (Math.abs(p.x - clickPoint.x) < 2 && Math.abs(p.y - clickPoint.y) < 2) {
                return;
            }
        }

        dragging = true;
        deselect = -1;
        if (draggingBox) {
            // We are dragging a box, so erase and redraw it.

            if (dragPoint != null) {
                drawDraggedShape(new Rectangle(Math.min(clickPoint.x, dragPoint.x), Math.min(clickPoint.y, dragPoint.y),
                        Math.abs(dragPoint.x - clickPoint.x), Math.abs(dragPoint.y - clickPoint.y)));
            }
            dragPoint = e.getPoint();
            if (squareBox) {
                if (Math.abs(dragPoint.x - clickPoint.x) > Math.abs(dragPoint.y - clickPoint.y)) {
                    if (dragPoint.y < clickPoint.y) {
                        dragPoint.y = clickPoint.y - Math.abs(dragPoint.x - clickPoint.x);
                    } else {
                        dragPoint.y = clickPoint.y + Math.abs(dragPoint.x - clickPoint.x);
                    }
                } else {
                    if (dragPoint.x < clickPoint.x) {
                        dragPoint.x = clickPoint.x - Math.abs(dragPoint.y - clickPoint.y);
                    } else {
                        dragPoint.x = clickPoint.x + Math.abs(dragPoint.y - clickPoint.y);
                    }
                }
            }
            drawDraggedShape(new Rectangle(Math.min(clickPoint.x, dragPoint.x), Math.min(clickPoint.y, dragPoint.y),
                    Math.abs(dragPoint.x - clickPoint.x), Math.abs(dragPoint.y - clickPoint.y)));
        }

        // Send the event to the current tool, if appropriate.
        if (sentClick) {
            activeTool.mouseDragged(e, this);
        }
    }

    @Override
    protected void mouseReleased(WidgetMouseEvent e) {
        Rectangle r, b;
        int j;
        int[] sel = theScene.getSelection();
        ObjectInfo info;

        moveToGrid(e);

        // Send the event to the current tool, if appropriate.
        if (sentClick) {
            if (!dragging) {
                Point p = e.getPoint();
                e.translatePoint(clickPoint.x - p.x, clickPoint.y - p.y);
            }
            activeTool.mouseReleased(e, this);
        }

        // If the user was dragging a selection box, then select anything it intersects.
        int[] oldSelection = theScene.getSelection();
        if (draggingSelectionBox) {
            dragPoint = e.getPoint();
            r = new Rectangle(Math.min(clickPoint.x, dragPoint.x), Math.min(clickPoint.y, dragPoint.y),
                    Math.abs(dragPoint.x - clickPoint.x), Math.abs(dragPoint.y - clickPoint.y));
            if (!e.isShiftDown()) {
                if (parentFrame instanceof LayoutWindow) {
                    ((LayoutWindow) parentFrame).clearSelection();
                } else {
                    theScene.clearSelection();
                }
                parentFrame.updateMenus();
            }
            Vec3 cameraPosition = theCamera.getCameraCoordinates().getOrigin();
            Vec3 cameraAxis = theCamera.getCameraCoordinates().getZDirection();
            for (int i = 0; i < theScene.getNumObjects(); i++) {
                info = theScene.getObject(i);
                if (info.isVisible() && !info.isLocked() && inFront(info, cameraPosition, cameraAxis)) {
                    theCamera.setObjectTransform(info.getCoords().fromLocal());
                    b = theCamera.findScreenBounds(info.getBounds());
                    if (b != null && b.x < r.x + r.width && b.y < r.y + r.height && r.x < b.x + b.width && r.y < b.y + b.height) {
                        if (!e.isShiftDown()) {
                            if (parentFrame instanceof LayoutWindow) {
                                ((LayoutWindow) parentFrame).addToSelection(i);
                            } else {
                                theScene.addToSelection(i);
                            }
                            parentFrame.updateMenus();
                        } else {
                            for (j = 0; j < sel.length && sel[j] != i; j++);
                            if (j == sel.length) {
                                if (parentFrame instanceof LayoutWindow) {
                                    ((LayoutWindow) parentFrame).addToSelection(i);
                                } else {
                                    theScene.addToSelection(i);
                                }
                                parentFrame.updateMenus();
                            }
                        }
                    }
                }
            }
            if (currentTool.hilightSelection()) {
                parentFrame.updateImage();
            }
        }
        draggingBox = draggingSelectionBox = false;

        // If the user shift-clicked a selected object and released the mouse without dragging,
        // then deselect the point.
        if (deselect > -1) {
            if (parentFrame instanceof LayoutWindow) {
                ((LayoutWindow) parentFrame).removeFromSelection(deselect);
            } else {
                theScene.removeFromSelection(deselect);
            }
            parentFrame.updateMenus();
            parentFrame.updateImage();
        }

        // If the selection changed, set an undo record.
        int[] newSelection = theScene.getSelection();
        boolean changed = (oldSelection.length != newSelection.length);
        for (int i = 0; i < newSelection.length && !changed; i++) {
            changed = (oldSelection[i] != newSelection[i]);
        }
        if (changed) {
            parentFrame.setUndoRecord(new UndoRecord(parentFrame, false, UndoRecord.SET_SCENE_SELECTION, oldSelection));
        }
        dragging = false;
    }

    /**
     * Double-clicking on object should bring up its editor.
     */
    public void mouseClicked(MouseClickedEvent e) {
        if (e.getClickCount() == 2 && (activeTool.whichClicks() & EditingTool.OBJECT_CLICKS) != 0 && clickedObject != null && clickedObject.getObject().isEditable()) {
            final Object3D obj = clickedObject.getObject();
            parentFrame.setUndoRecord(new UndoRecord(parentFrame, false, UndoRecord.COPY_OBJECT, obj, obj.duplicate()));
            obj.edit(parentFrame, clickedObject, new Runnable() {
                @Override
                public void run() {
                    theScene.objectModified(obj);
                    parentFrame.updateImage();
                    parentFrame.updateMenus();
                }
            });
        }
    }

    /**
     * This is called recursively to move any children of a bound camera.
     */
    private void moveChildren(ObjectInfo obj, Mat4 transform, UndoRecord undo) {
        CoordinateSystem coords = obj.getCoords();
        CoordinateSystem oldCoords = coords.duplicate();
        coords.transformCoordinates(transform);
        undo.addCommand(UndoRecord.COPY_COORDS, coords, oldCoords);
        for (int i = 0; i < obj.getChildren().length; i++) {
            moveChildren(obj.getChildren()[i], transform, undo);
        }
    }
}
