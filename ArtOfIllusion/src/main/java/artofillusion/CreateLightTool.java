/* Copyright (C) 1999-2008 by Peter Eastman
   Changes copyright (C) 2020-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * CreateLightTool is an EditingTool used for creating PointLight objects.
 */
@EditingTool.ButtonImage("light")
@EditingTool.Tooltip("createLightTool.tipText")
@EditingTool.ActivatedToolText("createLightTool.helpText")
public class CreateLightTool extends EditingTool {

    static int counter = 2;
    Point clickPoint, dragPoint;
    boolean controlDown;

    public CreateLightTool(LayoutWindow fr) {
        super(fr);
    }

    @Override
    public void mousePressed(WidgetMouseEvent e, ViewerCanvas view) {
        clickPoint = e.getPoint();
        dragPoint = null;
        controlDown = e.isControlDown();
    }

    @Override
    public void mouseDragged(WidgetMouseEvent e, ViewerCanvas view) {
        dragPoint = e.getPoint();

        // Draw lines while dragging: one to show the light direction, and if this is a spotlight,
        // two more to show the cone angle.
        GeneralPath path = new GeneralPath();
        path.append(new Line2D.Double(clickPoint.x, clickPoint.y, dragPoint.x, dragPoint.y), false);
        double dx, dy, len, angle;
        if (controlDown) {
            dx = (double) (dragPoint.x - clickPoint.x);
            dy = (double) (dragPoint.y - clickPoint.y);
            len = Math.sqrt(dx * dx + dy * dy);
            angle = (double) ((int) (Math.atan(50.0 / len) * 360.0 / Math.PI));
            theWindow.setHelpText(Translate.text("createLightTool.dragText", Double.toString(angle)));
            path.append(new Line2D.Double(clickPoint.x, clickPoint.y, dragPoint.x + (int) (dy * 50.0 / len), dragPoint.y - (int) (dx * 50.0 / len)), false);
            path.append(new Line2D.Double(clickPoint.x, clickPoint.y, dragPoint.x - (int) (dy * 50.0 / len), dragPoint.y + (int) (dx * 50.0 / len)), false);
        }
        view.drawDraggedShape(path);
    }

    @Override
    public void mouseReleased(WidgetMouseEvent e, ViewerCanvas view) {
        Scene theScene = ((LayoutWindow) theWindow).getScene();
        Camera cam = view.getCamera();
        Vec3 orig, ydir, zdir;
        Object3D obj;

        orig = cam.convertScreenToWorld(clickPoint, view.getDistToPlane());
        if (dragPoint == null) {
            ydir = new Vec3(0.0, 1.0, 0.0);
            zdir = new Vec3(0.0, 0.0, 1.0);
            obj = new PointLight(new RGBColor(1.0f, 1.0f, 1.0f), 1.0f, 0.1);
        } else {
            dragPoint = e.getPoint();
            zdir = cam.findDragVector(cam.convertScreenToWorld(clickPoint, view.getDistToPlane()),
                    dragPoint.x - clickPoint.x, dragPoint.y - clickPoint.y);
            zdir.normalize();
            ydir = cam.getViewToWorld().times(Vec3.vz());
            if (controlDown) {
                double dx, dy, len, angle;

                dx = (double) (dragPoint.x - clickPoint.x);
                dy = (double) (dragPoint.y - clickPoint.y);
                len = Math.sqrt(dx * dx + dy * dy);
                angle = (double) ((int) (Math.atan(50.0 / len) * 360.0 / Math.PI));
                obj = new SpotLight(new RGBColor(1.0f, 1.0f, 1.0f), 1.0f, angle, 0.0, 0.1);
                theWindow.setHelpText(Translate.text("createLightTool.helpText"));
            } else {
                obj = new DirectionalLight(new RGBColor(1.0f, 1.0f, 1.0f), 1.0f);
            }
        }
        ObjectInfo info = new ObjectInfo(obj, new CoordinateSystem(orig, zdir, ydir), "Light " + (counter++));
        info.addTrack(new PositionTrack(info), 0);
        info.addTrack(new RotationTrack(info), 1);
        UndoRecord undo = new UndoRecord(theWindow);
        int[] sel = ((LayoutWindow) theWindow).getSelectedIndices();
        ((LayoutWindow) theWindow).addObject(info, undo);
        undo.addCommand(UndoRecord.SET_SCENE_SELECTION, sel);
        theWindow.setUndoRecord(undo);
        ((LayoutWindow) theWindow).setSelection(theScene.getNumObjects() - 1);
        theWindow.updateImage();
    }
}
