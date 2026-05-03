/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.theme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Getter;

import java.awt.*;

@XStreamAlias("colorset")
@Getter
public final class UIThemeColorSet {
    private static final Color dc = new Color(0,0,0);

    @XStreamAsAttribute @XStreamAlias("name")
    private String name = "";

    @XStreamAlias("applicationbackground")
    private ColorSetColor applicationBackground;

    @XStreamAlias("viewerbackground")
    private ColorSetColor viewerBackground;

    @XStreamAlias("viewerline")
    private ColorSetColor viewerLine;

    @XStreamAlias("palettebackground")
    private ColorSetColor paletteBackground;

    @XStreamAlias("textcolor")
    private ColorSetColor textColor;

    @XStreamAlias("viewerhandle")
    private ColorSetColor viewerHandle;
    @XStreamAlias("viewerhighlight")
    private ColorSetColor viewerHighlight;
    @XStreamAlias("viewerspecialhighlight")
    private ColorSetColor viewerSpecialHighlight;
    @XStreamAlias("viewerdisabled")
    private ColorSetColor viewerDisabled;
    @XStreamAlias("viewersurface")
    private ColorSetColor viewerSurface;
    @XStreamAlias("viewertransparent")
    private ColorSetColor viewerTransparent;
    @XStreamAlias("viewerlowvalue")
    private ColorSetColor viewerLowValue;
    @XStreamAlias("viewerhighvalue")
    private ColorSetColor viewerHighValue;

    @XStreamAlias("dockabletitlecolor")
    private ColorSetColor dockableTitleColor;
    @XStreamAlias("dockablebarcolor1")
    private ColorSetColor dockableBarColor1;
    @XStreamAlias("dockablebarcolor2")
    private ColorSetColor dockableBarColor2;


    public Color getTextColor() {
        return textColor == null ? dc : textColor.getColor();
    }

    public Color getViewerBackground() {
        return viewerBackground == null ? dc : viewerBackground.getColor();
    }

    public Color getViewerLine() {
        return viewerLine == null ? dc : viewerLine.getColor();
    }

    public Color getPaletteBackground() {
        return paletteBackground == null ? dc : paletteBackground.getColor();
    }

    public Color getApplicationBackground() {
        return applicationBackground == null ? dc : applicationBackground.getColor();
    }

    public Color getViewerHandle() {
        return viewerHandle == null ? dc : viewerHandle.getColor();
    }

    public Color getViewerHighlight() {
        return viewerHighlight == null ? dc : viewerHighlight.getColor();
    }

    public Color getViewerSpecialHighlight() {
        return viewerSpecialHighlight == null ? dc : viewerSpecialHighlight.getColor();
    }

    public Color getViewerDisabled() {
        return viewerDisabled == null ? dc : viewerDisabled.getColor();
    }

    public Color getViewerSurface() {
        return viewerSurface == null ? dc : viewerSurface.getColor();
    }

    public Color getViewerTransparent() {
        return viewerTransparent == null ? dc : viewerTransparent.getColor();
    }

    public Color getViewerLowValue() {
        return viewerLowValue == null ? dc : viewerLowValue.getColor();
    }

    public Color getViewerHighValue() {
        return viewerHighValue == null ? dc : viewerHighValue.getColor();
    }

    public Color getDockableTitleColor() {
        return dockableTitleColor == null ? dc : dockableTitleColor.getColor();
    }

    public Color getDockableBarColor1() {
        return dockableBarColor1 == null ? dc : dockableBarColor1.getColor();
    }

    public Color getDockableBarColor2() {
        return dockableBarColor2 == null ? dc : dockableBarColor2.getColor();
    }

}
