/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion.procedural;

import buoy.event.RepaintEvent;
import buoy.widget.CustomWidget;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class ProcedureEditorPane extends CustomWidget {

    private final Dimension size = new Dimension(1280, 1024);
    private final Procedure model;

    private Set<Link> selectedLinks = new HashSet<>();
    private Set<Module<?>> selectedModules = new HashSet<>();

    public ProcedureEditorPane(Procedure model) {
        this.model = model;
        addEventLink(RepaintEvent.class, this, "paint");
    }

    private void paint(buoy.event.RepaintEvent event) {

    }

    /**
     * Get the preferred size at which this Widget will look best. When a
     * WidgetContainer lays out its contents, it will attempt to make this
     * Widget as close as possible to its preferred size.
     */
    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    public boolean hasSelectedLinks() {
        return !selectedLinks.isEmpty();
    }

    public boolean hasSelectedModules() {
        return !selectedModules.isEmpty();
    }
}
