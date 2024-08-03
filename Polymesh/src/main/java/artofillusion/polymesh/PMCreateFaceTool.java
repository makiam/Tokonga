/* Copyright (C) 1999-2004 by Peter Eastman
   Changes copyright (C) 2023 by Maksim Khramov
This program is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.UndoRecord;
import artofillusion.ViewerCanvas;
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
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

/**
 * PMKnifeTool is an EditingTool used fto divide edges of PolyMesh objects.
 */
@EditingTool.ButtonImage("polymesh:createface")
@EditingTool.Tooltip("polymesh:createFaceTool.tipText")
@EditingTool.ActivatedToolText("polymesh:createFaceTool.helpText")
public class PMCreateFaceTool extends EditingTool {

    private final List<Vec3> clickPoints = new Vector<>();
    private final MeshEditController controller;
    private ViewerCanvas canvas;
    private int from = -1;
    private int to = -1;
    private Vec3 fromPoint;
    private Vec3[] pr;

    public PMCreateFaceTool(EditingWindow fr, MeshEditController controller) {
        super(fr);
        this.controller = controller;
    }

    @Override
    public void activate() {
        super.activate();
        clickPoints.clear();
        from = to = -1;
        fromPoint = null;
    }

    @Override
    public void mouseReleased(WidgetMouseEvent ev, ViewerCanvas view) {
        Point e = ev.getPoint();
        PolyMesh mesh, viewMesh;
        viewMesh = mesh = (PolyMesh) controller.getObject().object;
        int[] invVertTable = null;
        int length = mesh.getVertices().length;
        if (mesh.getMirrorState() != PolyMesh.NO_MIRROR) {
            viewMesh = mesh.getMirroredMesh();
            invVertTable = mesh.getInvMirroredVerts();
            length = invVertTable.length;
        }

        MeshVertex[] v;
        boolean project = (controller instanceof PolyMeshEditorWindow ? ((PolyMeshEditorWindow) controller).getProjectOntoSurface() : false);
        if (project && mesh.getSmoothingMethod() != PolyMesh.NO_SMOOTHING && viewMesh.getSubdividedMesh() != null) {
            v = viewMesh.getSubdividedMesh().getVertices();
        } else {
            v = mesh.getVertices();
        }
        pr = new Vec3[length];
        for (int i = 0; i < length; ++i) {
            pr[i] = v[i].r;
        }
        canvas = view;
        double closestz = Double.MAX_VALUE;
        int which = -1;
        for (int i = 0; i < pr.length; i++) {
            Vec2 p = canvas.getCamera().getObjectToScreen().timesXY(pr[i]);
            Point ps = new Point((int) p.x, (int) p.y);
            if (e.x < ps.x - PolyMeshViewer.HANDLE_SIZE / 2 || e.x > ps.x + PolyMeshViewer.HANDLE_SIZE / 2
                    || e.y < ps.y - PolyMeshViewer.HANDLE_SIZE / 2 || e.y > ps.y + PolyMeshViewer.HANDLE_SIZE / 2) {
                continue;
            }
            double z = canvas.getCamera().getObjectToView().timesZ(pr[i]);
            if (z < closestz) {
                if (invVertTable == null) {
                    which = i;
                } else {
                    which = invVertTable[i];
                }
                closestz = z;
            }
        }
        if (clickPoints.isEmpty() && from == -1 && which != -1) {
            from = which;
            fromPoint = pr[from];
            return;
        }
        if (canvas == view && from != -1) {
            if (which == -1) {
                if (clickPoints.isEmpty()) {
                    clickPoints.add(fromPoint = get3DPoint(fromPoint, e));
                    return;
                }
                for (int i = 0; i < clickPoints.size(); i++) {
                    Vec3 cpv = clickPoints.get(i);
                    Vec2 ps = canvas.getCamera().getObjectToScreen().timesXY(cpv);
                    if (e.x < ps.x - PolyMeshViewer.HANDLE_SIZE / 2 || e.x > ps.x + PolyMeshViewer.HANDLE_SIZE / 2
                            || e.y < ps.y - PolyMeshViewer.HANDLE_SIZE / 2 || e.y > ps.y + PolyMeshViewer.HANDLE_SIZE / 2) {
                        continue;
                    }
                    if ((ev.getModifiers() & ActionEvent.CTRL_MASK) != 0) {
                        clickPoints.remove(i);
                        theWindow.updateImage();
                        return;
                    } else if (i == 0) {
                        addFace();
                        return;
                    }
                }
                clickPoints.add(fromPoint = get3DPoint(fromPoint, e));
            } else {
                to = which;
                addFace();
            }
        }
    }

    @Override
    public void keyPressed(KeyPressedEvent e, ViewerCanvas view) {
        if (!(canvas == view)) {
            return;
        }
        int key = e.getKeyCode();
        if (from != -1) {
            switch (key) {
                case KeyPressedEvent.VK_ESCAPE:
                    from = -1;
                    clickPoints.clear();
                    theWindow.updateImage();
                    break;
                case KeyPressedEvent.VK_W:
                    if (clickPoints.size() > 0) {
                        clickPoints.remove(clickPoints.size() - 1);
                    }
                    theWindow.updateImage();
                    break;
            }
        }
    }

    private void addFace() {
        if (clickPoints.isEmpty()) {
            return;
        }
        Vec3[] newPoints = clickPoints.toArray(new Vec3[0]);

        PolyMesh mesh = (PolyMesh) controller.getObject().object;
        PolyMesh origMesh = (PolyMesh) mesh.duplicate();
        if (to == -1) {
            mesh.addStandaloneFace(newPoints);
        } else {
            if (!mesh.addFaceFromPoints(from, to, newPoints)) {
                to = -1;
                return;
            }

        }
        controller.setMesh(mesh);
        theWindow.setUndoRecord(new UndoRecord(theWindow, false, UndoRecord.COPY_OBJECT, mesh, origMesh));
        clickPoints.clear();
        fromPoint = null;
        from = to = -1;
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
        if (canvas == view && from != -1) {
            Vec2 p = view.getCamera().getObjectToScreen().timesXY(pr[from]);
            Point pf = new Point((int) p.x, (int) p.y);
            view.drawBox(pf.x - PolyMeshViewer.HANDLE_SIZE / 2, pf.y - PolyMeshViewer.HANDLE_SIZE / 2, PolyMeshViewer.HANDLE_SIZE, PolyMeshViewer.HANDLE_SIZE, Color.red);
            if (clickPoints.size() > 0) {
                Vec3 v = clickPoints.get(0);
                Vec2 vp = canvas.getCamera().getObjectToScreen().timesXY(v);
                Point vpp = new Point((int) Math.round(vp.x), (int) Math.round(vp.y));
                Point vppt;
                view.drawLine(pf, vpp, Color.black);
                for (int k = 0; k < clickPoints.size() - 1; ++k) {
                    v = clickPoints.get(k);
                    vp = canvas.getCamera().getObjectToScreen().timesXY(v);
                    vpp = new Point((int) Math.round(vp.x), (int) Math.round(vp.y));
                    v = clickPoints.get(k + 1);
                    vp = canvas.getCamera().getObjectToScreen().timesXY(v);
                    vppt = new Point((int) Math.round(vp.x), (int) Math.round(vp.y));
                    view.drawLine(vpp, vppt, Color.black);
                }
                for (int k = 0; k < clickPoints.size(); ++k) {
                    v = clickPoints.get(k);
                    vp = canvas.getCamera().getObjectToScreen().timesXY(v);
                    vpp = new Point((int) Math.round(vp.x), (int) Math.round(vp.y));
                    view.drawBox(vpp.x - PolyMeshViewer.HANDLE_SIZE / 2, vpp.y - PolyMeshViewer.HANDLE_SIZE / 2, PolyMeshViewer.HANDLE_SIZE, PolyMeshViewer.HANDLE_SIZE, Color.red);
                }
            }
        }
    }
}
