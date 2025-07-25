/*
 *  Changes copyright (C) 2023-2025 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.polymesh;

import artofillusion.UndoRecord;
import artofillusion.ViewerCanvas;
import artofillusion.math.Mat4;
import artofillusion.math.Vec2;
import artofillusion.math.Vec3;
import artofillusion.object.MeshVertex;
import artofillusion.ui.EditingTool;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.MeshEditController;
import buoy.event.KeyPressedEvent;
import buoy.event.WidgetMouseEvent;
import java.awt.Color;
import java.awt.Point;

import java.util.List;
import java.util.Vector;
import lombok.extern.slf4j.Slf4j;

/**
 * PMExtrudeCurveTool lets the user extrude faces along a curve.
 */
@Slf4j
@EditingTool.ButtonImage("polymesh:extrudecurve")
@EditingTool.Tooltip("polymesh:extrudeCurveTool.tipText")
@EditingTool.ActivatedToolText("polymesh:extrudeCurveTool.helpText")
public class PMExtrudeCurveTool extends EditingTool {

    private final List<CurvePoint> clickPoints;
    private PolyMesh orMesh;
    private boolean[] orSel;
    private final MeshEditController controller;
    private ViewerCanvas canvas;
    private Vec3 fromPoint, currentPoint;
    boolean constantSize;
    int dragging;
    boolean previewMode = true;
    private static final int HANDLE_SIZE = 7;

    public PMExtrudeCurveTool(EditingWindow fr, MeshEditController controller) {
        super(fr);
        clickPoints = new Vector<>();
        fromPoint = null;
        this.controller = controller;
    }

    @Override
    public void activate() {
        super.activate();
        clickPoints.clear();
        fromPoint = null;
    }

    @Override
    public void mousePressed(WidgetMouseEvent e, ViewerCanvas view) {
        dragging = -1;
        if (clickPoints.isEmpty()) {
            return;
        }
        if (canvas == view) {
            for (int i = 0; i < clickPoints.size(); i++) {
                if (!(clickPoints.get(i)).clickedOnto(e, view)) {
                    continue;
                }
                dragging = i + 1;
                if (e.isControlDown()) {
                    clickPoints.remove(i);
                    if (previewMode) {
                        extrudeFaces(false);
                    }
                    theWindow.updateImage();
                    dragging = 0;
                }
                return;
            }
        }

    }

    @Override
    public void mouseDragged(WidgetMouseEvent ev, ViewerCanvas view) {
        if (dragging < 1) {
            return;
        }
        CurvePoint cp = clickPoints.get(dragging - 1);
        if (dragging == 1) {
            cp.mouseDragged(fromPoint, ev.getPoint());
        } else {
            Vec3 p = clickPoints.get(dragging - 2).position;
            cp.mouseDragged(p, ev.getPoint());
        }
        if (previewMode) {
            extrudeFaces(false);
        }
        theWindow.updateImage();
    }

    @Override
    public void mouseReleased(WidgetMouseEvent ev, ViewerCanvas view) {
        Point e = ev.getPoint();
        canvas = view;
        if (clickPoints.isEmpty() && fromPoint == null) {
            fromPoint = getInitialPoint();
            if (fromPoint == null) {
                return;
            }
            clickPoints.add(new CurvePoint(currentPoint = get3DPoint(fromPoint, e), 1.0));
            constantSize = ev.isShiftDown();
            PolyMesh mesh = (PolyMesh) controller.getObject().getGeometry();
            orMesh = mesh.duplicate();
            orSel = controller.getSelection();
            if (!constantSize) {
                computeScales();
            }
            if (previewMode) {
                extrudeFaces(false);
            }
            return;
        }
        if (canvas == view) {
            if (dragging > -1) {
                dragging = -1;
                return;
            }
            if (ev.isControlDown()) {
                doCancel();
                return;
            }
            clickPoints.add(new CurvePoint(currentPoint = get3DPoint(currentPoint, e), 1.0));
            if (!constantSize) {
                computeScales();
            }
            if (previewMode) {
                extrudeFaces(false);
            }
            theWindow.updateImage();
        }
    }

    private Vec3 getInitialPoint() {
        if (clickPoints.isEmpty()) {
            orSel = controller.getSelection();
            orMesh = (PolyMesh) controller.getObject().getGeometry();
        }
        return getInitialPoint(orSel, orMesh);
    }

    private Vec3 getInitialPoint(boolean[] sel, PolyMesh mesh) {
        Vec3 fromPoint;
        if (controller.getSelectionMode() != PolyMeshEditorWindow.FACE_MODE) {
            return null;
        }
        boolean nonZeroSel = false;
        for (int i = 0; i < sel.length; i++) {
            nonZeroSel |= sel[i];
            if (nonZeroSel) {
                break;
            }
        }
        if (!nonZeroSel) {
            return null;
        }
        PolyMesh.Wface[] faces = mesh.getFaces();
        MeshVertex[] verts = mesh.getVertices();
        fromPoint = new Vec3();
        int count = 0;
        for (int i = 0; i < faces.length; i++) {
            if (!sel[i]) {
                continue;
            }
            ++count;
            Vec3 p = new Vec3();
            int[] fe = mesh.getFaceVertices(faces[i]);
            for (int j = 0; j < fe.length; j++) {
                p.add(verts[fe[j]].r);
            }
            p.scale(1.0 / (float) fe.length);
            fromPoint.add(p);
        }
        fromPoint.scale(1.0 / (float) count);
        return fromPoint;
    }

    private void applyRotationMatrix(boolean[] sel, Mat4 m, PolyMesh mesh) {
        PolyMesh.Wface[] faces = mesh.getFaces();
        MeshVertex[] verts = mesh.getVertices();
        boolean[] rotated = new boolean[verts.length];
        for (int i = 0; i < faces.length; i++) {
            if (!sel[i]) {
                continue;
            }
            int[] fe = mesh.getFaceVertices(faces[i]);
            for (int j = 0; j < fe.length; j++) {
                if (!rotated[fe[j]]) {
                    verts[fe[j]].r = m.times(verts[fe[j]].r);
                    rotated[fe[j]] = true;
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyPressedEvent e, ViewerCanvas view) {
        if (!(canvas == view)) {
            return;
        }
        int key = e.getKeyCode();
        if (fromPoint != null) {
            switch (key) {
                case KeyPressedEvent.VK_ESCAPE:
                    log.debug("Escape...");
                    doCancel();
                    break;
                case KeyPressedEvent.VK_W:
                    if (clickPoints.size() > 0) {
                        clickPoints.remove(clickPoints.size() - 1);
                    }
                    theWindow.updateImage();
                    break;
                case KeyPressedEvent.VK_ENTER:
                    extrudeFaces(true);
                    break;
                case KeyPressedEvent.VK_J:
                    previewMode = !previewMode;
                    if (previewMode && clickPoints.size() != 0) {
                        extrudeFaces(false);
                    } else if (clickPoints.size() != 0) {
                        controller.setMesh(orMesh.duplicate());
                        controller.setSelection(orSel);
                    }
                    theWindow.updateImage();
                    break;
            }
        }
    }

    private void doCancel() {
        if (previewMode && clickPoints.size() != 0) {
            controller.setMesh(orMesh.duplicate());
            controller.setSelection(orSel);
        }
        fromPoint = null;
        clickPoints.clear();
        theWindow.updateImage();
    }

    private void computeScales() {
        double length = 0;
        double cumul = 0;
        Vec3 previous = fromPoint;
        for (int i = 0; i < clickPoints.size(); i++) {
            length += clickPoints.get(i).position.minus(previous).length();
            previous = clickPoints.get(i).position;
        }
        if (length < 0.005) {
            clickPoints.forEach(cp -> cp.amplitude = 1.0);
        } else {
            previous = fromPoint;
            for (int i = 0; i < clickPoints.size(); i++) {
                cumul += clickPoints.get(i).position.minus(previous).length();
                clickPoints.get(i).amplitude = 1.0 - cumul / length;
                previous = clickPoints.get(i).position;
            }
        }
    }

    private void extrudeFaces(boolean done) {
        boolean[] sel = orSel;
        if (clickPoints.isEmpty()) {
            return;
        }
        Vec3 previous;
        PolyMesh mesh = orMesh.duplicate();
        Vec3 extdir, nextdir, normal;
        double scale;
        double angle;
        previous = fromPoint;
        double size;
        for (int i = 0; i < clickPoints.size(); i++) {
            Vec3[] normals = mesh.getFaceNormals();
            normal = new Vec3();
            for (int j = 0; j < normals.length; j++) {
                if (sel[j]) {
                    normal.add(normals[j]);
                }
            }
            normal.normalize();
            extdir = clickPoints.get(i).position.minus(previous);
            scale = extdir.length();
            extdir.normalize();
            angle = 0;
            if (i < clickPoints.size() - 1) {
                nextdir = clickPoints.get(i + 1).position.minus(clickPoints.get(i).position);
                nextdir.normalize();
                nextdir.add(extdir);
                nextdir.normalize();
            } else {
                nextdir = extdir;
            }
            angle = Math.acos(extdir.dot(normal));
            nextdir = normal.cross(nextdir);
            if (nextdir.length() < 0.005) {
                nextdir = null;
            } else {
                nextdir.normalize();
            }
            mesh.extrudeRegion(sel, scale, extdir);
            boolean[] newSel = new boolean[mesh.getFaces().length];
            for (int j = 0; j < sel.length; j++) {
                newSel[j] = sel[j];
            }
            sel = newSel;
            previous = clickPoints.get(i).position;
            size = clickPoints.get(i).amplitude;
            if (nextdir != null) {
                Vec3 trans = getInitialPoint(sel, mesh);
                Mat4 m = Mat4.translation(-trans.x, -trans.y, -trans.z);
                if (size > 1e-6) {
                    m = Mat4.scale(size, size, size).times(m);
                }
                m = Mat4.axisRotation(nextdir, angle).times(m);
                m = Mat4.translation(trans.x, trans.y, trans.z).times(m);
                applyRotationMatrix(sel, m, mesh);
                if (size < 1e-6 && i == clickPoints.size() - 1) {
                    mesh.collapseFaces(sel);
                    sel = new boolean[mesh.getFaces().length];
                }
            }
        }
        controller.setMesh(mesh);
        controller.setSelection(sel);
        if (done) {
            fromPoint = null;
            clickPoints.clear();
            theWindow.setUndoRecord(new UndoRecord(theWindow, false, UndoRecord.COPY_OBJECT, mesh, orMesh));
        }
    }

    Vec3 get3DPoint(Vec3 ref, Point clickPoint) {
        Vec2 pf = canvas.getCamera().getObjectToScreen().timesXY(ref);
        return ref.plus(canvas.getCamera().findDragVector(ref, (int) Math.round(clickPoint.x - pf.x), (int) Math.round(clickPoint.y - pf.y)));
    }

    /**
     * Draw any graphics that this tool overlays on top of the view.
     */
    @Override
    public void drawOverlay(ViewerCanvas view) {
        Vec3 aPoint = getInitialPoint();
        if (aPoint == null) {
            return;
        }
        Vec2 p = view.getCamera().getObjectToScreen().timesXY(aPoint);
        Point pf = new Point((int) p.x, (int) p.y);
        view.drawBox(pf.x - HANDLE_SIZE / 2, pf.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE, Color.red);
        if (canvas == view) {
            if (clickPoints.size() > 0) {
                Vec3 v = clickPoints.get(0).position;
                Vec2 vp = canvas.getCamera().getObjectToScreen().timesXY(v);
                Point vpp = new Point((int) Math.round(vp.x), (int) Math.round(vp.y));
                Point vppt;
                view.drawLine(pf, vpp, Color.black);
                for (int k = 0; k < clickPoints.size() - 1; ++k) {
                    v = clickPoints.get(k).position;
                    vp = canvas.getCamera().getObjectToScreen().timesXY(v);
                    vpp = new Point((int) Math.round(vp.x), (int) Math.round(vp.y));
                    v = clickPoints.get(k + 1).position;
                    vp = canvas.getCamera().getObjectToScreen().timesXY(v);
                    vppt = new Point((int) Math.round(vp.x), (int) Math.round(vp.y));
                    view.drawLine(vpp, vppt, Color.black);
                }
                for (int k = 0; k < clickPoints.size(); ++k) {
                    clickPoints.get(k).draw(view);
                    //v = ((CurvePoint)clickPoints.get(k)).position;
                    //vp = canvas.getCamera().getObjectToScreen().timesXY( v );
                    //vpp = new Point( (int)Math.round(vp.x), (int)Math.round(vp.y) );
                    //view.drawBox( vpp.x - HANDLE_SIZE/2, vpp.y - HANDLE_SIZE/2, HANDLE_SIZE, HANDLE_SIZE, Color.red);
                }
            }
        }
    }

    private class CurvePoint {

        Vec3 position;
        double amplitude;
        double devAngle;
        short dragging;
        static final int SCALE_HEIGHT = 30;
        static final int NONE = -1;
        static final int MOVING = 0;
        static final int HANDLE_UP = 1;
        static final int HANDLE_DOWN = 2;

        public CurvePoint(Vec3 position, double amplitude) {
            this.position = position;
            amplitude = 1;

            this.amplitude = amplitude;
        }

        public void draw(ViewerCanvas view) {
            Vec2 p = view.getCamera().getObjectToScreen().timesXY(position);
            Point pf = new Point((int) p.x, (int) p.y);
            view.drawBox(pf.x - HANDLE_SIZE / 2, pf.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE, Color.red);
            double scaleFactor = view.getScale();
            Point handleup = new Point(pf.x, (int) Math.round(pf.y + amplitude * SCALE_HEIGHT));
            Point handledown = new Point(pf.x, (int) Math.round(pf.y - amplitude * SCALE_HEIGHT));
            //Shape dot = new Ellipse2D.Float(handleup.x,handleup.y,8,8);
            //view.fillShape(dot, Color.blue);
            view.drawBox(handleup.x - HANDLE_SIZE / 2, handleup.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE, Color.blue);
            view.drawBox(handledown.x - HANDLE_SIZE / 2, handledown.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE, Color.blue);
            view.drawLine(pf, handleup, Color.black);
            view.drawLine(pf, handledown, Color.black);
        }

        public boolean clickedOnto(WidgetMouseEvent ev, ViewerCanvas view) {
            Point e = ev.getPoint();
            Vec2 ps = canvas.getCamera().getObjectToScreen().timesXY(position);
            if (!(e.x < ps.x - HANDLE_SIZE / 2 || e.x > ps.x + HANDLE_SIZE / 2
                    || e.y < ps.y - HANDLE_SIZE / 2 || e.y > ps.y + HANDLE_SIZE / 2)) {
                if (ev.isShiftDown()) {
                    dragging = NONE;
                    amplitude = 1.0;
                } else {
                    dragging = MOVING;
                }
                return true;
            }
            Point hpt = new Point((int) Math.round(ps.x), (int) Math.round(ps.y + amplitude * SCALE_HEIGHT));
            if (!(e.x < hpt.x - HANDLE_SIZE / 2 || e.x > hpt.x + HANDLE_SIZE / 2
                    || e.y < hpt.y - HANDLE_SIZE / 2 || e.y > hpt.y + HANDLE_SIZE / 2)) {
                dragging = HANDLE_UP;
                return true;
            }
            hpt = new Point((int) Math.round(ps.x), (int) Math.round(ps.y - amplitude * SCALE_HEIGHT));
            if (!(e.x < hpt.x - HANDLE_SIZE / 2 || e.x > hpt.x + HANDLE_SIZE / 2
                    || e.y < hpt.y - HANDLE_SIZE / 2 || e.y > hpt.y + HANDLE_SIZE / 2)) {
                dragging = HANDLE_DOWN;
                return true;
            }
            dragging = NONE;
            return false;
        }

        public void mouseDragged(Vec3 p, Point e) {
            Vec2 pt;
            switch (dragging) {
                case MOVING:
                    position = get3DPoint(p, e);
                    break;
                case HANDLE_UP:
                    pt = canvas.getCamera().getObjectToScreen().timesXY(position);
                    amplitude = (e.y - pt.y) / SCALE_HEIGHT;
                    if (amplitude < 0) {
                        amplitude = 0;
                    }
                    break;
                case HANDLE_DOWN:
                    pt = canvas.getCamera().getObjectToScreen().timesXY(position);
                    amplitude = (-e.y + pt.y) / SCALE_HEIGHT;
                    if (amplitude < 0) {
                        amplitude = 0;
                    }
                    break;
                default:
                    break;

            }

        }
    }
}
