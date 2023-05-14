/* Copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.ui;

import artofillusion.math.RGBColor;
import buoy.widget.Widget;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.awt.event.MouseEvent.BUTTON1;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;


/**
 *
 * @author mkhramov
 */
public class ColorSampleWidget extends Widget {

    private RGBColor color;

    public RGBColor getColor() {
        return color;
    }

    private String title;

    public void setColor(RGBColor color) {
        this.color = color;
        this.component.setForeground(color.getColor());
    }


    public ColorSampleWidget(RGBColor color, String title, int width, int height, boolean editable) {
        Dimension size = new Dimension(width, height);
        this.component = new ColorSampleComponent();
        this.component.setName(title);
        this.color = color.duplicate();
        this.component.setPreferredSize(size);
        this.component.setMaximumSize(size);
        this.component.setForeground(color.getColor());
        if (editable) {
            this.title = title;
            this.component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ColorSampleWidget.this.mouseClicked(e);
                }

            });
        }
    }

    public ColorSampleWidget(RGBColor color, String title, int width, int height) {
        this(color, title, width, height, true);
    }

    public ColorSampleWidget(RGBColor color, int width, int height) {
        this(color, "", width, height, false);
    }

    public ColorSampleWidget(RGBColor color, String title) {
        this(color, title, 50, 30);
    }

    public ColorSampleWidget(RGBColor color) {
        this(color, "", 50, 30, false);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void mouseClicked(MouseEvent e) {
        if (BUTTON1 != e.getButton()) {
            return;
        }
        new ColorChooser(UIUtilities.findFrame(this), title, color);
        this.component.setForeground(color.getColor());
    }

    private static class ColorSampleComponent extends JPanel {

        private static final Border border = new BevelBorder(BevelBorder.LOWERED);
        private static final long serialVersionUID = 1L;

        public ColorSampleComponent() {
            super.setBorder(border);
        }

        @Override
        public void setBorder(Border border) {
            //Suppress change border
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            //super.paintComponent(graphics);
            graphics.setColor(getForeground());
            Insets insets = border.getBorderInsets(this);
            graphics.fillRect(insets.left, insets.top, this.getWidth() - (insets.left + insets.right), this.getHeight() - (insets.bottom + insets.top));
        }
    }
}
