package artofillusion.polymesh;/*
 *  Copyright (C) 2002-2004 by Peter Eastman, modifications for Polymesh plugin (C) 2005 Francois Guillet
 *  Changes copyright (C) 2024 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

import artofillusion.Scene;
import artofillusion.Translator;
import artofillusion.ui.Translate;
import buoy.widget.BFrame;

public class PMOBJTranslator implements Translator {
    @Override
    public String getName() {
        return Translate.text("polymesh:importWavefront");
    }

    @Override
    public void importFile(BFrame parent) {
        PMOBJImporter.importFile(parent);
    }

    @Override
    public void exportFile(BFrame parent, Scene theScene) {
        PMOBJExporter.exportFile(parent, theScene);
    }
}
