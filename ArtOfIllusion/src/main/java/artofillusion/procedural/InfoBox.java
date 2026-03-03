/* This is a box used for displaying information about ports when the user clicks
   on them. */

 /* Copyright (C) 2000 by Peter Eastman
   Changes copyright (C) 2023-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.procedural;

import java.awt.*;

public class InfoBox {

    private String[] text = new String[0];
    final Rectangle bounds = new Rectangle();

    static final Font defaultFont = Font.decode("Serif");
    static final FontMetrics defaultMetrics = Toolkit.getDefaultToolkit().getFontMetrics(defaultFont);

    /* Set the text for the box. */
    public void setText(String[] text) {
        this.text = text;
        bounds.height = text.length * (defaultMetrics.getMaxAscent() + defaultMetrics.getMaxDescent()) + 10;
        bounds.width = 10;
        for(String s: text) bounds.width = Math.max(bounds.width, defaultMetrics.stringWidth(s) + 10);
    }

    /* Set the position of the box. */
    public void setPosition(int x, int y) {
        bounds.x = x;
        bounds.y = y;
    }

    /* Get the boundary rectangle. */
    public Rectangle getBounds() {
        return bounds;
    }

    /* Draw the box. */
    public void draw(Graphics g) {
        g.setColor(Color.yellow);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(Color.black);
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setFont(defaultFont);
        for (int i = 0; i < text.length; i++) {
            g.drawString(text[i],
                    bounds.x
                    + (bounds.width - defaultMetrics.stringWidth(text[i])) / 2,
                    bounds.y + (bounds.height * (i + 1))
                    / (text.length + 1) + defaultMetrics.getAscent() / 2
            );
        }
    }
}
