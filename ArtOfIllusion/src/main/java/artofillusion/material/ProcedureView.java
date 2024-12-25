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

/**
 *
 * @author MaksK
 */
class ProcedureView extends JComponent implements MouseListener, MouseWheelListener {
    private static final Color NETWORK_GRID_COLOR = new Color(69, 69, 69);
    private static final Color NETWORK_BACKGROUND_COLOR = new Color(85, 85, 85);
    private static final int GRID_CELL_SIZE = 48;
    private static final int GRID_OFFSET = 6;

    public ProcedureView() {
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
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
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}
