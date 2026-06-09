/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import buoy.event.*;
import buoy.widget.CustomWidget;

import java.awt.*;
import java.awt.geom.CubicCurve2D;

public class ProcedureEditorPane extends CustomWidget {

    public ProcedureEditorPane() {
        addEventLink(MousePressedEvent.class, this, "mousePressed");
        addEventLink(MouseReleasedEvent.class, this, "mouseReleased");
        addEventLink(MouseClickedEvent.class, this, "mouseClicked");
        addEventLink(MouseDraggedEvent.class, this, "mouseDragged");
        addEventLink(RepaintEvent.class, this, "paint");
    }



    @SuppressWarnings("java:S1144")
    private void paint(RepaintEvent ev) {
        paint(ev.getGraphics());
    }

    private void paint(Graphics2D graphics) {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    static Shape createBezierCurve(Link link) {
        int x1 = link.from.getPosition().x;
        int y1 = link.from.getPosition().y;
        int x2 = link.to.getPosition().x;
        int y2 = link.to.getPosition().y;
        float ctrlX1;
        float ctrlY1;
        float ctrlX2;
        float ctrlY2;
        if (link.from.getLocation() == IOPort.LEFT || link.from.getLocation() == IOPort.RIGHT) {
            ctrlX1 = (x2 - x1) * ProcedureEditorTheme.BEZIER_HARDNESS + x1;
            ctrlY1 = y1;
        } else {
            ctrlX1 = x1;
            ctrlY1 = (y2 - y1) * ProcedureEditorTheme.BEZIER_HARDNESS + y1;
        }
        if (link.to.getLocation() == IOPort.LEFT || link.to.getLocation() == IOPort.RIGHT) {
            ctrlX2 = (1 - ProcedureEditorTheme.BEZIER_HARDNESS) * (x2 - x1) + x1;
            ctrlY2 = y2;
        } else {
            ctrlX2 = x2;
            ctrlY2 = (1 - ProcedureEditorTheme.BEZIER_HARDNESS) * (y2 - y1) + y1;
        }
        return new CubicCurve2D.Float(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2);
    }
}
