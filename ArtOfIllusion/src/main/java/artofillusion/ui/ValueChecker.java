/* A ValueChecker determines whether a value is valid under an arbitrary set of criteria.
   It is used by ValueField. */

 /* Copyright (C) 2001 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.ui;

public interface ValueChecker {

    boolean isValid(double val);
}
