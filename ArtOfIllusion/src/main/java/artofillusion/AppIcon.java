/* Copyright (C) 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.ImageIcon;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppIcon {


  private static ImageIcon appIcon;

  public static ImageIcon getAppIcon() {
    if (appIcon == null) {
      ImageIcon icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("artofillusion/Icons/appIcon.png"));
      icon = (icon != null && icon.getIconWidth() == -1) ? null : icon;
      appIcon = icon;
    }
    return appIcon;
  }
}
