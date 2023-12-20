/* Copyright (C) 2001-2008 by Peter Eastman
   Changes copyright (C) 2022-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.*;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Mat4;
import artofillusion.math.Vec3;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.widget.*;

/**
 * The lathe tool creates new objects by rotating a curve around an axis.
 */
public class LatheTool implements ModellingTool {

    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;
    public static final int AXIS_THROUGH_ENDS = 3;

    /* Get the text that appear as the menu item.*/
    @Override
    public String getName() {
        return Translate.text("menu.lathe");
    }

    /* See whether an appropriate object is selected and either display an error
     message, or bring up the extrude window. */
    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void commandSelected(LayoutWindow window) {
        Scene scene = window.getScene();
        int[] selection = window.getSelectedIndices();

        if (selection.length == 1) {
            ObjectInfo obj = scene.getObject(selection[0]);
            if (obj.getObject() instanceof Curve) {
                new LatheDialog(window, obj);
                return;
            }
        }
        new BStandardDialog("", Translate.text("Tools:lathe.tool.message"), BStandardDialog.INFORMATION).showMessageDialog(window.getFrame());
    }

    /**
     * Create a mesh by rotating a curve around an axis.
     *
     * @param curve the Curve to lathe
     * @param latheAxis the axis around which to rotate the curve. This should be one of the constants
     * defined by this class: X_AXIS, Y_AXIS, Z_AXIS, or AXIS_THROUGH_ENDS.
     * @param segments the number of segments the lathed mesh should include. The larger this number,
     * the higher the resolution of the resulting mesh.
     * @param angle the total angle by which to rotate the curve, in degrees.
     * @param latheRadius the radius by which to offset the rotation axis from the curve before performing
     * the lathe operation.
     * @return the mesh created by lathing the curve
     */
    protected static Mesh latheCurve(Curve curve, int latheAxis, int segments, double angle, double latheRadius) {
        MeshVertex[] vert = curve.getVertices();
        Vec3 axis, radius, center = new Vec3();
        double angleStep = angle * Math.PI / (segments * 180.0);
        boolean closed = false;

        if (angle == 360.0) {
            closed = true;
        } else {
            segments++;
        }

        // Determine the rotation axis.
        if (latheAxis == LatheTool.X_AXIS) {
            axis = Vec3.vx();
            radius = Vec3.vy();
        } else if (latheAxis == LatheTool.Y_AXIS) {
            axis = Vec3.vy();
            radius = Vec3.vx();
        } else if (latheAxis == LatheTool.Z_AXIS) {
            axis = Vec3.vz();
            radius = Vec3.vx();
        } else if (latheAxis == LatheTool.AXIS_THROUGH_ENDS) {
            axis = vert[0].r.minus(vert[vert.length - 1].r);
            axis.normalize();
            center.set(vert[0].r);
            radius = new Vec3();
            for (int i = 0; i < vert.length; i++) {
                radius.add(vert[i].r);
            }
            radius.scale(1.0 / vert.length);
            radius.subtract(center);
            radius.subtract(axis.times(axis.dot(center)));
            radius.normalize();
        } else {
            throw new IllegalArgumentException("Illegal value specified for lathe axis");
        }
        center.add(radius.times(-latheRadius));

        // Calculate the vertices of the lathed surface.
        Vec3[][] v = new Vec3[segments][vert.length];
        Vec3 cm = new Vec3();
        CoordinateSystem coords = new CoordinateSystem(center, axis, radius);
        for (int i = 0; i < segments; i++) {
            Mat4 m = coords.fromLocal().times(Mat4.zrotation(i * angleStep)).times(coords.toLocal());
            for (int j = 0; j < vert.length; j++) {
                v[i][j] = m.times(vert[j].r);
                cm.add(v[i][j]);
            }
        }

        // Create the arrays of smoothness values.
        float[] usmooth = new float[segments], vsmooth = new float[vert.length];
        float[] s = curve.getSmoothness();
        for (int i = 0; i < segments; i++) {
            usmooth[i] = 1.0f;
        }
        for (int i = 0; i < s.length; i++) {
            vsmooth[i] = s[i];
        }
        int smoothMethod = curve.getSmoothingMethod();
        if (smoothMethod == Mesh.NO_SMOOTHING) {
            for (int i = 0; i < s.length; i++) {
                vsmooth[i] = 0.0f;
            }
            smoothMethod = Mesh.APPROXIMATING;
        } else {
            for (int i = 0; i < s.length; i++) {
                vsmooth[i] = s[i];
            }
        }

        // Center it.
        cm.scale(1.0 / (segments * vert.length));
        for (int i = 0; i < v.length; i++) {
            for (int j = 0; j < v[i].length; j++) {
                v[i][j].subtract(cm);
            }
        }
        SplineMesh mesh = new SplineMesh(v, usmooth, vsmooth, smoothMethod, closed, curve.isClosed());
        mesh.makeRightSideOut();
        return mesh;
    }
}
