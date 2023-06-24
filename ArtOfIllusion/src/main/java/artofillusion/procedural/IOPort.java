/* Copyright (C) 2000-2012 by Peter Eastman
   Changes copyright (C) 2020-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import java.awt.*;
import lombok.Getter;
import lombok.Setter;

/**
 * This is the graphical representation of an input or output port on a module.
 */
public class IOPort {

    int x;
    int y;
    /**
     * Get the type of value for this port.
     */
    @Getter
    final int valueType;
    /**
     * Get the type of port this is (input or output).
     */
    @Getter
    final int type;
    /**
     * Get the location of this port (top, bottom, left, or right).
     */
    @Getter
    final int location;
    String[] description;
    Rectangle bounds;
    /**
     * Get the module this port belongs to.
     * Set the module this port belongs to.
     */
    @Getter @Setter
    private Module module;

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;

    public static final int NUMBER = 0;
    public static final int COLOR = 1;

    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int SIZE = 5;

    public IOPort(int valueType, int type, int location, String... description) {
        this.valueType = valueType;
        this.type = type;
        this.location = location;
        this.description = description;
    }

    /**
     * Get the port's screen position.
     */
    public Point getPosition() {
        return new Point(x, y);
    }

    /**
     * Set the port's screen position.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        if (location == TOP) {
            bounds = new Rectangle(x - SIZE, y - 1, 2 * SIZE, SIZE + 2);
        } else if (location == BOTTOM) {
            bounds = new Rectangle(x - SIZE, y - SIZE - 1, 2 * SIZE, SIZE + 2);
        }
        if (location == LEFT) {
            bounds = new Rectangle(x - 1, y - SIZE, SIZE + 2, 2 * SIZE);
        } else if (location == RIGHT) {
            bounds = new Rectangle(x - SIZE - 1, y - SIZE, SIZE + 2, 2 * SIZE);
        }
    }

    /**
     * Get the index of this port in its Module's list of input or output ports.
     */
    public int getIndex() {
        return (type == INPUT) ? module.getInputIndex(this) : module.getOutputIndex(this);
    }

    /**
     * Determine whether a point on the screen is inside this port.
     */
    public boolean contains(Point p) {
        return bounds.contains(p);
    }

    /**
     * Get the description of this port.
     */
    public String[] getDescription() {
        return description;
    }

    /**
     * Set the description of this port.
     */
    public void setDescription(String... desc) {
        description = desc;
    }

}
