/*
 *  Copyright (C) 2003-2005 by Peter Eastman, (C) 2005 by Francois Guillet for PolyMesh adaptation
 *  Changes copyright (C) 2023-2024 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.polymesh;

import artofillusion.Camera;
import artofillusion.MeshEditorWindow;
import artofillusion.MeshViewer;
import artofillusion.ViewerCanvas;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Mat4;
import artofillusion.math.Vec2;
import artofillusion.math.Vec3;
import artofillusion.object.Mesh;
import artofillusion.ui.MeshEditController;
import static artofillusion.ui.UIUtilities.*;
import buoy.event.WidgetMouseEvent;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;

/**
 * This manipulator simply sends two float values along x and y direction
 * when the mouse is dragged.
 * A specific icon is displayed when set and help text is custom
 * to the client tool
 */
public class MouseDragManipulator extends Manipulator {

    private ImageIcon image;
    private String helpText;
    private boolean dragging = false;
    private Point baseClick;
    private CoordinateSystem oldCoords;
    private Mat4 viewToWorld;
    private static final double DRAG_SCALE = 0.01;
    private Vec3 center;
    private Vec2 axisCenter;

    /**
     * Creates a mouse drag manipulator that displays the given image at selection center
     *
     * @param tool The tool responsible for the manipulator
     * @param view The view in which the manipulators are displayed
     * @param image The image to display at selection center
     */
    public MouseDragManipulator(AdvancedEditingTool tool, ViewerCanvas view, ImageIcon image) {
        super(tool, view, null);
        this.image = image;
        helpText = "";
    }

    @Override
    public void draw() {
        if (!active) {
            return;
        }
        MeshEditController controller = ((MeshViewer) view).getController();
        Camera cam = view.getCamera();
        AdvancedEditingTool.SelectionProperties props = tool.findSelectionProperties(view.getCamera());
        bounds = findScreenBounds(props.bounds, cam, (MeshViewer) view, controller);
        setBounds(bounds);
        if (bounds == null) {
            return;
        }
        if (!dragging) {
            center = new Vec3(props.featurePoints[0]);
        }
        axisCenter = cam.getObjectToScreen().timesXY(center);
        if (image != null) {
            view.drawImage(image.getImage(), (int) (axisCenter.x - image.getIconWidth() / 2), (int) (axisCenter.y - image.getIconHeight() / 2));
        }

    }

    @Override
    public boolean mousePressed(WidgetMouseEvent e) {
        if (!active) {
            return false;
        }
        //3D manipulators don't draw the bounds, but bounds is used to detect
        //a valid selection
        if (bounds == null) {
            return false;
        }
        //good enough for us
        if (mouseButtonTwo(e)) {
            Camera cam = view.getCamera();
            baseClick = e.getPoint();
            oldCoords = cam.getCameraCoordinates().duplicate();
            viewToWorld = cam.getViewToWorld();
            dragging = true;
            return true;
        } else if (mouseButtonOne(e)) {
            Point p = e.getPoint();
            if (image != null) {
                int x = (int) (axisCenter.x - image.getIconWidth() / 2);
                int y = (int) (axisCenter.y - image.getIconHeight() / 2);
                int width = image.getIconWidth();
                int height = image.getIconHeight();
                if (p.x < x || p.x > x + width || p.y < y || p.y > y + height) {
                    return false;
                }
            }
            dispatchEvent(new Manipulator.ManipulatorPrepareChangingEvent(this, view));
            baseClick = e.getPoint();
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mousePressedOnHandle(WidgetMouseEvent e, int handle, Vec3 pos) {
        if (!active) {
            return false;
        }
        baseClick = new Point(e.getPoint());
        dragging = true;
        dispatchEvent(new ManipulatorPrepareChangingEvent(this, view));
        return false;
    }

    @Override
    public boolean mouseDragged(WidgetMouseEvent e) {
        if (!active) {
            return false;
        }
        if (!dragging) {
            return false;
        }
        if (mouseButtonTwo(e)) {
            viewDragged(e);
            return true;
        }

        Point dragPoint = e.getPoint();
        double width = (dragPoint.x - baseClick.x) / view.getScale();
        double height = (baseClick.y - dragPoint.y) / view.getScale();
        if (((MeshViewer) view).getController().getSelectionMode() == MeshEditorWindow.FACE_MODE) {
            if (e.isShiftDown() && ((e.getModifiers() & ActionEvent.CTRL_MASK) == 0)) {
                if (Math.abs(width) > Math.abs(height)) {
                    height = 0.0;
                } else {
                    width = 0.0;
                }
            }
        } else {
            if (e.isShiftDown() && ((e.getModifiers() & ActionEvent.CTRL_MASK) == 0)) {
                height = 0.0;
            }
            if (width < 0.0) {
                width = 0.0;
            }
        }
        Vec2 drag = new Vec2(width, height);
        dispatchEvent(new ManipulatorMouseDragEvent(this, drag, view, e.isControlDown(), e.isShiftDown()));
        return true;
    }

    private void viewDragged(WidgetMouseEvent e) {
        Camera cam = view.getCamera();
        Point dragPoint = e.getPoint();
        CoordinateSystem c = oldCoords.duplicate();
        int dx, dy;
        double angle;
        Vec3 axis;

        dx = dragPoint.x - baseClick.x;
        dy = dragPoint.y - baseClick.y;
        if (e.isControlDown()) {
            axis = viewToWorld.timesDirection(Vec3.vz());
            angle = dx * DRAG_SCALE;
        } else if (e.isShiftDown()) {
            if (Math.abs(dx) > Math.abs(dy)) {
                axis = viewToWorld.timesDirection(Vec3.vy());
                angle = dx * DRAG_SCALE;
            } else {
                axis = viewToWorld.timesDirection(Vec3.vx());
                angle = -dy * DRAG_SCALE;
            }
        } else {
            axis = new Vec3(-dy * DRAG_SCALE, dx * DRAG_SCALE, 0.0);
            angle = axis.length();
            axis = axis.times(1.0 / angle);
            axis = viewToWorld.timesDirection(axis);
        }
        if (angle != 0.0) {
            c.transformCoordinates(Mat4.translation(-center.x, -center.y, -center.z));
            c.transformCoordinates(Mat4.axisRotation(axis, -angle));
            c.transformCoordinates(Mat4.translation(center.x, center.y, center.z));
            cam.setCameraCoordinates(c);
            view.repaint();
        }
    }

    @Override
    public boolean mouseReleased(WidgetMouseEvent e) {
        if (!active) {
            return false;
        }
        if (!dragging) {
            return false;
        }
        dragging = false;
        if (!mouseButtonTwo(e)) {
            dispatchEvent(new ManipulatorCompletedEvent(this, view));
        }
        return true;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public ImageIcon getImage() {
        return image;
    }

    public void setImage(ImageIcon image) {
        this.image = image;
    }

    public static class ManipulatorMouseDragEvent extends ManipulatorEvent {

        final Vec2 drag;
        final boolean ctrl;
        final boolean shift;

        public ManipulatorMouseDragEvent(Manipulator manipulator, Vec2 dr, ViewerCanvas view, boolean ctrl, boolean shift) {
            super(manipulator, view);
            drag = dr;
            this.ctrl = ctrl;
            this.shift = shift;
        }

        public Vec2 getDrag() {
            return drag;
        }

        public boolean isCtrlDown() {
            return ctrl;
        }

        public boolean isShiftDown() {
            return shift;
        }
    }
}
