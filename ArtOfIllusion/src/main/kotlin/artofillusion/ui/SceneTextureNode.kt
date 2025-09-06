/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui


import artofillusion.texture.Texture
import javax.swing.tree.DefaultMutableTreeNode

class SceneTextureNode(userObject: Texture) : DefaultMutableTreeNode(userObject, true) {

    override fun getAllowsChildren(): Boolean {
        return false
    }

    override fun getUserObject(): Texture? = super.getUserObject() as Texture?

    override fun toString(): String = "Texture: ${(userObject as Texture).name}"
}
