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
        return textColor.getColor();
    }

    public Color getViewerBackground() {
        return viewerBackground.getColor();
    }

    public Color getViewerLine() {
        return viewerLine.getColor();
    }

    public Color getPaletteBackground() {
        return paletteBackground.getColor();
    }

    public Color getApplicationBackground() {
        return applicationBackground.getColor();
    }

    public Color getViewerHandle() {
        return viewerHandle.getColor();
    }

    public Color getViewerHighlight() {
        return viewerHighlight.getColor();
    }

    public Color getViewerSpecialHighlight() {
        return viewerSpecialHighlight.getColor();
    }

    public Color getViewerDisabled() {
        return viewerDisabled.getColor();
    }

    public Color getViewerSurface() {
        return viewerSurface.getColor();
    }

    public Color getViewerTransparent() {
        return viewerTransparent.getColor();
    }

    public Color getViewerLowValue() {
        return viewerLowValue.getColor();
    }

    public Color getViewerHighValue() {
        return viewerHighValue.getColor();
    }

    public Color getDockableTitleColor() {
        return dockableTitleColor.getColor();
    }

    public Color getDockableBarColor1() {
        return dockableBarColor1.getColor();
    }

    public Color getDockableBarColor2() {
        return dockableBarColor2.getColor();
    }
}
