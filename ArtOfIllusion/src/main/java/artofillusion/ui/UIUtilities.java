/* Copyright (C) 2004-2009 by Peter Eastman
   Changes copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import buoy.widget.*;
import buoy.event.WidgetMouseEvent;
import java.awt.*;

import static java.awt.event.MouseEvent.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * This class provides a variety of static methods for performing useful UI related operations.
 */
public class UIUtilities {

    private static Font defaultFont;
    private static int standardDialogInsets = 0;

    /**
     * Given a WindowWidget, center it in the screen.
     */
    public static void centerWindow(WindowWidget win) {
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Rectangle winBounds = win.getBounds();
        int x = (screenBounds.width - winBounds.width) / 2;
        int y = (screenBounds.height - winBounds.height) / 2;
        if (x < screenBounds.x) {
            x = screenBounds.x;
        }
        if (y < screenBounds.y) {
            y = screenBounds.y;
        }
        win.setBounds(new Rectangle(x, y, winBounds.width, winBounds.height));
    }

    /**
     * Given a BDialog, center it relative to a parent window.
     */
    public static void centerDialog(BDialog dlg, WindowWidget parent) {
        Rectangle r1 = parent.getBounds(), r2 = dlg.getBounds();
        int x = r1.x + (r1.width - r2.width) / 2;
        int y = r1.y + (r1.height - r2.height) / 2;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        dlg.setBounds(new Rectangle(x, y, r2.width, r2.height));
    }

    /**
     * Ensure that a WindowWidget fits entirely on the screen, making it smaller if necessary.
     */
    public static void fitWindowToScreen(WindowWidget win) {
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Rectangle winBounds = win.getBounds();
        if (winBounds.x < screenBounds.x) {
            winBounds.x = screenBounds.x;
        }
        if (winBounds.y < screenBounds.y) {
            winBounds.y = screenBounds.y;
        }
        if (winBounds.width > screenBounds.width) {
            winBounds.width = screenBounds.width;
        }
        if (winBounds.height > screenBounds.height) {
            winBounds.height = screenBounds.height;
        }
        win.setBounds(winBounds);
    }

    /**
     * Get the default font for the program (may be null).
     */
    public static Font getDefaultFont() {
        return defaultFont;
    }

    /**
     * Set the default font for the program (may be null).
     */
    public static void setDefaultFont(Font font) {
        defaultFont = font;
    }

    /**
     * Get the insets which should be used on all dialogs.
     */
    public static int getStandardDialogInsets() {
        return standardDialogInsets;
    }

    /**
     * Set the insets which should be used on all dialogs.
     */
    public static void setStandardDialogInsets(int pixels) {
        standardDialogInsets = pixels;
    }

    /**
     * Set up a Widget and all of its children to have the default font for the program.
     */
    public static void applyDefaultFont(Widget w) {
        if (UIUtilities.getDefaultFont() == null) {
            return;
        }
        w.setFont(UIUtilities.getDefaultFont());
        if (w instanceof WidgetContainer) {
            if (w instanceof BMenuBar) {
                return;
            }
            Collection<Widget<?>> children = ((WidgetContainer) w).getChildren();
            children.forEach(widget -> applyDefaultFont(widget));
        }
    }

    /**
     * Set up a Widget and all of its children to have the default background and text colors for the program.
     */
    public static void applyDefaultBackground(Widget w) {
        applyBackground(w, ThemeManager.getAppBackgroundColor());
        applyTextColor(w, ThemeManager.getTextColor());
    }

    /**
     * Set up a Widget and all of its children to have a specific background color.
     */
    public static void applyBackground(Widget w, Color color) {
        if (w instanceof WidgetContainer) {
            w.setBackground(color);
            Collection<Widget<?>> children = ((WidgetContainer) w).getChildren();
            children.forEach(widget -> applyBackground(widget, color));
        } else if (w instanceof BLabel) {
            w.setBackground(color);
        } else if (w instanceof BButton || w instanceof BComboBox || w instanceof BCheckBox || w instanceof BRadioButton) {
            ((JComponent) w.getComponent()).setOpaque(false);
        }
    }

    /**
     * Set up a Widget and all of its children to have a specific text color.
     */
    public static void applyTextColor(Widget w, Color color) {
        if (w instanceof WidgetContainer) {
            Collection<Widget<?>> children = ((WidgetContainer) w).getChildren();
            children.forEach(widget -> applyTextColor(widget, color));
        } else if (w instanceof BLabel || w instanceof BCheckBox || w instanceof BRadioButton) {
            w.getComponent().setForeground(color);
        }
    }

    /**
     * Given an BList, create an appropriate container for it. This involves a properly configured
     * BScrollPane, with an outline around it.
     */
    public static WidgetContainer createScrollingList(BList list) {
        BScrollPane scroll = new BScrollPane(list, BScrollPane.SCROLLBAR_AS_NEEDED, BScrollPane.SCROLLBAR_ALWAYS);
        scroll.setBackground(list.getBackground());
        scroll.setForceWidth(true);
        return BOutline.createBevelBorder(scroll, false);
    }

    /**
     * Given a Widget, find the window that contains it. If the Widget is not in a window, return null.
     */
    public static WindowWidget findWindow(Widget w) {
        if (w instanceof WindowWidget) {
            return (WindowWidget) w;
        }
        if (w == null) {
            return null;
        }
        return findWindow(w.getParent());
    }

    /**
     * Given a Widget, find its parent BFrame. If the Widget is inside a BFrame, that frame will be
     * returned. If it is inside a BDialog, this returns the dialog's parent frame. Otherwise,
     * this returns null.
     */
    public static BFrame findFrame(Widget w) {
        if (w instanceof BFrame) {
            return (BFrame) w;
        }
        if (w == null) {
            return null;
        }
        return findFrame(w.getParent());
    }

    /**
     * Break a string into lines which are short enough to easily display in a window.
     */
    public static String[] breakString(String s) {
        int lines = (s.length() / 60) + 1;
        if (lines < 2) {
            return new String[]{s};
        }
        int lineLength = s.length() / lines;
        List<String> line = new Vector<>();
        int index = 0;
        while (index + lineLength < s.length()) {
            int next = s.indexOf(' ', index + lineLength);
            if (next == -1) {
                next = s.length();
            }
            line.add(s.substring(index, next).trim());
            index = next;
        }
        if (index < s.length()) {
            line.add(s.substring(index).trim());
        }
        return line.toArray(String[]::new);
    }

    /**
     * Recursively enable or disable a container and everything inside it.
     */
    public static void setEnabled(Widget w, boolean enabled) {
        w.setEnabled(enabled);
        if (w instanceof WidgetContainer) {
            Collection<Widget<?>> children = ((WidgetContainer) w).getChildren();
            children.forEach(widget -> setEnabled(widget, enabled));
        }
    }

    /**
     * Find every Widget which is contained within a specified one, either as a direct child
     * or through multiple levels of nesting.
     */
    public static List<Widget> findAllChildren(Widget w) {
        ArrayList<Widget> list = new ArrayList<>();
        addChildrenToList(w, list);
        return list;
    }

    /**
     * Recursively add child Widgets to a list.
     */
    private static void addChildrenToList(Widget w, List<Widget> list) {
        if (w instanceof WidgetContainer) {
            Collection<Widget<?>> children = ((WidgetContainer) w).getChildren();
            for (Widget child : children) {
                list.add(child);
                addChildrenToList(child, list);
            }
        }
    }

    /**
     * Test event for specified mouse/modifier key combination.
     * This regularizes 3-button and 1-button + modifier support across
     * multiple platforms.
     */
    public static boolean mouseButtonOne(WidgetMouseEvent e) {
        int mods = e.getModifiersEx();
        return ((mods & BUTTON1_DOWN_MASK) != 0 || e.getButton() == BUTTON1)
                && ((mods & (META_DOWN_MASK | ALT_DOWN_MASK)) == 0);
    }

    public static boolean mouseButtonTwo(WidgetMouseEvent e) {
        int mods = e.getModifiersEx();
        return (((mods & BUTTON2_DOWN_MASK) != 0 || e.getButton() == BUTTON2)
                && ((mods & BUTTON1_DOWN_MASK) == 0))
                || ((mods & ALT_DOWN_MASK) != 0);
    }

    public static boolean mouseButtonThree(WidgetMouseEvent e) {
        int mods = e.getModifiersEx();
        return (((mods & BUTTON3_DOWN_MASK) != 0 || e.getButton() == BUTTON3)
                && ((mods & BUTTON1_DOWN_MASK) == 0))
                || ((mods & META_DOWN_MASK) != 0);
    }
}
