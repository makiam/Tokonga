/* Copyright (C) 2008 by Peter Eastman
   Updates copyright (C) 2020 by Petri Ihalainen
   Changes copyright (C) 2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.object.*;
import lombok.Getter;

import java.util.*;
import java.beans.*;

/**
 * This class represents a named list of objects in a Scene. It is used for storing
 * saved selections.
 */
public class ObjectSet {

    /**
     * -- GETTER --
     *  Get the name of this ObjectSet.
     */
    @Getter
    private String name;
    private int[] objectIDs;

    static {
        new Encoder().setPersistenceDelegate(ObjectSet.class,
                new DefaultPersistenceDelegate(new String[]{"name", "objectIDs"}));
    }

    /**
     * No-args constructor for saving/loading metadata
     */
    public ObjectSet() {
        name = "";
        objectIDs = new int[0];
    }

    /**
     * Create a new ObjectSet.
     *
     * @param name the name of this set
     * @param objectIDs the IDs of all objects to include in the set
     */
    public ObjectSet(String name, int[] objectIDs) {
        this.name = name;
        this.objectIDs = objectIDs.clone();
    }

    /**
     * Create a new ObjectSet.
     *
     * @param name the name of this set
     * @param objects the objects to include in the set
     */
    public ObjectSet(String name, ObjectInfo[] objects) {
        this.name = name;
        objectIDs = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            objectIDs[i] = objects[i].getId();
        }
    }

    /**
     * Set the name of this ObjectSet.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the IDs of all objects in the set.
     */
    public int[] getObjectIDs() {
        return objectIDs.clone();
    }

    /**
     * Set the IDs from saved metadata
     */
    public void setObjectIDs(int[] objectIDs) {
        this.objectIDs = objectIDs;
    }

    /**
     * Get the objects in the set.
     */
    public List<ObjectInfo> getObjects(Scene scene) {
        List<ObjectInfo> list = new ArrayList<>();
        for (int id : objectIDs) {
            ObjectInfo info = scene.getObjectById(id);
            if (info == null) {
                continue;
            }
            list.add(info);
        }
        return list;
    }
}
