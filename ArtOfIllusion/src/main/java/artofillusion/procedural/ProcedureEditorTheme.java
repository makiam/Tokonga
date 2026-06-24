/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcedureEditorTheme {
    protected static final Stroke contourStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final Color darkLinkColor = new Color(64, 64, 64);
    static final Color blueLinkColor = new Color(40, 40, 255);
    static final Color selectedLinkColor = new Color(255, 165, 0);
    static final Color outputBackgroundColor = new Color(210, 210, 240);
    static final Color outlineColor = new Color(110, 110, 160);
    static final Color selectedColor = new Color(255, 60, 60);
    static final float BEZIER_HARDNESS = 0.5f; //increase hardness to a have a more pronounced shape
    static final Stroke normal = new BasicStroke();
    static final Stroke bold = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
}
