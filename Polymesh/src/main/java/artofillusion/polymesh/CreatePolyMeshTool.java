/* Copyright (C) 2001-2004 by Peter Eastman, 2005 by Francois Guillet
   Changes copyright (C) 2023-2025 by Maksim Khramov
This program is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.Camera;
import artofillusion.LayoutWindow;
import artofillusion.Scene;
import artofillusion.SceneViewer;
import artofillusion.UndoRecord;
import artofillusion.ViewerCanvas;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import artofillusion.object.Mesh;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.EditingTool;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.Translate;
import buoy.event.WidgetMouseEvent;

import java.awt.Point;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * CreatePolyMeshTool is an EditingTool used for creating PolyMesh objects.
 */
@Slf4j
@EditingTool.ButtonImage("polymesh:polymesh")
@EditingTool.Tooltip("polymesh:createPolyMeshTool.tipText")
public class CreatePolyMeshTool extends EditingTool {

    private final EditingWindow edw;
    private int counter = 1;

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private int shape = 0;
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private int usize = 3;
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private int vsize = 3;
    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private int smoothingMethod =  Mesh.NO_SMOOTHING;

    @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
    private PolyMesh templateMesh;

    boolean shiftDown;
    Point clickPoint;

    public CreatePolyMeshTool(EditingWindow fr) {
        super(fr);
        edw = fr;
    }

    @Override
    public void activate() {
        super.activate();
        setHelpText();
    }

    private void setHelpText() {
        String shapeDesc, smoothingDesc;
        if (shape == 0) {
            shapeDesc = "cube";
        } else if (shape == 2) {
            shapeDesc = "octahedron";
        } else if (shape == 3) {
            shapeDesc = "cylinder";
        } else if (shape == 1) {
            shapeDesc = "single face";
        } else {
            shapeDesc = "flat";
        }
        switch (smoothingMethod) {
            default:
            case 0:
                smoothingDesc = "none";
                break;
            case 1:
                smoothingDesc = "shading";
                break;
            case 2:
                smoothingDesc = "approximating";
                break;
            case 3:
                smoothingDesc = "interpolating";
                break;
        }
        if ("none".equals(smoothingDesc)) {
            smoothingDesc = Translate.text("polymesh:none");
        } else {
            smoothingDesc = Translate.text("menu." + smoothingDesc).toLowerCase();
        }
        if (shape <= 2) {
            theWindow.setHelpText(Translate.text("polymesh:createPolyMeshTool.helpText1",
                    new Object[]{Translate.text("polymesh:createPolyMeshTool." + shapeDesc), smoothingDesc}));
        } else {
            theWindow.setHelpText(Translate.text("polymesh:createPolyMeshTool.helpText2",
                    new Object[]{Translate.text("polymesh:createPolyMeshTool." + shapeDesc),
                        Integer.toString(usize), Integer.toString(vsize), smoothingDesc}));
        }
    }

    @Override
    public void mousePressed(WidgetMouseEvent e, ViewerCanvas view) {
        clickPoint = e.getPoint();
        shiftDown = e.isShiftDown();
        ((SceneViewer) view).beginDraggingBox(clickPoint, shiftDown);
    }

    @Override
    public void mouseReleased(WidgetMouseEvent e, ViewerCanvas view) {
        Scene theScene = ((LayoutWindow) theWindow).getScene();
        Camera cam = view.getCamera();
        Point dragPoint = e.getPoint();
        Vec3 v1, v2, v3, orig, xdir, ydir, zdir;
        double xsize, ysize, zsize;

        if (shiftDown) {
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
        if (dragPoint.x == clickPoint.x || dragPoint.y == clickPoint.y) {
            ((SceneViewer) view).repaint();
            return;
        }
        v1 = cam.convertScreenToWorld(clickPoint, cam.getDistToScreen());
        v2 = cam.convertScreenToWorld(new Point(dragPoint.x, clickPoint.y), cam.getDistToScreen());
        v3 = cam.convertScreenToWorld(dragPoint, cam.getDistToScreen());
        orig = v1.plus(v3).times(0.5);
        if (dragPoint.x < clickPoint.x) {
            xdir = v1.minus(v2);
        } else {
            xdir = v2.minus(v1);
        }
        if (dragPoint.y < clickPoint.y) {
            ydir = v3.minus(v2);
        } else {
            ydir = v2.minus(v3);
        }
        xsize = xdir.length();
        ysize = ydir.length();
        xdir = xdir.times(1.0 / xsize);
        ydir = ydir.times(1.0 / ysize);
        zdir = xdir.cross(ydir);
        zsize = Math.min(xsize, ysize);

        //SplineMesh obj = new SplineMesh(v, usmoothness, vsmoothness, smoothing, shape != FLAT, shape == TORUS);
        PolyMesh obj = null;
        if (templateMesh == null) {
            obj = new PolyMesh(shape, usize, vsize, xsize, ysize, zsize);
        } else {
            obj = templateMesh.duplicate();
            obj.setSize(xsize, ysize, zsize);
        }
        obj.setSmoothingMethod(smoothingMethod);
        ObjectInfo info = new ObjectInfo(obj, new CoordinateSystem(orig, zdir, ydir), "PolyMesh " + (counter++));

        UndoRecord undo = new UndoRecord(theWindow);
        undo.addCommandAtBeginning(UndoRecord.SET_SCENE_SELECTION, new Object[]{((LayoutWindow) theWindow).getSelectedIndices()});
        ((LayoutWindow) theWindow).addObject(info, undo);
        theWindow.setUndoRecord(undo);
        ((LayoutWindow) theWindow).setSelection(theScene.getNumObjects() - 1);
        theWindow.updateImage();
        theWindow.setModified();
    }

    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void iconDoubleClicked() {
        SwingUtilities.invokeLater(() -> new artofillusion.polymesh.PolyMeshToolDialog(this, edw.getFrame().getComponent()).setVisible(true));
        setHelpText();
    }

}
