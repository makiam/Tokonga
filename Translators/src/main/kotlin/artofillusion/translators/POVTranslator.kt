/* Copyright (C) 2002-2004 by Norbert Krieg and Peter Eastman
   Changes copyright (C) 2024 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.translators

import artofillusion.Scene
import artofillusion.Translator
import buoy.widget.BFrame

/**
 * POVTranslator is a Translator which (imports and) exports Povray files.
 */
class POVTranslator : Translator {
    override fun getName() = "Povray V3.5 (.pov)"

    override fun canImport() = false

    override fun importFile(parent: BFrame?) {}

    override fun exportFile(parent: BFrame?, theScene: Scene) = POVExporter.exportFile(parent, theScene)
}
