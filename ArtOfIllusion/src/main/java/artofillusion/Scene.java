/* Copyright (C) 1999-2013 by Peter Eastman
   Changes copyright (C) 2016-2023 by Maksim Khramov
   Changes copyright (C) 2017-2020 by Petri Ihalainen

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.animation.*;
import artofillusion.image.*;
import artofillusion.material.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import artofillusion.util.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.*;
import lombok.extern.slf4j.Slf4j;

/**
 * The Scene class describes a collection of objects, arranged relative to each other to
 * form a scene, as well as the available textures and materials, environment options, etc.
 */
@Slf4j
public class Scene {

    private List<ObjectInfo> objects;
    private List<Material> materials;
    private List<Texture> textures;

    private List<ImageMap> images;
    private Vector<Integer> selection;
    private List<ListChangeListener> textureListeners, materialListeners;
    private HashMap<String, Object> metadataMap;
    private HashMap<ObjectInfo, Integer> objectIndexMap;
    private RGBColor ambientColor, environColor, fogColor;
    private Texture environTexture;
    private TextureMapping environMapping;
    private int gridSubdivisions, environMode, framesPerSecond, nextID;
    private double fogDist, gridSpacing, time;
    private boolean fog, showGrid, snapToGrid;
    private String name, directory;

    private ParameterValue[] environParamValue;

    private final List<String> errors = new ArrayList<>();

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public static final int HANDLE_SIZE = 4;
    public static final int ENVIRON_SOLID = 0;
    public static final int ENVIRON_DIFFUSE = 1;
    public static final int ENVIRON_EMISSIVE = 2;

    private static final byte[] FILE_PREFIX = {'A', 'o', 'I', 'S', 'c', 'e', 'n', 'e'};

    public Scene() {
        UniformTexture defTex = new UniformTexture();

        objects = new Vector<>();
        materials = new Vector<>();
        textures = new Vector<>();
        images = new Vector<>();
        selection = new Vector<>();
        metadataMap = new HashMap<>();
        textureListeners = new Vector<>();
        materialListeners = new Vector<>();
        defTex.setName("Default Texture");
        textures.add(defTex);
        ambientColor = new RGBColor(0.3f, 0.3f, 0.3f);
        environColor = new RGBColor(0.0f, 0.0f, 0.0f);
        environTexture = defTex;
        environMapping = defTex.getDefaultMapping(new Sphere(1.0, 1.0, 1.0));
        environParamValue = new ParameterValue[0];
        environMode = ENVIRON_SOLID;
        fogColor = new RGBColor(0.3f, 0.3f, 0.3f);
        fogDist = 20.0;
        fog = false;
        framesPerSecond = 30;
        nextID = 1;

        // Grids are off by default.
        showGrid = snapToGrid = false;
        gridSpacing = 1.0;
        gridSubdivisions = 10;
    }

    /**
     * Get the name of this scene.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this scene.
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Get the directory on disk in which this scene is saved.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Set the directory on disk in which this scene is saved.
     */
    public void setDirectory(String newDir) {
        directory = newDir;
    }

    /**
     * Get the current time.
     */
    public double getTime() {
        return time;
    }

    /**
     * Set the current time.
     */
    public void setTime(double t) {
        time = t;
        boolean[] processed = new boolean[objects.size()];
        objects.forEach(item
                -> applyTracksToObject(item, processed, null, objects.indexOf(item)));
        for (ObjectInfo obj : objects) {
            obj.getObject().sceneChanged(obj, this);
        }
    }

    /**
     * Modify an object (and any objects that depend on it) based on its tracks at the current time.
     */
    public void applyTracksToObject(ObjectInfo info) {
        applyTracksToObject(info, new boolean[objects.size()], null, 0);
        objects.forEach(item -> item.getObject().sceneChanged(item, this));
    }

    /**
     * This should be called after one or more objects have been modified by the user.
     * It applies the animation tracks of all other objects which depend on the modified
     * ones. It also applies a subset of the animation tracks on the modified objects
     * themselves to reflect their dependencies on other parts of the scene.
     */
    public void applyTracksAfterModification(Collection<ObjectInfo> changedObjects) {
        boolean[] changed = new boolean[objects.size()];
        boolean[] processed = new boolean[objects.size()];

        // First apply a subset of the tracks of the modified objects.
        for (ObjectInfo info : changedObjects) {
            int index = indexOf(info);
            changed[index] = processed[index] = true;

            // Find Constraint and IK tracks at the top of the list and apply them.
            int i;
            for (i = 0; i < info.getTracks().length && (info.getTracks()[i] instanceof ConstraintTrack
                    || info.getTracks()[i] instanceof IKTrack || info.getTracks()[i].isNullTrack()); i++);
            for (int j = i - 1; j >= 0; j--) {
                if (info.getTracks()[j].isEnabled()) {
                    info.getTracks()[j].apply(time);
                }
            }
            if (info.getPose() != null) {
                info.getObject().applyPoseKeyframe(info.getPose());
            }
        }

        // Now apply tracks to all dependent objects.
        objects.forEach(item
                -> applyTracksToObject(item, processed, changed, objects.indexOf(item)));
        objects.forEach(item -> item.getObject().sceneChanged(item, this));
    }

    private void applyTracksToObject(ObjectInfo info, boolean[] processed, boolean[] changed, int index) {
        if (processed[index]) {
            // This object has already been updated.

            info.getObject().sceneChanged(info, this);
            return;
        }
        processed[index] = true;

        // Determine whether this object possesses a Position or Rotation track, and update any
        // tracks it is dependent on.
        boolean hasPos = false, hasRot = false, hasPose = false;
        for (Track track : info.getTracks()) {
            if (track.isNullTrack() || !track.isEnabled()) {
                continue;
            }
            ObjectInfo[] depends = track.getDependencies();
            for (int i = 0; i < depends.length; i++) {
                int k = indexOf(depends[i]);
                if (k > -1 && !processed[k]) {
                    applyTracksToObject(depends[i], processed, changed, k);
                }
                if (k > -1 && changed != null && changed[k]) {
                    changed[index] = true;
                }
            }
            if (track instanceof PositionTrack || track instanceof ProceduralPositionTrack) {
                hasPos = true;
            } else if (track instanceof RotationTrack || track instanceof ProceduralRotationTrack) {
                hasRot = true;
            } else if (track instanceof PoseTrack || track instanceof IKTrack) {
                hasPose = true;
            }
        }
        if (changed != null && !changed[index]) {
            return;
        }
        if (hasPos) {
            Vec3 orig = info.getCoords().getOrigin();
            orig.set(0.0, 0.0, 0.0);
            info.getCoords().setOrigin(orig);
        }
        if (hasRot) {
            info.getCoords().setOrientation(0.0, 0.0, 0.0);
        }
        if (hasPose) {
            info.clearCachedMeshes();
        }
        info.setPose(null);

        // Apply the tracks.
        info.clearDistortion();
        for (int j = info.getTracks().length - 1; j >= 0; j--) {
            if (info.getTracks()[j].isEnabled()) {
                info.getTracks()[j].apply(time);
            }
        }
        if (info.getPose() != null) {
            info.getObject().applyPoseKeyframe(info.getPose());
        }
    }

    /**
     * Get the number of frames per second.
     */
    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    /**
     * Set the number of frames per second.
     */
    public void setFramesPerSecond(int n) {
        framesPerSecond = n;
    }

    /**
     * Get the scene's ambient light color.
     */
    public RGBColor getAmbientColor() {
        return ambientColor;
    }

    /**
     * Set the scene's ambient light color.
     */
    public void setAmbientColor(RGBColor color) {
        ambientColor = color;
    }

    /**
     * Get the Scene's environment mapping mode. This will be either ENVIRON_SOLID, ENVIRON_DIFFUSE, or
     * ENVIRON_EMISSIVE.
     */
    public int getEnvironmentMode() {
        return environMode;
    }

    /**
     * Set the Scene's environment mapping mode. This should be either ENVIRON_SOLID, ENVIRON_DIFFUSE, or
     * ENVIRON_EMISSIVE.
     */
    public void setEnvironmentMode(int mode) {
        environMode = mode;
    }

    /**
     * Get the texture being used as an environment mapping.
     */
    public Texture getEnvironmentTexture() {
        return environTexture;
    }

    /**
     * Set the texture being used as an environment mapping.
     */
    public void setEnvironmentTexture(Texture tex) {
        environTexture = tex;
    }

    /**
     * Get the TextureMapping being used to map the environment map texture to the environment sphere.
     */
    public TextureMapping getEnvironmentMapping() {
        return environMapping;
    }

    /**
     * Set the TextureMapping to use for mapping the environment map texture to the environment sphere.
     */
    public void setEnvironmentMapping(TextureMapping map) {
        environMapping = map;
    }

    /**
     * Get the parameter values used for the environment map.
     */
    public ParameterValue[] getEnvironmentParameterValues() {
        return environParamValue;
    }

    /**
     * Set the parameter values used for the environment map.
     */
    public void setEnvironmentParameterValues(ParameterValue[] value) {
        environParamValue = value;
    }

    /**
     * Get the environment color.
     */
    public RGBColor getEnvironmentColor() {
        return environColor;
    }

    /**
     * Set the environment color.
     */
    public void setEnvironmentColor(RGBColor color) {
        environColor = color;
    }

    /**
     * Get the fog color.
     */
    public RGBColor getFogColor() {
        return fogColor;
    }

    /**
     * Set the fog color.
     */
    public void setFogColor(RGBColor color) {
        fogColor = color;
    }

    /**
     * Determine whether fog is enabled.
     */
    public boolean getFogState() {
        return fog;
    }

    /**
     * Get the length constant for exponential fog.
     */
    public double getFogDistance() {
        return fogDist;
    }

    /**
     * Set the state of fog in the scene.
     *
     * @param state sets whether fog is enabled
     * @param dist the length constant for exponential fog.
     */
    public void setFog(boolean state, double dist) {
        fog = state;
        fogDist = dist;
    }

    /**
     * Get whether the grid is displayed.
     */
    public boolean getShowGrid() {
        return showGrid;
    }

    /**
     * Set whether the grid is displayed.
     */
    public void setShowGrid(boolean show) {
        showGrid = show;
    }

    /**
     * Get whether snap-to-grid is enabled.
     */
    public boolean getSnapToGrid() {
        return snapToGrid;
    }

    /**
     * Set whether snap-to-grid is enabled.
     */
    public void setSnapToGrid(boolean snap) {
        snapToGrid = snap;
    }

    /**
     * Get the grid spacing.
     */
    public double getGridSpacing() {
        return gridSpacing;
    }

    /**
     * Set the grid spacing.
     */
    public void setGridSpacing(double spacing) {
        gridSpacing = spacing;
    }

    /**
     * Get the number of grid snap-to subdivisions.
     */
    public int getGridSubdivisions() {
        return gridSubdivisions;
    }

    /**
     * Set the number of grid snap-to subdivisions.
     */
    public void setGridSubdivisions(int subdivisions) {
        gridSubdivisions = subdivisions;
    }

    /**
     * Add a new object to the scene. If undo is not null, appropriate commands will be
     * added to it to undo this operation.
     */
    public void addObject(Object3D obj, CoordinateSystem coords, String name, UndoRecord undo) {
        addObject(new ObjectInfo(obj, coords, name), undo);
    }

    /**
     * Add a new object to the scene. If undo is not null, appropriate commands will be
     * added to it to undo this operation.
     */
    public void addObject(ObjectInfo info, UndoRecord undo) {
        addObject(info, objects.size(), undo);
    }

    /**
     * Add a new object to the scene in the specified position. If undo is not null,
     * appropriate commands will be added to it to undo this operation.
     */
    public void addObject(ObjectInfo info, int index, UndoRecord undo) {
        info.setId(nextID++);
        if (info.getTracks() == null) {
            info.addTrack(new PositionTrack(info), 0);
            info.addTrack(new RotationTrack(info), 1);
        }
        if (info.getObject().canSetTexture() && info.getObject().getTextureMapping() == null) {
            info.setTexture(getDefaultTexture(),
                    getDefaultTexture().getDefaultMapping(info.getObject()));
        }

        info.getObject().sceneChanged(info, this);
        objects.add(index, info);
        objectIndexMap = null;

        if (undo != null) {
            undo.addCommandAtBeginning(UndoRecord.DELETE_OBJECT, index);
        }

        updateSelectionInfo();
    }

    /**
     * Delete an object from the scene. If undo is not null, appropriate commands will be
     * added to it to undo this operation.
     */
    public void removeObject(int which, UndoRecord undo) {
        ObjectInfo info = objects.get(which);
        objects.remove(which);
        objectIndexMap = null;
        if (undo != null) {
            undo.addCommandAtBeginning(UndoRecord.ADD_OBJECT, info, which);
        }
        if (info.getParent() != null) {
            int j;
            for (j = 0; info.getParent().getChildren()[j] != info; j++);
            if (undo != null) {
                undo.addCommandAtBeginning(UndoRecord.ADD_TO_GROUP, info.getParent(), info, j);
            }
            info.getParent().removeChild(j);
        }
        for (int i = 0; i < objects.size(); i++) {
            ObjectInfo obj = objects.get(i);
            for (int j = 0; j < obj.getTracks().length; j++) {
                Track tr = obj.getTracks()[j];
                ObjectInfo[] depends = tr.getDependencies();
                for (int k = 0; k < depends.length; k++) {
                    if (depends[k] == info) {
                        if (undo != null) {
                            undo.addCommandAtBeginning(UndoRecord.COPY_TRACK, tr, tr.duplicate(tr.getParent()));
                        }
                        obj.getTracks()[j].deleteDependencies(info);
                    }
                }
            }
        }
        clearSelection();
    }

    /**
     * Add a new Material to the scene.
     */
    public void addMaterial(Material mat) {
        addMaterial(mat, materials.size());
    }

    /**
     * Add a new Material to the scene.
     *
     * @param mat the Material to add
     * @param index the position in the list to add it at
     */
    public void addMaterial(Material mat, int index) {
        materials.add(index, mat);
        int size = materials.size() - 1;
        materialListeners.forEach(listener -> listener.itemAdded(size, mat));
    }

    /**
     * Remove a Material from the scene.
     */
    public void removeMaterial(int which) {
        Material mat = materials.remove(which);
        materialListeners.forEach(listener -> listener.itemRemoved(which, mat));
        objects.stream()
                .filter(item -> item.getObject().getMaterial() == mat)
                .forEach(item -> item.setMaterial(null, null));
    }

    /**
     * Reorder the list of Materials by moving a Material to a new position in the list.
     *
     * @param oldIndex the index of the Material to move
     * @param newIndex the new position to move it to
     */
    public void reorderMaterial(int oldIndex, int newIndex) {
        if (newIndex < 0 || newIndex >= materials.size()) {
            throw new IllegalArgumentException("Illegal value for newIndex: " + newIndex);
        }
        Material mat = materials.remove(oldIndex);
        materials.add(newIndex, mat);
    }

    /**
     * Add a new Texture to the scene.
     */
    public void addTexture(Texture tex) {
        addTexture(tex, textures.size());
    }

    /**
     * Add a new Texture to the scene.
     *
     * @param tex the Texture to add
     * @param index the position in the list to add it at
     */
    public void addTexture(Texture tex, int index) {
        textures.add(index, tex);
        int pos = textures.size() - 1;
        textureListeners.forEach(listener ->  listener.itemAdded(pos, tex));
    }

    /**
     * Remove a Texture from the scene.
     */
    public void removeTexture(int which) {

        Texture tex = textures.remove(which);
        textureListeners.forEach(listener -> listener.itemRemoved(which, tex));
        if (textures.isEmpty()) {
            UniformTexture defTex = new UniformTexture();
            defTex.setName("Default Texture");
            textures.add(defTex);
            textureListeners.forEach(listener -> listener.itemAdded(0, defTex));
        }
        Texture def = textures.get(0);
        for (int i = 0; i < objects.size(); i++) {
            ObjectInfo obj = objects.get(i);
            if (obj.getObject().getTexture() == tex) {
                obj.setTexture(def, def.getDefaultMapping(obj.getObject()));
            }
            if (obj.getObject().getTextureMapping() instanceof LayeredMapping) {
                LayeredMapping map = (LayeredMapping) obj.getObject().getTextureMapping();
                for (int j = map.getNumLayers() - 1; j >= 0; j--) {
                    if (map.getLayer(j) == tex) {
                        map.deleteLayer(j);
                    }
                }
                obj.setTexture(obj.getObject().getTexture(), map);
            }
        }
        if (environTexture == tex) {
            environTexture = def;
            environMapping = def.getDefaultMapping(new Sphere(1.0, 1.0, 1.0));
        }
        if (environMapping instanceof LayeredMapping) {
            Sphere tempObject = new Sphere(1, 1, 1);
            tempObject.setTexture(environTexture, environMapping);
            tempObject.setParameterValues(environParamValue);
            LayeredMapping map = (LayeredMapping) environMapping;
            for (int j = map.getNumLayers() - 1; j >= 0; j--) {
                if (map.getLayer(j) == tex) {
                    map.deleteLayer(j);
                }
            }
            tempObject.setTexture(environTexture, environMapping);
            environParamValue = tempObject.getParameterValues();
        }
    }

    /**
     * Reorder the list of Textures by moving a Texture to a new position in the list.
     *
     * @param oldIndex the index of the Texture to move
     * @param newIndex the new position to move it to
     */
    public void reorderTexture(int oldIndex, int newIndex) {
        if (newIndex < 0 || newIndex >= textures.size()) {
            throw new IllegalArgumentException("Illegal value for newIndex: " + newIndex);
        }
        Texture tex = textures.remove(oldIndex);
        textures.add(newIndex, tex);
    }

    /**
     * This method should be called after a Material has been edited. It notifies
     * any objects using the Material that it has changed.
     */
    public void changeMaterial(int which) {
        Material mat = materials.get(which);

        objects.stream().filter(item -> item.getObject().getMaterial() == mat).
                forEach(item -> {
                    Object3D obj = item.getObject();
                    obj.setMaterial(mat, obj.getMaterialMapping());
                });
        materialListeners.forEach(listener -> listener.itemChanged(which, mat));
    }

    /**
     * This method should be called after a Texture has been edited. It notifies
     * any objects using the Texture that it has changed.
     */
    public void changeTexture(int which) {
        Texture tex = textures.get(which);

        for (ObjectInfo obj : objects) {

            if (obj.getObject().getTexture() == tex) {
                obj.setTexture(tex, obj.getObject().getTextureMapping());
            } else if (obj.getObject().getTexture() instanceof LayeredTexture) {
                for (Texture layer : ((LayeredMapping) obj.getObject().getTextureMapping()).getLayers()) {
                    if (layer == tex) {
                        obj.setTexture(tex, obj.getObject().getTextureMapping());
                        break;
                    }
                }
            }
        }
        textureListeners.forEach(listener -> listener.itemChanged(which, tex));

    }

    /**
     * Add an object which wants to be notified when the list of Materials in the Scene changes.
     */
    public void addMaterialListener(ListChangeListener ls) {
        materialListeners.add(ls);
    }

    /**
     * Remove an object from the set to be notified when the list of Materials changes.
     */
    public void removeMaterialListener(ListChangeListener ls) {
        materialListeners.remove(ls);
    }

    /**
     * Add an object which wants to be notified when the list of Textures in the Scene changes.
     */
    public void addTextureListener(ListChangeListener ls) {
        textureListeners.add(ls);
    }

    /**
     * Remove an object from the set to be notified when the list of Textures changes.
     */
    public void removeTextureListener(ListChangeListener ls) {
        textureListeners.remove(ls);
    }

    /**
     * Get a piece of metadata stored in this scene.
     *
     * @param name the name of the piece of metadata to get
     * @return the value associated with that name, or null if there is none
     */
    public Object getMetadata(String name) {
        return metadataMap.get(name);
    }

    /**
     * Store a piece of metadata in this scene. This may be an arbitrary object which
     * you want to store as part of the scene. When the scene is saved to disk, metadata
     * objects are stored using the java.beans.XMLEncoder class. This means that if the
     * object is not a bean, you must register a PersistenceDelegate for it before calling
     * this method. Otherwise, it will fail to be saved.
     *
     * @param name the name of the piece of metadata to set
     * @param value the value to store
     */
    public void setMetadata(String name, Object value) {
        metadataMap.put(name, value);
    }

    /**
     * Get the names of all metadata objects stored in this scene.
     */
    public Set<String> getAllMetadataNames() {
        return metadataMap.keySet();
    }


    /**
     * Add an image map to the scene.
     */
    public void addImage(ImageMap im) {
        images.add(im);
    }

    /**
     * Remove an image map from the scene.
     */
    public boolean removeImage(int which) {
        ImageMap image = images.get(which);

        for (var texture: textures) {
            if (texture.usesImage(image)) {
                return false;
            }
        }
        for (var material: materials) {
            if (material.usesImage(image)) {
                return false;
            }
        }
        images.remove(which);
        return true;
    }

    /**
     * Replace an ImageMap with another one
     */
    public void replaceImage(int which, ImageMap im) {
        images.set(which, im);
    }

    /**
     * Replace every instance of one object in the scene with another one. If undo is not
     * null, commands will be added to it to undo this operation.
     */
    public void replaceObject(Object3D original, Object3D replaceWith, UndoRecord undo) {
        Optional<UndoRecord> optionalUndo = Optional.ofNullable(undo);
        objects.stream().filter(item -> item.getObject() == original).forEach(item -> {
            optionalUndo.ifPresent(command -> command.addCommand(UndoRecord.SET_OBJECT, item, original));
            item.setObject(replaceWith);
            item.clearCachedMeshes();
        });
    }

    /**
     * This should be called whenever an object changes. It clears any cached meshes for
     * any instances of the object.
     */
    public void objectModified(Object3D obj) {
        objects.stream()
                .filter(item -> item.getObject() == obj)
                .forEach(item -> {
                    item.clearCachedMeshes();
                    item.setPose(null);
                });
    }

    /**
     * Set one object to be selected, deselecting all other objects.
     *
     * @deprecated Call setSelection() on the LayoutWindow instead.
     */
    @Deprecated
    public void setSelection(int which) {
        clearSelection();
        addToSelection(which);
        updateSelectionInfo();
    }

    /**
     * Set a list of objects to be selected, deselecting all other objects.
     *
     * @deprecated Call setSelection() on the LayoutWindow instead.
     */
    @Deprecated
    public void setSelection(int[] which) {
        clearSelection();
        for (int index : which) {
            ObjectInfo info = objects.get(index);
            if (!info.selected) {
                selection.addElement(index);
            }
            info.selected = true;
        }
        updateSelectionInfo();
    }

    /**
     * Add an object to the list of selected objects.
     *
     * @deprecated Call addToSelection() on the LayoutWindow instead.
     */
    @Deprecated
    public void addToSelection(int which) {
        ObjectInfo info = objects.get(which);
        if (!info.selected) {
            selection.addElement(which);
        }
        info.selected = true;
        updateSelectionInfo();
    }

    /**
     * Deselect all objects.
     *
     * @deprecated Call clearSelection() on the LayoutWindow instead.
     */
    @Deprecated
    public void clearSelection() {
        if (selection.isEmpty()) {
            return;
        }
        selection.clear();
        objects.forEach(item -> {
            item.selected = false;
        });

        updateSelectionInfo();
    }

    /**
     * Deselect a particular object.
     *
     * @deprecated Call removeFromSelection() on the LayoutWindow instead.
     */
    @Deprecated
    public void removeFromSelection(int which) {
        ObjectInfo info = objects.get(which);
        selection.removeElement(which);
        info.selected = false;
        updateSelectionInfo();
    }

    /**
     * Calculate the list of which objects are children of selected objects.
     */
    private void updateSelectionInfo() {
        for (int i = objects.size() - 1; i >= 0; i--) {
            objects.get(i).parentSelected = false;
        }
        for (int i = objects.size() - 1; i >= 0; i--) {
            ObjectInfo info = objects.get(i);
            ObjectInfo parent = info.getParent();
            while (parent != null) {
                if (parent.selected || parent.parentSelected) {
                    info.parentSelected = true;
                    break;
                }
                parent = parent.getParent();
            }
        }
    }

    /**
     * Get the number of objects in this scene.
     */
    public int getNumObjects() {
        return objects.size();
    }

    /**
     * Get the i'th object.
     */
    public ObjectInfo getObject(int i) {
        return objects.get(i);
    }

    /**
     * Get the object with the specified name, or null if there is none. If
     * more than one object has the same name, this will return the first one.
     */
    public ObjectInfo getObject(String name) {
        return objects.stream().filter(info -> info.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Get the object with the specified ID, or null if there is none.
     */
    public ObjectInfo getObjectById(int id) {
        return objects.stream().filter(info -> info.getId() == id).findFirst().orElse(null);
    }


    /**
     * Get all objects in the Scene in the form of a List.
     */
    public List<ObjectInfo> getObjects() {
        return Collections.unmodifiableList(objects);
    }

    /*
    Get all materials from scene as List
     */
    public List<Material> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    /*
    Get all textures from scene as List
     */
    public List<Texture> getTextures() {
        return Collections.unmodifiableList(textures);
    }

    /**
     * Get the index of the specified object.
     */
    public int indexOf(ObjectInfo info) {
        if (objectIndexMap == null) {
            // Build an index for fast lookup
            objectIndexMap = new HashMap<>();
            objects.forEach(item -> objectIndexMap.put(item, objects.indexOf(item)));
        }

        return objectIndexMap.getOrDefault(info, -1);
    }

    /**
     * Get the number of textures in this scene.
     */
    public int getNumTextures() {
        return textures.size();
    }

    /**
     * Get the list of scene cameras.
     */
    public List<ObjectInfo> getCameras() {
        return objects.stream()
                .filter(item -> item.getObject() instanceof SceneCamera)
                .collect(Collectors.toList());
    }

    /**
     * Get the index of the specified texture.
     */
    public int indexOf(Texture tex) {
        return textures.indexOf(tex);
    }

    /**
     * Get the i'th texture.
     */
    public Texture getTexture(int i) {
        return textures.get(i);
    }

    /**
     * Get the texture with the specified name, or null if there is none. If
     * more than one texture has the same name, this will return the first one.
     */
    public Texture getTexture(String name) {
        return textures.stream()
                .filter(asset -> asset.getName()
                .equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the number of materials in this scene.
     */
    public int getNumMaterials() {
        return materials.size();
    }

    /**
     * Get the i'th material.
     */
    public Material getMaterial(int i) {
        return materials.get(i);
    }

    /**
     * Get the material with the specified name, or null if there is none. If
     * more than one material has the same name, this will return the first one.
     */
    public Material getMaterial(String name) {
        return materials.stream()
                .filter(asset -> asset.getName().equals(name))
                .findFirst().orElse(null);
    }

    /**
     * Get the index of the specified material.
     */
    public int indexOf(Material mat) {
        return materials.indexOf(mat);
    }

    /**
     * Get the number of image maps in this scene.
     */
    public int getNumImages() {
        return images.size();
    }

    /**
     * Get the i'th image map.
     */
    public ImageMap getImage(int i) {
        return images.get(i);
    }

    /**
     * Get the index of the specified image map.
     */
    public int indexOf(ImageMap im) {
        return images.indexOf(im);
    }

    /**
     * Get the default Texture for newly created objects.
     */
    public Texture getDefaultTexture() {
        return textures.get(0);
    }

    /**
     * Get a list of the indices of all selected objects.
     *
     * @deprecated Call getSelectedIndices() or getSelectedObjects() on the LayoutWindow instead.
     */
    @Deprecated
    public int[] getSelection() {
        int[] sel = new int[selection.size()];

        for (int i = 0; i < sel.length; i++) {
            sel[i] = selection.elementAt(i);
        }
        return sel;
    }

    /**
     * Get the indices of all objects which are either selected, or are children of
     * selected objects.
     *
     * @deprecated Call getSelectionWithChildren() on the LayoutWindow instead.
     */
    @Deprecated
    public int[] getSelectionWithChildren() {
        int count = 0;
        for (int i = objects.size() - 1; i >= 0; i--) {
            ObjectInfo info = objects.get(i);
            if (info.selected || info.parentSelected) {
                count++;
            }
        }
        int[] sel = new int[count];
        count = 0;
        for (int i = objects.size() - 1; i >= 0; i--) {
            ObjectInfo info = objects.get(i);
            if (info.selected || info.parentSelected) {
                sel[count++] = i;
            }
        }
        return sel;
    }

    /**
     * The following constructor is used for reading files. If fullScene is false, only the
     * Textures and Materials are read.
     */
    public Scene(File f, boolean fullScene) throws IOException, InvalidObjectException {
        setName(f.getName());
        setDirectory(f.getParent());
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f));
        buf.mark(FILE_PREFIX.length);

        // See if the file begins with the expected prefix.
        boolean hasPrefix = true;
        for (int i = 0; hasPrefix && i < FILE_PREFIX.length; i++) {
            hasPrefix &= (buf.read() == FILE_PREFIX[i]);
        }
        if (!hasPrefix) {
            buf.reset(); // This is an old file that doesn't start with the prefix.
        }
        // We expect the data to be gzipped, but if it's somehow gotten decompressed we should accept that to.

        DataInputStream in;
        try {
            in = new DataInputStream(new GZIPInputStream(buf));
        } catch (IOException ex) {
            buf.close();
            buf = new BufferedInputStream(new FileInputStream(f));
            in = new DataInputStream(buf);
        }
        initFromStream(in, fullScene);
        in.close();
    }

    /**
     * The following constructor is used for reading from arbitrary input streams. If fullScene
     * is false, only the Textures and Materials are read.
     */
    public Scene(DataInputStream in, boolean fullScene) throws IOException, InvalidObjectException {
        initFromStream(in, fullScene);
    }

    /**
     * Initialize the scene based on information read from an input stream.
     */
    private void initFromStream(DataInputStream in, boolean fullScene) throws IOException, InvalidObjectException {
        int count;
        short version = in.readShort();
        Hashtable<Integer, Object3D> table;
        Class<?> cls;
        Constructor<?> con;

        if (version < 0 || version > 5) {
            throw new InvalidObjectException("");
        }

        ambientColor = new RGBColor(in);
        fogColor = new RGBColor(in);
        fog = in.readBoolean();
        fogDist = in.readDouble();
        showGrid = in.readBoolean();
        snapToGrid = in.readBoolean();
        gridSpacing = in.readDouble();
        gridSubdivisions = in.readInt();
        framesPerSecond = in.readInt();
        nextID = 1;

        // Read the image maps.
        count = in.readInt();
        images = new Vector<>(count);
        for (int i = 0; i < count; i++) {
            if (version == 0) {
                images.add(new MIPMappedImage(in, (short) 0));
                continue;
            }
            String classname = in.readUTF();
            try {
                cls = ArtOfIllusion.getClass(classname);
                if (cls == null) {
                    throw new IOException("Unknown class: " + classname);
                }
                con = cls.getConstructor(DataInputStream.class);
                images.add((ImageMap) con.newInstance(in));
            } catch (IOException | ReflectiveOperationException | SecurityException ex) {
                throw new IOException("Error loading image: " + ex.getMessage());
            }
        }

        // Read the materials.
        count = in.readInt();
        materials = new Vector<>(count);
        for (int i = 0; i < count; i++) {
            try {
                String classname = in.readUTF();
                int len = in.readInt();
                byte[] bytes = new byte[len];
                in.readFully(bytes);
                cls = ArtOfIllusion.getClass(classname);
                try {
                    if (cls == null) {
                        throw new IOException("Unknown class: " + classname);
                    }
                    con = cls.getConstructor(DataInputStream.class, Scene.class);
                    materials.add((Material) con.newInstance(new DataInputStream(new ByteArrayInputStream(bytes)), this));
                } catch (IOException | ReflectiveOperationException | SecurityException ex) {
                    log.atError().setCause(ex).log("Error loading material: {}", ex.getMessage());
                    if (ex instanceof ClassNotFoundException) {
                        errors.add(Translate.text("errorFindingClass", classname));
                    } else {
                        errors.add(Translate.text("errorInstantiatingClass", classname));
                    }
                    UniformMaterial m = new UniformMaterial();
                    m.setName("<unreadable>");
                    materials.add(m);
                }
            } catch (IOException | ClassNotFoundException ex) {
                log.atError().setCause(ex).log("Error reading: {}", ex.getMessage());
                throw new IOException();
            }
        }

        // Read the textures.
        count = in.readInt();
        textures = new Vector<>(count);
        for (int i = 0; i < count; i++) {
            try {
                String classname = in.readUTF();
                int len = in.readInt();
                byte[] bytes = new byte[len];
                in.readFully(bytes);
                cls = ArtOfIllusion.getClass(classname);
                try {
                    if (cls == null) {
                        throw new IOException("Unknown class: " + classname);
                    }
                    con = cls.getConstructor(DataInputStream.class, Scene.class);
                    textures.add((Texture) con.newInstance(new DataInputStream(new ByteArrayInputStream(bytes)), this));
                } catch (IOException | SecurityException | ReflectiveOperationException ex) {
                    log.atError().setCause(ex).log("Error loading texture: {}", ex.getMessage());
                    if (ex instanceof ClassNotFoundException) {
                        errors.add(Translate.text("errorFindingClass", classname));
                    } else {
                        errors.add(Translate.text("errorInstantiatingClass", classname));
                    }
                    UniformTexture t = new UniformTexture();
                    t.setName("<unreadable>");
                    textures.add(t);
                }
            } catch (IOException | ClassNotFoundException | IllegalArgumentException ex) {
                log.atError().setCause(ex).log("Error reading: {}", ex.getMessage());
                throw new IOException();
            }
        }

        // Read the objects.
        count = in.readInt();
        objects = new Vector<>(count);
        table = new Hashtable<>(count);
        for (int i = 0; i < count; i++) {
            objects.add(readObjectFromFile(in, table, version));
        }
        objectIndexMap = null;
        selection = new Vector<>();

        // Read the list of children for each object.
        for (int i = 0; i < objects.size(); i++) {
            ObjectInfo info = objects.get(i);
            int num = in.readInt();
            for (int j = 0; j < num; j++) {
                ObjectInfo child = objects.get(in.readInt());
                info.addChild(child, j);
            }
        }

        // Read in the environment mapping information.
        environMode = (int) in.readShort();
        if (environMode == ENVIRON_SOLID) {
            environColor = new RGBColor(in);
            environTexture = textures.get(0);
            environMapping = environTexture.getDefaultMapping(new Sphere(1.0, 1.0, 1.0));
            environParamValue = new ParameterValue[0];
        } else {
            int texIndex = in.readInt();
            if (texIndex == -1) {
                // This is a layered texture.

                Object3D sphere = new Sphere(1.0, 1.0, 1.0);
                environTexture = new LayeredTexture(sphere);
                String mapClassName = in.readUTF();
                if (!LayeredMapping.class.getName().equals(mapClassName)) {
                    throw new InvalidObjectException("");
                }
                environMapping = environTexture.getDefaultMapping(sphere);
                ((LayeredMapping) environMapping).readFromFile(in, this);
            } else {
                environTexture = textures.get(texIndex);
                try {
                    Class<?> mapClass = ArtOfIllusion.getClass(in.readUTF());
                    con = mapClass.getConstructor(DataInputStream.class, Object3D.class, Texture.class);
                    environMapping = (TextureMapping) con.newInstance(in, new Sphere(1.0, 1.0, 1.0), environTexture);
                } catch (IOException | SecurityException | ReflectiveOperationException ex) {
                    throw new IOException();
                }
            }
            environColor = new RGBColor(0.0f, 0.0f, 0.0f);
            environParamValue = new ParameterValue[environMapping.getParameters().length];
            if (version > 2) {
                for (int i = 0; i < environParamValue.length; i++) {
                    environParamValue[i] = Object3D.readParameterValue(in);
                }
            }
        }

        // Read the metadata.
        metadataMap = new HashMap<>();
        if (version > 3) {
            count = in.readInt();
            SearchlistClassLoader loader = new SearchlistClassLoader(getClass().getClassLoader());
            for (ClassLoader cl : PluginRegistry.getPluginClassLoaders()) {
                loader.add(cl);
            }
            for (int i = 0; i < count; i++) {
                try {
                    String name = in.readUTF();
                    byte[] data = new byte[in.readInt()];
                    in.readFully(data);
                    XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(data), null, null, loader);
                    metadataMap.put(name, decoder.readObject());
                } catch (IOException ex) {
                    log.atError().setCause(ex).log("Metadata reading error: {}", ex.getMessage());
                    // Nothing more we can do about it.
                }
            }
        }
        textureListeners = new Vector<>();
        materialListeners = new Vector<>();
        setTime(0.0);
    }

    private ObjectInfo readObjectFromFile(DataInputStream in, Hashtable<Integer, Object3D> table, int version) throws IOException, InvalidObjectException {
        ObjectInfo info = new ObjectInfo(null, new CoordinateSystem(in), in.readUTF());
        Class<?> cls;
        Constructor<?> con;
        Object3D obj;

        info.setId(in.readInt());
        if (info.getId() >= nextID) {
            nextID = info.getId() + 1;
        }

        info.setVisible(in.readBoolean());
        info.setLocked(version < 5 ? false : in.readBoolean());
        Integer key = in.readInt();
        obj = table.get(key);
        if (obj == null) {
            try {
                String classname = in.readUTF();
                int len = in.readInt();
                byte[] bytes = new byte[len];
                in.readFully(bytes);
                try {
                    cls = ArtOfIllusion.getClass(classname);
                    con = cls.getConstructor(DataInputStream.class, Scene.class);
                    obj = (Object3D) con.newInstance(new DataInputStream(new ByteArrayInputStream(bytes)), this);
                } catch (SecurityException | ReflectiveOperationException ex) {
                    if (ex instanceof InvocationTargetException) {
                        log.atError().setCause(ex.getCause()).log("Object reading error: {}", ex.getCause().getMessage());
                    } else {
                        log.atError().setCause(ex).log("Object reading error: {}", ex.getMessage());
                    }
                    if (ex instanceof ClassNotFoundException) {
                        errors.add(info.getName() + ": " + Translate.text("errorFindingClass", classname));
                    } else {
                        errors.add(info.getName() + ": " + Translate.text("errorInstantiatingClass", classname));
                    }
                    obj = new NullObject();
                    info.setName("<unreadable> " + info.getName());

                }
                table.put(key, obj);
            } catch (IOException | IllegalArgumentException ex) {
                log.atError().setCause(ex).log("Object reading error: {}", ex.getMessage());
                throw new IOException();
            }
        }
        info.setObject(obj);

        if (version < 2 && obj.getTexture() != null) {
            // Initialize the texture parameters.

            TextureParameter[] texParam = obj.getTextureMapping().getParameters();
            ParameterValue[] paramValue = obj.getParameterValues();
            double[] val = new double[paramValue.length];
            boolean[] perVertex = new boolean[paramValue.length];
            for (int i = 0; i < val.length; i++) {
                val[i] = in.readDouble();
            }
            for (int i = 0; i < perVertex.length; i++) {
                perVertex[i] = in.readBoolean();
            }
            for (int i = 0; i < paramValue.length; i++) {
                if (paramValue[i] == null) {
                    if (perVertex[i]) {
                        paramValue[i] = new VertexParameterValue((Mesh) obj, texParam[i]);
                    } else {
                        paramValue[i] = new ConstantParameterValue(val[i]);
                    }
                }
            }
            obj.setParameterValues(paramValue);
        }

        // Read the tracks for this object.
        int tracks = in.readInt();
        try {
            for (int i = 0; i < tracks; i++) {
                cls = ArtOfIllusion.getClass(in.readUTF());
                con = cls.getConstructor(ObjectInfo.class);
                Track tr = (Track) con.newInstance(info);
                tr.initFromStream(in, this);
                info.addTrack(tr, i);
            }
            if (info.getTracks() == null) {
                info.tracks = new Track[0];
            }
        } catch (IOException | ReflectiveOperationException | SecurityException ex) {
            log.atError().setCause(ex).log("Tracks reading error: {}", ex.getMessage());
            throw new IOException();
        }
        return info;
    }

    /**
     * Save the Scene to a file.
     */
    public void writeToFile(File f) throws IOException {
        int mode = (ArtOfIllusion.getPreferences().getKeepBackupFiles() ? SafeFileOutputStream.OVERWRITE + SafeFileOutputStream.KEEP_BACKUP : SafeFileOutputStream.OVERWRITE);
        SafeFileOutputStream safeOut = new SafeFileOutputStream(f, mode);
        BufferedOutputStream bout = new BufferedOutputStream(safeOut);
        bout.write(FILE_PREFIX);
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(bout))) {
            writeToStream(out);
        }
    }

    /**
     * Write the Scene's representation to an output stream.
     */
    public void writeToStream(DataOutputStream out) throws IOException {

        out.writeShort(5);
        ambientColor.writeToFile(out);
        fogColor.writeToFile(out);
        out.writeBoolean(fog);
        out.writeDouble(fogDist);
        out.writeBoolean(showGrid);
        out.writeBoolean(snapToGrid);
        out.writeDouble(gridSpacing);
        out.writeInt(gridSubdivisions);
        out.writeInt(framesPerSecond);

        // Save the image maps.
        out.writeInt(images.size());
        for (var image : images) {
            out.writeUTF(image.getClass().getName());
            image.writeToStream(out, this);
        }

        // Save the materials.
        out.writeInt(materials.size());
        for (var mat: materials) {
            out.writeUTF(mat.getClass().getName());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mat.writeToFile(new DataOutputStream(bos), this);
            byte[] bytes = bos.toByteArray();
            out.writeInt(bytes.length);
            out.write(bytes, 0, bytes.length);
        }

        // Save the textures.
        out.writeInt(textures.size());
        for (var tex: textures) {
            out.writeUTF(tex.getClass().getName());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            tex.writeToFile(new DataOutputStream(bos), this);
            byte[] bytes = bos.toByteArray();
            out.writeInt(bytes.length);
            out.write(bytes, 0, bytes.length);
        }

        // Save the objects.
        int index = 0;
        Map<Object3D, Integer> table = new Hashtable<>(objects.size());
        out.writeInt(objects.size());
        for (var object: objects) {
            index = writeObjectToFile(out, object, table, index);
        }

        // Record the children of each object.  The format of this will be changed in the
        // next version.
        for (var object: objects) {
            out.writeInt(object.getChildren().length);
            for (ObjectInfo children : object.getChildren()) {
                out.writeInt(indexOf(children));
            }
        }

        // Save the environment mapping information.
        out.writeShort((short) environMode);
        if (environMode == ENVIRON_SOLID) {
            environColor.writeToFile(out);
        } else {
            out.writeInt(textures.lastIndexOf(environTexture));
            out.writeUTF(environMapping.getClass().getName());
            if (environMapping instanceof LayeredMapping) {
                ((LayeredMapping) environMapping).writeToFile(out, this);
            } else {
                environMapping.writeToFile(out);
            }
            for (ParameterValue value : environParamValue) {
                out.writeUTF(value.getClass().getName());
                value.writeToStream(out);
            }
        }

        // Save metadata.
        out.writeInt(metadataMap.size());
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        SearchlistClassLoader loader = new SearchlistClassLoader(getClass().getClassLoader());
        for (ClassLoader cl : PluginRegistry.getPluginClassLoaders()) {
            loader.add(cl);
        }
        Thread.currentThread().setContextClassLoader(loader); // So that plugin classes can be saved correctly.
        for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
            ByteArrayOutputStream value = new ByteArrayOutputStream();
            XMLEncoder encoder = new XMLEncoder(value);
            encoder.setExceptionListener((Exception ex) -> log.atError().setCause(ex).log("Metadata save error: {}", ex.getMessage()));
            encoder.writeObject(entry.getValue());
            encoder.close();
            out.writeUTF(entry.getKey());
            out.writeInt(value.size());
            out.write(value.toByteArray());
        }
        Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    /**
     * Write the information about a single object to a file.
     */
    private int writeObjectToFile(DataOutputStream out, ObjectInfo info, Map<Object3D, Integer> table, int index) throws IOException {
        Integer key;

        info.getCoords().writeToFile(out);
        out.writeUTF(info.getName());
        out.writeInt(info.getId());
        out.writeBoolean(info.isVisible());
        out.writeBoolean(info.isLocked());
        key = table.get(info.getObject());
        if (key == null) {
            out.writeInt(index);
            out.writeUTF(info.getObject().getClass().getName());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            info.getObject().writeToFile(new DataOutputStream(bos), this);
            byte[] bytes = bos.toByteArray();
            out.writeInt(bytes.length);
            out.write(bytes, 0, bytes.length);
            key = index++;
            table.put(info.getObject(), key);
        } else {
            out.writeInt(key);
        }

        // Write the tracks for this object.
        out.writeInt(info.getTracks().length);
        for (var track : info.getTracks()) {
            out.writeUTF(track.getClass().getName());
            track.writeToStream(out, this);
        }
        return index;
    }
}
