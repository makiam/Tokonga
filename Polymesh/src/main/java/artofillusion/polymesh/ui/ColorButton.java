/*
 *  Copyright (C) 2007 by Francois Guillet
 *  Changes copyright (C) 2023-2024 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.polymesh.ui;

import artofillusion.ui.Translate;
import buoy.event.RepaintEvent;
import buoy.widget.BButton;
import buoy.widget.BColorChooser;
import buoy.widget.CustomWidget;
import buoy.widget.RowContainer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class ColorButton extends RowContainer {

    public class ColorWidget extends CustomWidget {

        private Color color;

        public ColorWidget(Color color) {
            setOpaque(true);
            addEventLink(RepaintEvent.class, this, "doPaint");
            this.color = color;
        }

        @SuppressWarnings("unused")
        private void doPaint(RepaintEvent ev) {
            Graphics2D g = ev.getGraphics();
            Rectangle r = new Rectangle(5, 5, 30, 20);
            if (isEnabled()) {
                g.setColor(color);
            } else {
                g.setColor(Color.LIGHT_GRAY);
            }
            g.fill(r);
        }

        public void setColor(Color color) {
            this.color = color;
            repaint();
        }

        public Color getColor() {
            return color;
        }

        @Override
        public Dimension getPreferredSize() {
            return size;
        }

        @Override
        public Dimension getMinimumSize() {
            return size;
        }

        @Override
        public Dimension getMaximumSize() {
            return size;
        }
    }

    private static final Dimension size = new Dimension(40, 30);

    final BButton button;
    final ColorWidget colorWidget;

    public ColorButton(Color color) {
        super();
        add(colorWidget = new ColorWidget(color));
        add(button = Translate.button("polymesh:setColor", event -> doButtonClicked()));
    }

    @SuppressWarnings("unused")
    private void doButtonClicked() {
        BColorChooser colorChooser = new BColorChooser(colorWidget.color, Translate.text("polymesh:chooseColor"));
        if (colorChooser.showDialog(this)) {
            colorWidget.color = colorChooser.getColor();
            colorWidget.repaint();
        }
    }

    public void setColor(Color color) {
        colorWidget.setColor(color);
    }

    public Color getColor() {
        return colorWidget.color;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled);
        colorWidget.setEnabled(enabled);
    }
}
