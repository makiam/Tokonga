/* Copyright (C) 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.theme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.AccessLevel;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@XStreamAlias("theme")
@Getter
public class UITheme {

    @Getter(AccessLevel.NONE)
    @XStreamAlias("selectable")
    private final Boolean selectable = true;

    public Boolean isSelectable() { return selectable == null || selectable; }

    public String getName() {
        return name == null ? "" : name;
    }

    public String getAuthor() {
        return author == null ? "" : author;
    }

    @XStreamAlias("name")
    private String name;

    @XStreamAlias("author")
    private String author;

    @XStreamAlias("description")
    private String description;

    public Integer getButtonMargin() {
        return  buttonMargin == null ? 0 : buttonMargin.getValue();
    }

    public Integer getPaletteMargin() {
        return paletteMargin == null ? 0 : paletteMargin.getValue();
    }

    @XStreamAlias("buttonmargin") private final Value buttonMargin = null;

    @XStreamAlias("palettemargin") private final Value paletteMargin = null;

    public List<UIThemeColorSet> getColorSets() {
        return colorSets == null ? List.of() : colorSets;
    }

    @XStreamImplicit
    private final List<UIThemeColorSet> colorSets = new ArrayList<>();

    @XStreamAlias("button")
    private Button button;
}
