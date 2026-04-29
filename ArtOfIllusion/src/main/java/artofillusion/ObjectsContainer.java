/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.object.ObjectInfo;

import java.util.Collections;
import java.util.List;

sealed interface ObjectsContainer permits Scene {

    /**
     * Get all objects in the Scene in the form of a List.
     */
    default List<ObjectInfo> getObjects() {
        var scene = (Scene)this;
        return Collections.unmodifiableList(scene.objects);
    }

    /**
     * Get the number of objects in this scene.
     */
    default int getNumObjects() {
        var scene = (Scene)this;
        return scene.objects.size();
    }

    /**
     * Get the i'th object.
     */
    default ObjectInfo getObject(int i) {
        var scene = (Scene)this;
        return scene.objects.get(i);
    }

    /**
     * Get the object with the specified name, or null if there is none. If
     * more than one object has the same name, this will return the first one.
     */
    default ObjectInfo getObject(String name) {
        var scene = (Scene)this;
        return scene.objects.stream().filter(info -> info.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Get the object with the specified ID, or null if there is none.
     */
    default ObjectInfo getObjectById(int id) {
        var scene = (Scene)this;
        return scene.objects.stream().filter(info -> info.getId() == id).findFirst().orElse(null);
    }
}
