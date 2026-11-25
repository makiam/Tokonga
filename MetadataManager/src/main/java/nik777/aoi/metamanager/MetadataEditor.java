/* Copyright (C) 2009 by Nik TJ
   Changes copyright (C) 2026 by Maksim Khramov


   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package nik777.aoi.metamanager;

import artofillusion.LayoutWindow;
import buoy.widget.Widget;

public interface MetadataEditor {
  String getName();
  Class getObjectType();
  Widget getWidget(Object paramObject, LayoutWindow paramLayoutWindow, boolean paramBoolean);
}


