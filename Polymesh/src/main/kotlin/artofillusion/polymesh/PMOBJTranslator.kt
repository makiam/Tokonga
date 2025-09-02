/*
 *  Copyright (C) 2002-2004 by Peter Eastman, modifications for Polymesh plugin (C) 2005 Francois Guillet
 *  Changes copyright (C) 2024 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.polymesh

import artofillusion.Scene
import artofillusion.Translator
import artofillusion.ui.Translate
import buoy.widget.BFrame

/**
 * PMOBJTranslator is a Translator which imports and exports OBJ files to/from
 * Polymeshes.
 *
 * @author Francois Guillet
 * @created 13 juin 2005
 */
class PMOBJTranslator : Translator {
    /**
     * Gets the name attribute of the PMOBJTranslator object
     *
     * @return The name value
     */
    override fun getName(): String? = Translate.text("polymesh:importWavefront")

    override fun importFile(parent: BFrame?) = PMOBJImporter.importFile(parent)

    override fun exportFile(parent: BFrame?, theScene: Scene) = PMOBJExporter.exportFile(parent, theScene)
}
