/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.material;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author MaksK
 */
@Slf4j
class ProcedureView extends JComponent implements MouseListener, MouseWheelListener {
    private static final Color NETWORK_GRID_COLOR = new Color(69, 69, 69);
    private static final Color NETWORK_BACKGROUND_COLOR = new Color(85, 85, 85);
    private static final int GRID_CELL_SIZE = 48;
    private static final int GRID_OFFSET = 6;

    @Getter
    private double viewX, viewY, viewScale = 1;

    public ProcedureView() {
        this.setFocusable(true);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        var focusListener = new ViewFocusListener();
        this.addFocusListener(focusListener);
        SwingUtilities.invokeLater(() ->  {
            SwingUtilities.getWindowAncestor(this).addWindowFocusListener(focusListener);
        });
        this.requestFocusInWindow();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Draw background
        g2.setColor(ProcedureView.NETWORK_BACKGROUND_COLOR);
        g2.fill(graphics.getClipBounds());

        // Draw grid
        paintGrid(g2);
    }

    private void paintGrid(Graphics2D g) {
        g.setColor(ProcedureView.NETWORK_GRID_COLOR);

        int gridCellSize = (int) Math.round(GRID_CELL_SIZE * getViewScale());
        int gridOffset = (int) Math.round(GRID_OFFSET * getViewScale());
        if (gridCellSize < 10) return;

        int transformOffsetX = (int) (getViewX() % gridCellSize);
        int transformOffsetY = (int) (getViewY() % gridCellSize);

        for (int y = -gridCellSize; y < getHeight() + gridCellSize; y += gridCellSize) {
            g.drawLine(0, y - gridOffset + transformOffsetY, getWidth(), y - gridOffset + transformOffsetY);
        }
        for (int x = -gridCellSize; x < getWidth() + gridCellSize; x += gridCellSize) {
            g.drawLine(x - gridOffset + transformOffsetX, 0, x - gridOffset + transformOffsetX, getHeight());
        }
    }

    private class ViewFocusListener implements WindowFocusListener, FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            log.info("Focus gained");
        }

        @Override
        public void focusLost(FocusEvent e) {
            log.info("Focus lost");
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            log.info("Window focus gained");
        }

        @Override
        public void windowLostFocus(WindowEvent e) {
            log.info("Window focus lost");
        }
    }
}
