/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import buoy.widget.BSplitPane;
import buoy.widget.Widget;
import buoy.widget.WidgetContainer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;

/**
 * QuadViewPane lays out its four children in a resizable 2x2 grid, similar to the
 * multi-view layout in Blender. Three movable dividers separate the panes, and they
 * cross at a small grab handle that can be dragged horizontally and vertically at
 * the same time. Divider positions can be saved with getDividerState() and restored
 * later with setDividerState(), e.g. from user preferences. The pane can also
 * collapse to a single visible child with showSingleView() and expand again with
 * showQuadView().
 */
@Slf4j
public class QuadViewPane extends WidgetContainer<JLayeredPane> {

    private static final int DIVIDER_SIZE = 4;
    private static final int HANDLE_SIZE = 12;
    private static final int NUM_PANES = 3;

    private final BSplitPane topRow;
    private final BSplitPane bottomRow;
    private final BSplitPane spine;
    private JComponent handle;

    /**
     * Divider positions as fractions of the inner size of each split pane, in order:
     * top row, bottom row, spine. Kept in sync with the actual divider positions in
     * quad view mode, and reapplied whenever the pane is laid out or expanded again.
     */
    private final double[] dividerFractions = {0.5, 0.5, 0.5};

    /** Index of the single visible pane in single view mode, or -1 in quad view mode. */
    private int singleViewIndex = -1;

    /** True while divider positions are set programmatically, to avoid overwriting the saved state. */
    private boolean adjusting;

    /**
     * Create a QuadViewPane which displays the four children in a 2x2 grid.
     */
    public QuadViewPane(Widget topLeft,
                        Widget topRight,
                        Widget bottomLeft,
                        Widget bottomRight)
    {
        component = createComponent();
        topRow = createSplit(BSplitPane.HORIZONTAL, topLeft, topRight);
        bottomRow = createSplit(BSplitPane.HORIZONTAL, bottomLeft, bottomRight);
        spine = createSplit(BSplitPane.VERTICAL, topRow, bottomRow);
        JLayeredPane layers = getComponent();
        layers.add(spine.getComponent(), JLayeredPane.DEFAULT_LAYER);
        handle = new Handle();
        handle.setVisible(false);
        handle.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragHandle(e);
            }
        });
        layers.add(handle, JLayeredPane.PALETTE_LAYER);
        // A custom layout manager is used, so that the layout of the four panes is
        // updated whenever the pane itself is laid out. It also positions the grab
        // handle at the crossing of the dividers.
        layers.setLayout(new LayoutManager() {
            @Override
            public void addLayoutComponent(String name, Component comp) {
            }

            @Override
            public void removeLayoutComponent(Component comp) {
            }

            @Override
            public Dimension preferredLayoutSize(Container parent) {
                return new Dimension(200, 200);
            }

            @Override
            public Dimension minimumLayoutSize(Container parent) {
                return new Dimension(0, 0);
            }

            @Override
            public void layoutContainer(Container parent) {
                layoutChildren();
            }
        });
    }


    protected JLayeredPane createComponent() {
        return new JLayeredPane();
    }

    private BSplitPane createSplit(BSplitPane.Orientation orientation, Widget left, Widget right) {
        BSplitPane split = new BSplitPane(orientation, left, right);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(false);
        split.setResizeWeight(0.5);
        JSplitPane peer = split.getComponent();
        peer.setBorder(null);
        peer.setDividerSize(DIVIDER_SIZE);
        peer.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e -> dividersMoved(split));
        return split;
    }

    /**
     * Show only one of the four panes.
     *
     * @param which 0 = top left, 1 = top right, 2 = bottom left, 3 = bottom right
     */
    public void showSingleView(int which) {
        singleViewIndex = which;
        handle.setVisible(false);
        setDividerSizes(0);
        collapseToSingle();
        revalidatePane();
        repaintPane();
    }

    /** Restore the full 2x2 grid, using the divider positions active before the last showSingleView(). */
    public void showQuadView() {
        singleViewIndex = -1;
        setDividerSizes(DIVIDER_SIZE);
        handle.setVisible(true);
        applyFractions();
        positionHandle();
        revalidatePane();
        repaintPane();
    }

    /**
     * Get the divider positions as a comma separated list of fractions,
     * suitable for storing in user preferences.
     */
    public String getDividerState() {
        return dividerFractions[0] + ", " + dividerFractions[1] + ", " + dividerFractions[2];
    }

    /**
     * Restore divider positions previously obtained with getDividerState().
     * The values are applied as soon as the pane is large enough to lay them out.
     *
     * @param state comma separated fractions, or null to keep the defaults
     */
    public void setDividerState(String state) {
        if (state == null || state.isBlank()) {
            return;
        }
        try {
            String[] values = state.split(",");
            if (values.length < NUM_PANES) {
                log.atWarn().log("Ignoring malformed quad view divider state '{}'", state);
                return;
            }
            for (int i = 0; i < NUM_PANES; i++) {
                dividerFractions[i] = clampFraction(Double.parseDouble(values[i]));
            }
        } catch (NumberFormatException ex) {
            log.atWarn().setCause(ex).log("Ignoring malformed quad view divider state '{}'", state);
        }
    }

    /**
     * Position the spine and synchronize the divider positions with the layout.
     */
    @Override
    public void layoutChildren() {
        JLayeredPane layers = getComponent();
        spine.getComponent().setBounds(0, 0, layers.getWidth(), layers.getHeight());
        if (singleViewIndex >= 0) {
            collapseToSingle();
        } else {
            applyFractions();
        }
        positionHandle();
    }

    private void dividersMoved(BSplitPane split) {
        if (singleViewIndex >= 0) {
            return;
        }
        if (!adjusting) {
            // Keep the two row dividers aligned, so that dragging either one moves
            // the whole dividers cross, like the multi-view layout in Blender.
            adjusting = true;
            try {
                if (split == topRow) {
                    bottomRow.setDividerLocation(clampLocation(bottomRow, topRow.getDividerLocation()));
                } else if (split == bottomRow) {
                    topRow.setDividerLocation(clampLocation(topRow, bottomRow.getDividerLocation()));
                }
            } finally {
                adjusting = false;
            }
            dividerFractions[0] = fractionOf(topRow);
            dividerFractions[1] = fractionOf(bottomRow);
            dividerFractions[2] = fractionOf(spine);
        }
        positionHandle();
    }

    private void collapseToSingle() {
        adjusting = true;
        try {
            switch (singleViewIndex) {
                case 0: // Top left
                    spine.setDividerLocation(1.0);
                    topRow.setDividerLocation(0.0);
                    break;
                case 1: // Top right
                    spine.setDividerLocation(1.0);
                    topRow.setDividerLocation(1.0);
                    break;
                case 2: // Bottom left
                    spine.setDividerLocation(0.0);
                    bottomRow.setDividerLocation(0.0);
                    break;
                case 3: // Bottom right
                    spine.setDividerLocation(0.0);
                    bottomRow.setDividerLocation(1.0);
                    break;
                default:
                    log.atError().log("Invalid single view index {}", singleViewIndex);
                    break;
            }
        } finally {
            adjusting = false;
        }
    }

    private void dragHandle(MouseEvent e) {
        if (singleViewIndex >= 0) {
            return;
        }
        Point p = SwingUtilities.convertPoint(handle, e.getPoint(), spine.getComponent());
        adjusting = true;
        try {
            topRow.setDividerLocation(clampLocation(topRow, p.x));
            bottomRow.setDividerLocation(clampLocation(bottomRow, p.x));
            spine.setDividerLocation(clampLocation(spine, p.y));
        } finally {
            adjusting = false;
        }
        dividersMoved(topRow);
    }

    private void applyFractions() {
        adjusting = true;
        try {
            setFraction(topRow, dividerFractions[0]);
            setFraction(bottomRow, dividerFractions[1]);
            setFraction(spine, dividerFractions[2]);
        } finally {
            adjusting = false;
        }
    }

    private void setFraction(BSplitPane split, double fraction) {
        split.setDividerLocation((int) Math.round(fraction * innerSize(split)));
    }

    private double fractionOf(BSplitPane split) {
        int size = innerSize(split);
        return size > 0 ? clampFraction(split.getDividerLocation() / (double) size) : 0.5;
    }

    private int clampLocation(BSplitPane split, int location) {
        return Math.max(0, Math.min(innerSize(split), location));
    }

    private static int innerSize(BSplitPane split) {
        int size = (split.getOrientation() == BSplitPane.HORIZONTAL ? split.getComponent().getWidth() : split.getComponent().getHeight());
        return Math.max(0, size - split.getComponent().getDividerSize());
    }

    private static double clampFraction(double fraction) {
        return Math.max(0.05, Math.min(0.95, fraction));
    }

    private void setDividerSizes(int size) {
        topRow.getComponent().setDividerSize(size);
        bottomRow.getComponent().setDividerSize(size);
        spine.getComponent().setDividerSize(size);
    }

    private void positionHandle() {
        if (singleViewIndex >= 0 || spine.getComponent().getWidth() == 0) {
            handle.setVisible(false);
            return;
        }
        int x = topRow.getDividerLocation();
        int y = spine.getDividerLocation();
        handle.setBounds(x - HANDLE_SIZE / 2, y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
        handle.setVisible(true);
    }

    private void revalidatePane() {
        getComponent().revalidate();
    }

    private void repaintPane() {
        getComponent().repaint();
    }

    /**
     * The grab handle at the crossing of the dividers. Dragging it resizes all four
     * panes at once, keeping the dividers crossed.
     */
    private static class Handle extends JComponent {

        Handle() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Color color = UIManager.getColor("Label.foreground");
            g.setColor(color != null ? color : Color.DARK_GRAY);
            int mid = HANDLE_SIZE / 2;
            g.drawLine(mid, 2, mid, HANDLE_SIZE - 3);
            g.drawLine(2, mid, HANDLE_SIZE - 3, mid);
        }
    }

    @Override
    public void removeAll() {
        topRow.removeAll();
        bottomRow.removeAll();
    }

    @Override
    public void remove(Widget widget) {
        topRow.remove(widget);
        bottomRow.remove(widget);
    }

    @Override
    public int getChildCount() {
        return topRow.getChildCount() + bottomRow.getChildCount();
    }

    @Override
    public Collection<Widget<?>> getChildren() {
        List<Widget<?>> children = new ArrayList<>();
        children.addAll(topRow.getChildren());
        children.addAll(bottomRow.getChildren());
        return children;
    }
}
