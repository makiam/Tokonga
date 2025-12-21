/* Copyright (C) 2016 - 2020 by Petri Ihalainen
   Changes copyright (C) 2023-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.view;

import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClickedPointFinder {

    private double[] bary;
    private Mat4 modelToScreen;
    private int w, h;
    private Vec3 cameraOrigin, cameraZ;
    private boolean perspective;

    public ClickedPointFinder() {
    }


  /**
     Return the closest point on a surface of an object that is found under
     a given point on the view. If no object surface is found, then a point at
     ViewerCanvas.distToPlane is returned.

     This works only for objects, that can produce a RenderingMesh.
   */

  public Vec3 newPoint(ViewerCanvas view, Point point) {
        Vec3 clickedPoint = view.getCamera().convertScreenToWorld(point, view.getDistToPlane()); // defaultpoint if nothing is there
        Vec3 pointOnTriangle;
        boolean inSpace = true;
        bary = new double[3];

        cameraOrigin = view.getCamera().getCameraCoordinates().getOrigin();
        cameraZ = view.getCamera().getCameraCoordinates().getZDirection();
        perspective = view.isPerspective();
        w = view.getBounds().width;
        h = view.getBounds().height;

        // ToScreen matrices produce the actual pixel coordinates on the ViewerCanvas.
        if ((view instanceof ObjectViewer viewer) && !(viewer.getUseWorldCoords())) {
            modelToScreen = view.getCamera().getObjectToScreen();
        } else {
            modelToScreen = view.getCamera().getWorldToScreen();
        }


        boolean[] hideTriangle;
        Vec3[] corner3D = new Vec3[3];
        Vec2[] corner2D = new Vec2[3];


        for (ObjectInfo info: renderableObjects(view)) {

            RenderingMesh surface = info.getPreviewMesh();
            if (view instanceof ObjectViewer viewer && (!viewer.getSceneVisible() || info == viewer.thisObjectInScene)) {
                hideTriangle = view.getHiddenRenderingTriangles();
            } else {
                hideTriangle = null;
            }
            Mat4 toContext = contextTransform(view, info);

            for (int t = 0; t < surface.triangle.length; t++) {
                if (hideTriangle != null && hideTriangle[t]) {
                    continue;
                }

                corner3D[0] = new Vec3(toContext.times(surface.vert[surface.triangle[t].v1]));
                corner3D[1] = new Vec3(toContext.times(surface.vert[surface.triangle[t].v2]));
                corner3D[2] = new Vec3(toContext.times(surface.vert[surface.triangle[t].v3]));

                corner2D[0] = modelToScreen.timesXY(corner3D[0]);
                corner2D[1] = modelToScreen.timesXY(corner3D[1]);
                corner2D[2] = modelToScreen.timesXY(corner3D[2]);

                if (onTriangle(corner2D, point)) {
                    (corner3D[0] = corner3D[0]).scale(bary[0]);
                    (corner3D[1] = corner3D[1]).scale(bary[1]);
                    (corner3D[2] = corner3D[2]).scale(bary[2]);

                    pointOnTriangle = new Vec3(corner3D[0].plus(corner3D[1].plus(corner3D[2])));

                    if (onView(pointOnTriangle)) // Needed for perspective mode
                    {
                        if (inSpace) {
                            clickedPoint = pointOnTriangle; // The first one found --> point no longer "in space".
                            inSpace = false;
                        } else if (closer(pointOnTriangle, clickedPoint) && onFrontSide(pointOnTriangle)) {
                            clickedPoint = pointOnTriangle;
                        }
                    }
                }
            }
        }
        return clickedPoint;
    }

    private boolean closer(Vec3 p1, Vec3 p2) {
        return p1.minus(cameraOrigin).dot(cameraZ) < p2.minus(cameraOrigin).dot(cameraZ);
    }

    private boolean onFrontSide(Vec3 p) {
        if (!perspective) // Everything is 'in front' of the camera in parallel mode
        {
            return true;
        } else return p.minus(cameraOrigin).dot(cameraZ) > 0.0;
    }

    private boolean onTriangle(Vec2[] corner2D, Point point) {
        // The sum of barycentric coordinates is always 1.0.
        // If the point is outside, some of those will be negative.

        bary = TriangleMath.baryCoordinates(corner2D, point);
        return bary[0] >= 0.0 && bary[1] >= 0.0 && +bary[2] >= 0.0;
    }

    private boolean onView(Vec3 p3D) {
        Vec2 p2D = modelToScreen.timesXY(p3D);
        return (p2D.x > 0 && p2D.x < w && p2D.y > 0 && p2D.y < h);
    }


    private List<ObjectInfo> renderableObjects(ViewerCanvas view) {
        List<ObjectInfo> renderable = new ArrayList<>();


        if (view instanceof SceneViewer || (view instanceof ObjectViewer viewer1 && viewer1.getSceneVisible())) {
            Scene scene = view.getScene();
            scene.getObjects().forEach((ObjectInfo oi) -> {
                if (oi.isVisible() && oi.getObject().canSetTexture()) {
                    renderable.add(oi);
                }
            });
        } else if (view instanceof ObjectViewer viewer) {
            renderable.add(viewer.getController().getObject());
        } else if (view instanceof ObjectPreviewCanvas canvas) {
            renderable.add(canvas.getObject());
        }

        return renderable;
    }

    private Mat4 contextTransform(ViewerCanvas view, ObjectInfo info) {
        Mat4 t;

        if (view instanceof ObjectViewer viewer) {
            if (viewer.getUseWorldCoords()) {
                if (viewer.getSceneVisible()) {
                    t = info.getCoords().fromLocal();
                } else {
                    t = viewer.getDisplayCoordinates().fromLocal();
                    t = t.times(info.getCoords().toLocal());
                }
            } else if (viewer.getSceneVisible()) {
                viewer.setUseWorldCoords(true);
                t = viewer.getDisplayCoordinates().toLocal();
                t = t.times(info.getCoords().fromLocal());
                viewer.setUseWorldCoords(false);
            } else {
                t = Mat4.identity();
            }
        } else {
            t = info.getCoords().fromLocal();
        }

        return t;
    }
}
