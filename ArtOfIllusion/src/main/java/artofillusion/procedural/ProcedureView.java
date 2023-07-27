package artofillusion.procedural;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProcedureView extends JComponent  {

    private static final Color NETWORK_BACKGROUND_COLOR = new Color(69, 69, 69);
    private static final Color NETWORK_GRID_COLOR = new Color(85, 85, 85);
    private static final int GRID_CELL_SIZE = 48;
    private static final int GRID_OFFSET = 6;

    @Getter
    private double viewX, viewY, viewScale = 1;

    public ProcedureView() {
        super();

    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Draw background
        g2.setColor(ProcedureView.NETWORK_BACKGROUND_COLOR);
        g2.fill(graphics.getClipBounds());
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
}
