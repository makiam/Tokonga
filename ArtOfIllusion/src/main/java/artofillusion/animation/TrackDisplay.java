/* Copyright (C) 2001 by Peter Eastman
 * 
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

/**
 * This interface describes a component which appears in the Score for displaying the
 * contents of Tracks.
 */
public interface TrackDisplay {

    /**
     * Set the starting time to display.
     */
    void setStartTime(double time);

    /**
     * Set the number of pixels per unit time.
     */
    void setScale(double s);

    /**
     * Set the number of subdivisions per unit time.
     */
    void setSubdivisions(int s);

    /**
     * Set the y offset (for vertically scrolling the panel).
     */
    void setYOffset(int offset);

    /**
     * Add a marker to the display.
     */
    void addMarker(Marker m);

    /**
     * Set the mode (select-and-move or scroll-and-scale) for this display.
     */
    void setMode(int m);
}
