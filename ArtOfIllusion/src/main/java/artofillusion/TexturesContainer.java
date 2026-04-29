/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.texture.Texture;
import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

sealed interface TexturesContainer permits Scene {

    /**
    Get all textures from scene as List
     */
    default List<Texture> getTextures() {
        var scene = (Scene) this;
        return Collections.unmodifiableList(scene.textures);
    }

    /**
     * Add a new Texture to the scene.
     */
    default void add(Texture texture) {
        var scene = (Scene) this;
        add(texture, scene.textures.size());
    }

    /**
     * Add a new Texture to the scene.
     */
    default void addTexture(Texture texture) {
        add(texture);
    }

    /**
     * Add a new Texture to the scene.
     *
     * @param texture the Texture to add
     * @param position the position in the list to add it at
     */
    default void addTexture(Texture texture, int position) {
        add(texture, position);
    }

    /**
     * Add a new Texture to the scene.
     *
     * @param texture the Texture to add
     * @param position the position in the list to add it at
     */
    default void add(Texture texture, int position) {
        var scene = (Scene) this;
        scene.textures.add(position, texture);
        EventBus.getDefault().post(new TextureAddedEvent(scene, texture, position));
    }

    /**
     * Get the texture by index.
     */
    default Texture getTexture(int position) {
        var scene = (Scene) this;
        return scene.textures.get(position);
    }

    /**
     * Get the index of the specified texture.
     */
    default int indexOf(Texture texture) {
        var scene = (Scene) this;
        return scene.textures.indexOf(texture);
    }

    /**
     * Get the number of textures in this scene.
     */
    default int getNumTextures() {
        var scene = (Scene) this;
        return scene.textures.size();
    }

    /**
     * Get the texture with the specified name, or null if there is none. If
     * more than one texture has the same name, this will return the first one.
     */
    default Texture getTexture(String name) {
        var scene = (Scene) this;
        for (var texture : scene.textures) {
            if (texture.getName().equals(name)) return texture;
        }
        return null;
    }


    record TextureAddedEvent(Scene scene, Texture texture, int position) {}
    record TextureRemovedEvent(Scene scene, Texture texture, int position) {}
}
