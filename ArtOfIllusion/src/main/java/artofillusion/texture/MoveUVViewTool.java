/* Copyright (C) 2003-2007 by Peter Eastman
   Changes copyright (C) 2022-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.texture;

import artofillusion.*;
import artofillusion.ui.*;
import buoy.event.*;
import java.awt.*;

/**
 * MoveUVViewTool is an EditingTool used for moving the viewpoint in the UV editing window.
 */
@EditingTool.ButtonImage("moveView")
@EditingTool.Tooltip("moveViewTool.tipText")
@EditingTool.ActivatedToolText("moveViewTool.helpText")
public class MoveUVViewTool extends EditingTool {

    private Point clickPoint;
    private boolean controlDown;
    private double minU;
    private double maxU;
    private double minV;
    private double maxV;
    private int vWidth;
    private int vHeight;

    public MoveUVViewTool(EditingWindow fr) {
        super(fr);
    }

    @Override
    public void mousePressed(WidgetMouseEvent e, ViewerCanvas view) {
        UVMappingViewer uvView = (UVMappingViewer) view;

        controlDown = e.isControlDown();
        clickPoint = e.getPoint();

        minU = uvView.getMinU();
        maxU = uvView.getMaxU();
        minV = uvView.getMinV();
        maxV = uvView.getMaxV();

        Rectangle d = uvView.getBounds();
        vWidth = d.width;
        vHeight = d.height;
    }

    @Override
    public void mouseDragged(WidgetMouseEvent e, ViewerCanvas view) {
        Point dragPoint = e.getPoint();
        UVMappingViewer uvView = (UVMappingViewer) view;

        int dx = dragPoint.x - clickPoint.x;
        int dy = dragPoint.y - clickPoint.y;

        if (controlDown) {
            double factor = Math.pow(1.01, dy);
            double midu = (minU + maxU) / 2;
            double midv = (minV + maxV) / 2;

            double newminu = ((minU - midu) / factor) + midu;
            double newmaxu = ((maxU - midu) / factor) + midu;
            double newminv = ((minV - midv) / factor) + midv;
            double newmaxv = ((maxV - midv) / factor) + midv;

            uvView.setParameters(newminu, newmaxu, newminv, newmaxv);
        } else {
            if (e.isShiftDown()) {
                if (Math.abs(dx) > Math.abs(dy)) {
                    dy = 0;
                } else {
                    dx = 0;
                }
            }
            double du = (minU - maxU) * dx / vWidth;
            double dv = (maxV - minV) * dy / vHeight;
            uvView.setParameters(minU + du, maxU + du, minV + dv, maxV + dv);
        }
    }

    @Override
    public void mouseReleased(WidgetMouseEvent e, ViewerCanvas view) {
        mouseDragged(e, view);
        theWindow.updateImage();
    }
}
