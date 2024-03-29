/* This is a listener interface for objects which need to be notified when a list of
 * objects changes. */

 /* Copyright (C) 2001 by Peter Eastman
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion;

public interface ListChangeListener {

    void itemAdded(int index, Object obj);

    void itemRemoved(int index, Object obj);

    void itemChanged(int index, Object obj);
}
