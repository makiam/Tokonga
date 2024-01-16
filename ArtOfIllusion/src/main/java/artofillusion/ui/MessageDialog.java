/* Copyright (C) 1999,2000,2002,2004 by Peter Eastman
   Changes copyright (C) 2023-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public final class MessageDialog {

    private static final String defaultTitle = "Art Of Illusion";
    private Component owner;
    private String title = null;

    public static MessageDialog create() {
        return new MessageDialog().withTitle(defaultTitle);
    }

    public MessageDialog withTitle(String title) {
        this.title = title;
        return this;
    }
    public MessageDialog withOwner(Component owner) {
        this.owner = owner;
        return this;
    }

    private static final Icon icon;
    static {
        icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("artofillusion/Icons/appIcon.png"));
    }

    public static void message(String message) {
        JOptionPane.showMessageDialog(null, message, defaultTitle, JOptionPane.PLAIN_MESSAGE, icon);
    }

    public static void info(String message) {
        JOptionPane.showMessageDialog(null, message, defaultTitle, JOptionPane.INFORMATION_MESSAGE, icon);
    }

    public void info(Object message) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.INFORMATION_MESSAGE, icon);
    }

    public void error(String message) {
        error((Object)Translate.text(message));
    }

    public void warning(Object message) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.WARNING_MESSAGE, icon);
    }

    public void error(Object message) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.ERROR_MESSAGE, icon);
    }

    public int option(String message) {
        String[] options = getOptions();
        return JOptionPane.showOptionDialog(owner, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, icon, options, options[1]);
    }

    public static String[] getOptions() {
        return new String[]{Translate.text("button.ok"), Translate.text("button.cancel")};
    }
}
