/* Copyright (C) 1999-2007 by Peter Eastman
   Changes copyright (C) 2016 by Petri Ihalainen
   Changes copyright (C) 2017-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.animation.PositionTrack;
import artofillusion.animation.RotationTrack;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Mat4;
import artofillusion.math.Vec2;
import artofillusion.math.Vec3;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

/**
 * CreateCurveTool is an EditingTool used for creating Curve objects.
 */
@EditingTool.ButtonImage("interpCurve")
@EditingTool.Tooltip("createCurveTool.tipText")
@EditingTool.ActivatedToolText("createCurveTool.helpText")
public class CreateCurveTool extends EditingTool {

    static int counter = 1;
    private List<Vec3> clickPoint;
    private List<Float> smoothness;
    private int smoothing = Mesh.APPROXIMATING;
    private Curve theCurve;
    private CoordinateSystem coords;

    public static final int HANDLE_SIZE = 3;

    public CreateCurveTool(EditingWindow fr) {
        super(fr);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        addToScene();
    }

    @Override
    public boolean hilightSelection() {
        return (clickPoint == null);
    }

    @Override
    public void drawOverlay(ViewerCanvas view) {
        Camera cam = view.getCamera();

        if (clickPoint == null) {
            return;
        }
        if (theCurve != null) {
            Mat4 trans = cam.getWorldToScreen().times(coords.fromLocal());
            WireframeMesh mesh = theCurve.getWireframeMesh();
            Point[] p = new Point[mesh.vert.length];
            for (int i = 0; i < p.length; i++) {
                Vec2 v = trans.timesXY(mesh.vert[i]);
                p[i] = new Point((int) v.x, (int) v.y);
            }
            for (int i = 0; i < mesh.from.length; i++) {
                view.drawLine(p[mesh.from[i]], p[mesh.to[i]], ViewerCanvas.lineColor);
            }
        }

        if (clickPoint.isEmpty()) {
            return;
        }
        int lastPoint = clickPoint.size() - 1;

        Mat4 cameraWorldToScreen = cam.getWorldToScreen();

        for (int listIndex = 0; listIndex < clickPoint.size(); listIndex++) {
            Vec2 screenPos = cameraWorldToScreen.timesXY(clickPoint.get(listIndex));
            Color handleColor = (listIndex == lastPoint) ? ViewerCanvas.highlightColor : ViewerCanvas.disabledColor;
            view.drawBox((int) screenPos.x - HANDLE_SIZE / 2, (int) screenPos.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE, handleColor);
        }
    }

    @Override
    public void mousePressed(WidgetMouseEvent e, ViewerCanvas view) {
        if (clickPoint == null) {
            clickPoint = new Vector<>();
            smoothness = new Vector<>();
            view.repaint();
        } else {
            Vec3 pos = clickPoint.get(clickPoint.size()-1);
            Vec2 screenPos = view.getCamera().getWorldToScreen().timesXY(pos);
            view.drawDraggedShape(new Line2D.Float(new Point2D.Double(screenPos.x, screenPos.y), e.getPoint()));
        }
    }

    @Override
    public void mouseDragged(WidgetMouseEvent e, ViewerCanvas view) {
        if (clickPoint.isEmpty()) {
            return;
        }
        Point dragPoint = e.getPoint();
        Vec3 pos = clickPoint.get(clickPoint.size()-1);
        Vec2 screenPos = view.getCamera().getWorldToScreen().timesXY(pos);
        view.drawDraggedShape(new Line2D.Float(new Point2D.Double(screenPos.x, screenPos.y), dragPoint));
    }

    @Override
    public void mouseReleased(WidgetMouseEvent e, ViewerCanvas view) {
        Camera cam = view.getCamera();
        Point dragPoint = e.getPoint();
        Vec3[] vertex;
        Vec3 orig;
        Vec3 ydir;
        Vec3 zdir;
        float[] s;

        if (e.getClickCount() != 2) {
            clickPoint.add(cam.convertScreenToWorld(dragPoint, view.getDistToPlane()));
            smoothness.add(e.isShiftDown() ? 0.0f : 1.0f);
        }
        if (clickPoint.size() > 1) {
            // Create a new line object.  First, find all the points in world coordinates.

            vertex = new Vec3[clickPoint.size()];
            s = new float[clickPoint.size()];
            orig = new Vec3();
            for (int i = 0; i < vertex.length; i++) {
                vertex[i] = clickPoint.get(i);
                s[i] = smoothness.get(i);
                orig = orig.plus(vertex[i]);
            }
            orig = orig.times(1.0 / vertex.length);

            // Find the object's coordinate system.
            ydir = cam.getViewToWorld().timesDirection(Vec3.vy());
            zdir = cam.getViewToWorld().timesDirection(new Vec3(0.0, 0.0, -1.0));
            coords = new CoordinateSystem(orig, zdir, ydir);
            if (view.getSnapToGrid()) {
                double spacing = view.getGridSpacing() / view.getSnapToSubdivisions();
                Vec3 offset = coords.toLocal().times(vertex[0]);
                offset.x = Math.IEEEremainder(offset.x, spacing);
                offset.y = Math.IEEEremainder(offset.y, spacing);
                offset.z = Math.IEEEremainder(offset.z, spacing);
                coords.fromLocal().transformDirection(offset);
                coords.setOrigin(orig.plus(offset));
            }

            // Transform all of the vertices into the object's coordinate system.
            for (int i = 0; i < vertex.length; i++) {
                vertex[i] = coords.toLocal().times(vertex[i]);
            }
            theCurve = new Curve(vertex, s, smoothing, false);
            if (e.getClickCount() == 2) {
                theCurve.setClosed(e.isControlDown());
                addToScene();
                return;
            }
            cam.setObjectTransform(coords.fromLocal());
        }
        theWindow.updateImage();
    }

    /**
     * When the user presses Enter, add the curve to the scene.
     */
    @Override
    public void keyPressed(KeyPressedEvent e, ViewerCanvas view) {
        if (e.getKeyCode() == KeyPressedEvent.VK_ENTER && theCurve != null) {
            theCurve.setClosed(e.isControlDown());
            addToScene();
            e.consume();
        }
    }

    /**
     * Add the curve to the scene.
     */
    private void addToScene() {
        boolean addCurve = (theCurve != null);
        if (addCurve) {
            ObjectInfo info = new ObjectInfo(theCurve, coords, "Curve " + (counter++));
            info.addTrack(new PositionTrack(info), 0);
            info.addTrack(new RotationTrack(info), 1);
            UndoRecord undo = new UndoRecord(theWindow);
            int[] sel = ((LayoutWindow) theWindow).getSelectedIndices();
            ((LayoutWindow) theWindow).addObject(info, undo);
            undo.addCommand(UndoRecord.SET_SCENE_SELECTION, sel);
            theWindow.setUndoRecord(undo);
            ((LayoutWindow) theWindow).setSelection(theWindow.getScene().getNumObjects() - 1);
        }
        clickPoint = null;
        smoothness = null;
        theCurve = null;
        coords = null;
        if (addCurve) {
            theWindow.updateImage();
        }
    }

    @Override
    public void iconDoubleClicked() {
        BComboBox smoothingChoice = new BComboBox(new String[]{
            Translate.text("Interpolating"),
            Translate.text("Approximating")
        });
        if (smoothing == Mesh.INTERPOLATING) {
            smoothingChoice.setSelectedIndex(0);
        } else {
            smoothingChoice.setSelectedIndex(1);
        }
        ComponentsDialog dlg = new ComponentsDialog(theFrame, Translate.text("selectCurveSmoothing"),
                new Widget[]{smoothingChoice},
                new String[]{Translate.text("Smoothing Method")});
        if (!dlg.clickedOk()) {
            return;
        }
        if (smoothingChoice.getSelectedIndex() == 0) {
            smoothing = Mesh.INTERPOLATING;
        } else {
            smoothing = Mesh.APPROXIMATING;
        }
    }
}
