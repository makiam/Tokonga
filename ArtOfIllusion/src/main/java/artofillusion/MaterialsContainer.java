/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion;

import artofillusion.material.Material;
import artofillusion.material.MaterialMapping;
import artofillusion.object.ObjectInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public sealed interface MaterialsContainer permits Scene {

    default List<Material> getMaterials() {
        var scene = (Scene) this;
        return Collections.unmodifiableList(scene.materials);
    }

    /**
     * Add a new Material to the scene at the given position.
     *
     * @param material The Material to add.
     * @param position The position to add the Material at.
     */
    default void addMaterial(Material material, int position) {
        add(material, position);
    }

    /**
     * Add a new Material to the scene at the given position.
     *
     * @param material The Material to add.
     * @param position The position to add the Material at.
     */
    default void add(Material material, int position) {
        var scene = (Scene) this;
        var action = new AddMaterialAction(scene, material, position).execute();
    }

    /**
     * Add a new Material to the scene.
     *
     * @param material The Material to add.
     */
    default void add(Material material) {
        var scene = (Scene) this;
        var action = new AddMaterialAction(scene, material).execute();
    }

    /**
     * Add a new Material to the scene
     *
     * @param material The Material to add.
     */
    default void addMaterial(Material material) {
        add(material);
    }


    /**
     * Get the material by index.
     */
    default Material getMaterial(int position) {
        var scene = (Scene) this;
        return scene.materials.get(position);
    }


    /**
     * Get the material with the specified name, or null if there is none. If
     * more than one material has the same name, this will return the first one.
     */
    default Material getMaterial(String name) {
        var scene = (Scene) this;
        for (var material : scene.materials) {
            if (material.getName().equals(name)) return material;
        }
        return null;
    }

    /**
     * Get the index of the specified material.
     */
    default int indexOf(Material material) {
        var scene = (Scene) this;
        return scene.materials.indexOf(material);
    }

    /**
     * Get the number of materials in this scene.
     */
    default int getNumMaterials() {
        var scene = (Scene) this;
        return scene.materials.size();
    }


    default void removeMaterial(int position) {
        var scene = (Scene) this;
        if(position >= 0 && position < scene.materials.size()) {
            var action = new RemoveMaterialAction(scene, position).execute();
        }
    }

    record AddMaterialAction(Scene scene, Material material, int position) implements UndoableEdit {

        AddMaterialAction(Scene scene, Material material) {
            this(scene, material, scene.materials.size());
        }

        @Override
        public void undo() {
            throw new Error("Not yet implemented");
        }

        @Override
        public void redo() {
            scene.materials.add(position, material);
            EventBus.getDefault().post(new MaterialAddedEvent(scene, material, position));
        }

        @Override
        public String getName() { return "Add Material"; }
    }

    final class RemoveMaterialAction implements UndoableEdit {

        private Map<ObjectInfo, MaterialMapping> map = new HashMap<>();
        private Material material;
        private int position;
        private final Scene scene;

        RemoveMaterialAction(Scene scene, int position) {
            this.scene = scene;
            this.material = scene.materials.get(position);
            this.position = position;
            for (var info: scene.objects) {
                if(info.getGeometry().getMaterial().equals(material)) {
                    map.put(info, info.getGeometry().getMaterialMapping());
                }
            }
        }

        @Override
        public void undo() {
            throw new Error("Not yet implemented");
        }

        @Override
        public void redo() {
            scene.materials.remove(position);
            map.keySet().forEach(it -> it.setMaterial(null, null));
            EventBus.getDefault().post(new MaterialRemovedEvent(scene, material, position));
        }

        @Override
        public String getName() { return "Remove Material"; }
    }


    record MaterialAddedEvent(Scene scene, Material material, int position) {}
    record MaterialRemovedEvent(Scene scene, Material material, int position) {}
}
