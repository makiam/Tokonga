/* Copyright (C) 2000-2004 by Peter Eastman
   Changes copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.texture;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.image.*;
import buoy.widget.*;
import java.io.*;

/**
 * LayeredTexture represents a texture which is composed of other textures layered on top
 * of each other. This class serves mainly as a placeholder - most of the real work is
 * done by LayeredMapping.
 */
public class LayeredTexture extends Texture {

    private LayeredMapping mapping;

    public LayeredTexture(Object3D obj) {
        mapping = new LayeredMapping(obj, this);
        name = "";
    }

    public LayeredTexture(LayeredMapping map) {
        mapping = map;
        mapping.theTexture = this;
        name = "";
    }

    /**
     * Determine whether this Texture uses the specified image.
     */
    @Override
    public boolean usesImage(ImageMap image) {

        for (Texture texture : mapping.getLayers()) {
            if (texture.usesImage(image)) {
                return true;
            }
        }
        return false;
    }

    /**
     * For the average properties, use the average properties of the bottom layer.
     */
    @Override
    public void getAverageSpec(TextureSpec spec, double time, double[] param) {
        mapping.getAverageSpec(spec, time, param);
    }

    /**
     * Every LayeredTexture has a unique LayeredMapping object associated with it.
     */
    @Override
    public TextureMapping getDefaultMapping(Object3D object) {
        return mapping;
    }

    /**
     * Set the mapping for this texture.
     */
    public void setMapping(LayeredMapping map) {
        mapping = map;
    }

    /**
     * There shouldn't ever be a reason to call this.
     */
    @Override
    public Texture duplicate() {
        return null;
    }

    /**
     * Determine whether this texture has a non-zero value anywhere for a particular component.
     *
     * @param component the texture component to check for (one of the *_COMPONENT constants)
     */
    @Override
    public boolean hasComponent(int component) {
        Texture[] tex = mapping.getLayers();

        if (component == TRANSPARENT_COLOR_COMPONENT) {
            if (tex.length == 0) {
                return true;
            }
            for (int i = 0; i < tex.length; i++) {
                if (tex[i].hasComponent(component)) {
                    // A layer in overlay mode being transparent doesn't necessarily mean the
                    // layered texture is transparent.

                    int mode = mapping.getLayerMode(i);
                    if (i == tex.length - 1 || (mode != LayeredMapping.OVERLAY_ADD_BUMPS && mode != LayeredMapping.OVERLAY_BLEND_BUMPS)) {
                        return true;
                    }
                }
            }
            return false;
        }
        if (tex.length == 0) {
            return false;
        }
        for (Texture texture : tex) {
            if (texture.hasComponent(component)) {
                return true;
            }
        }
        return false;
    }

    /**
     * LayeredTexture does not provide its own editor, since this is done directly through the
     * ObjectTextureDialog.
     */
    @Override
    public void edit(WindowWidget<?> fr, Scene sc) {
    }

    public LayeredTexture(DataInputStream in, Scene theScene) throws IOException {
        short version = in.readShort();

        if (version != 0) {
            throw new InvalidObjectException("");
        }
        name = in.readUTF();
    }

    @Override
    public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
        out.writeShort(0);
        out.writeUTF(name);
    }
}
