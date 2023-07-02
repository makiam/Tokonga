/* Copyright (C) 1999,2000,2002,2004 by Peter Eastman
   Changes copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import java.beans.PropertyChangeEvent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class MessageDialog {
    static {
        artofillusion.ArtOfIllusion.getPreferences().addPropertyChangeListener(MessageDialog::onPropertyChange);
    }
    public static void message(String message) {

    }
    public static void error(String message) {
        error((Object)message);
    }

    public static void error(Object message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, message, "Art Of Illusion", JOptionPane.ERROR_MESSAGE));
    }
    private static void onPropertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals("language")) {
            
        }
    }
}
