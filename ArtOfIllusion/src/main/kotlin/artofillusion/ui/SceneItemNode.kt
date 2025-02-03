/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui

import artofillusion.`object`.ObjectInfo
import javax.swing.tree.DefaultMutableTreeNode

class SceneItemNode(userObject: ObjectInfo) : DefaultMutableTreeNode(userObject, true) {
    init {
        val items = userObject.children
        if (items.isEmpty()) this.allowsChildren = false
        items.forEach { this.add(SceneItemNode(it)) }
    }

    val userObject: ObjectInfo?
        get() = super.getUserObject() as ObjectInfo?

    override fun toString(): String {
        return "Scene Object: ${(userObject as ObjectInfo)!!.name}"
    }
}
