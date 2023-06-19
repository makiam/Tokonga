/* Copyright (C) 2004-2007 by Peter Eastman
   Changes copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import javax.swing.SwingUtilities;

/**
 * TitleWindow displays a window containing the title and credits.
 */
public class TitleWindow {
    private final SplashScreen splash;

    public TitleWindow() {
        splash = new SplashScreen();
    }

    public TitleWindow setDisposable() {
        splash.setDisposable();
        return this;
    }

    public TitleWindow show() {
        SwingUtilities.invokeLater(() -> splash.setVisible(true));
        return this;
    }
    public void dispose() {
        splash.dispose();
    }
}
