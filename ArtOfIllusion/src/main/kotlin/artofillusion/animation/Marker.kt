/* This represents a movable marker on the time axis. */

/* Copyright (C) 2001 by Peter Eastman
 *  Changes copyright (C) 2024 by Maksim Khramov
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.animation

import java.awt.Color

data class Marker(var position: Double, val name: String, val color: Color)
