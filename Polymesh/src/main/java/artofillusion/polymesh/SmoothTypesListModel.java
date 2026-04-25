/* Copyright (C) 2026 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.ui.Translate;

import javax.swing.*;

public class SmoothTypesListModel extends DefaultComboBoxModel<String> {
    SmoothTypesListModel() {
        addElement(Translate.text("menu.none"));
        addElement(Translate.text("menu.shading"));
        addElement(Translate.text("menu.approximating"));
        addElement(Translate.text("menu.interpolating"));
    }
}
