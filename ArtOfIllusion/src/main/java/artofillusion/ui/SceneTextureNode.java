/* Copyright (C) 2025-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import artofillusion.texture.Texture;

import javax.swing.tree.DefaultMutableTreeNode;

public class SceneTextureNode extends DefaultMutableTreeNode {

    public SceneTextureNode(Texture texture) {
        super(texture, false);
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public Texture getUserObject() {
        return (Texture) super.getUserObject();
    }

    @Override
    public String toString() {
        return "Texture: " + getUserObject().getName();
    }
}
