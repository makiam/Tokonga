package artofillusion.procedural;

import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

public final class ProcedureEditorCanvas extends JComponent implements MouseWheelListener {

    @Setter
    private Procedure model;

    private double panX = 0, panY = 0, zoom = 1.0;

    public ProcedureEditorCanvas() {
        setBackground(ProcedureEditorTheme.BACKGROUND_COLOR);
        setDoubleBuffered(true);
        setFocusable(true);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        var oldZoom = zoom;
        var factor = e.getWheelRotation() > 0 ? 1 / 1.1 : 1.1;
        zoom = Math.max(ProcedureEditorTheme.MIN_ZOOM, Math.min(ProcedureEditorTheme.MAX_ZOOM, zoom * factor));

        var mx = e.getX();
        var my = e.getY();
        panX = mx - (mx - panX) * (zoom / oldZoom);
        panY = my - (my - panY) * (zoom / oldZoom);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawGrid(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        var gridSize = 40.0;
        while (gridSize * zoom < 15) gridSize *= 4;
        var tl = screenToWorld(new Point(0, 0));
        var br = screenToWorld(new Point(getWidth(), getHeight()));
        var sx = (int) (Math.floor(tl.getX() / gridSize) * gridSize);
        var ex = (int) (Math.ceil(br.getX() / gridSize) * gridSize);
        var sy = (int) (Math.floor(tl.getY() / gridSize) * gridSize);
        var ey = (int) (Math.ceil(br.getY() / gridSize) * gridSize);

        g2d.setColor(ProcedureEditorTheme.GRID_COLOR);
        for (var x = sx; x <= ex; x += gridSize) {
            var p1 = worldToScreen(new Point2D.Double(x, tl.getY()));
            var p2 = worldToScreen(new Point2D.Double(x, br.getY()));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        for (var y = sy; y <= ey; y += gridSize) {
            var p1 = worldToScreen(new Point2D.Double(tl.getX(), y));
            var p2 = worldToScreen(new Point2D.Double(br.getX(), y));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        var origin = worldToScreen(new Point2D.Double(0, 0));
        g2d.setColor(new Color(200, 50, 50, 100));
        g2d.drawLine(0, origin.y, getWidth(), origin.y);
        g2d.setColor(new Color(50, 200, 50, 100));
        g2d.drawLine(origin.x, 0, origin.x, getHeight());
    }

    public Point2D screenToWorld(Point screen) {
        return new Point2D.Double((screen.x - panX) / zoom, (screen.y - panY) / zoom);
    }

    public Point worldToScreen(Point2D world) {
        return new Point((int)(world.getX() * zoom + panX), (int)(world.getY() * zoom + panY));
    }
}
