/* Copyright (C) 2004-2007 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import java.io.*;
import java.util.*;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * ExternalObject is an Object3D that is stored in a separate file.
 */
@Slf4j
public class ExternalObject extends ObjectWrapper {

    private File externalFile;
    private int objectId;
    private String objectName;

    /**
     * Get an error message which describes why the object could not be loaded,
     * or null if it was loaded successfully.
     */
    @Getter
    private String loadingError;

    private boolean includeChildren;

    /**
     * Create an ExternalObject from a file.
     *
     * @param file the scene file containing the object
     * @param name the name of the object to load
     */
    public ExternalObject(File file, String name) {
        this();
        externalFile = file;
        objectName = name;
        includeChildren = true;
    }

    /**
     * This constructor is used internally.
     */
    private ExternalObject() {
        theObject = new NullObject();
    }

    /**
     * Get the name of the object in the external scene.
     */
    public String getExternalObjectName() {
        return objectName;
    }

    /**
     * Set the name of the object in the external scene.
     */
    public void setExternalObjectName(String name) {
        objectName = name;
    }

    /**
     * Get the id of the object in the external scene.
     */
    public int getExternalObjectId() {
        return objectId;
    }

    /**
     * Set the id of the object in the external scene.
     */
    public void setExternalObjectId(int id) {
        objectId = id;
    }

    /**
     * Get whether to include children of the external object.
     */
    public boolean getIncludeChildren() {
        return includeChildren;
    }

    /**
     * Set whether to include children of the external object.
     */
    public void setIncludeChildren(boolean include) {
        includeChildren = include;
    }

    /**
     * Get the path to the external scene file.
     */
    public File getExternalSceneFile() {
        return externalFile;
    }

    /**
     * Set the path to the external scene file.
     */
    public void setExternalSceneFile(File file) {
        externalFile = file;
    }

    /**
     * Reload the external object from its file.
     */
    public void reloadObject() {
        theObject = new NullObject();
        loadingError = null;
        try {
            if (!externalFile.isFile()) {
                loadingError = Translate.text("externalObject.sceneNotFound", externalFile.getAbsolutePath());
                return;
            }
            Scene scene = new Scene(externalFile, true);
            ObjectInfo foundObject = null;
            for (ObjectInfo info : scene.getObjects()) {
                if (!info.getName().equals(objectName)) {
                    continue;
                }
                if (info.getId() == objectId) {
                    foundObject = info;
                    break;
                }
                if (foundObject == null) {
                    foundObject = info; // Right name but wrong ID.  Tentatively accept it, but keep looking.
                }
            }
            if (foundObject == null) {
                loadingError = Translate.text("externalObject.objectNotFound", externalFile.getAbsolutePath(), objectName);
            } else {
                if (includeChildren && foundObject.getChildren().length > 0) {
                    // Create an ObjectCollection containing the object and all its children.

                    ArrayList<ObjectInfo> allObjects = new ArrayList<>();
                    addObjectsToList(foundObject, allObjects, foundObject.getCoords().toLocal());
                    theObject = new ExternalObjectCollection(allObjects);
                } else {
                    theObject = foundObject.getObject();
                }
            }
        } catch (IOException ex) {
            // If anything goes wrong, use a null object and return an error message.
            loadingError = ex.getMessage();
            log.atError().setCause(ex).log("Error loading external object: {}", loadingError);

        }
    }

    /**
     * Add an object and all its children to a list.
     */
    private void addObjectsToList(ObjectInfo obj, List<ObjectInfo> allObjects, Mat4 transform) {
        obj.getCoords().transformCoordinates(transform);
        allObjects.add(obj);
        for (ObjectInfo child : obj.getChildren()) {
            addObjectsToList(child, allObjects, transform);
        }
    }

    /**
     * Create a new object which is an exact duplicate of this one.
     */
    @Override
    public ExternalObject duplicate() {
        ExternalObject obj = new ExternalObject(externalFile, objectName);
        obj.theObject = theObject;
        obj.includeChildren = includeChildren;
        return obj;
    }

    /**
     * Copy all the properties of another object, to make this one identical to it. If the
     * two objects are of different classes, this will throw a ClassCastException.
     */
    @Override
    public void copyObject(Object3D obj) {
        ExternalObject eo = (ExternalObject) obj;
        externalFile = eo.externalFile;
        objectName = eo.objectName;
        theObject = eo.theObject;
        includeChildren = eo.includeChildren;
    }

    /**
     * ExternalObjects cannot be resized, since they are entirely defined by a separate file.
     */
    @Override
    public void setSize(double xsize, double ysize, double zsize) {
        // ExternalObjects cannot be resized, since they are entirely defined by a separate file.
    }

    /**
     * If the object can be edited by the user, isEditable() should be overridden to return true.
     * edit() should then create a window and allow the user to edit the object.
     */
    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public void edit(EditingWindow parent, ObjectInfo info, Runnable cb) {
        SwingUtilities.invokeLater(() -> new ExternalObjectEditingWindow(parent, this, info, cb).setVisible(true));
    }

    /**
     * This method tells whether textures can be assigned to the object. Objects for which
     * it makes no sense to assign a texture (curves, lights, etc.) should override this
     * method to return false.
     */
    @Override
    public boolean canSetTexture() {
        return false;
    }

    /**
     * This method tells whether materials can be assigned to the object. The default
     * implementation will give the correct result for most objects, but subclasses
     * can override this if necessary.
     */
    @Override
    public boolean canSetMaterial() {
        return false;
    }

    /**
     * The following method writes the object's data to an output stream.
     */
    @Override
    public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
        super.writeToFile(out, theScene);
        out.writeShort(1);
        out.writeUTF(externalFile.getAbsolutePath());
        out.writeUTF(findRelativePath(theScene));
        out.writeUTF(objectName);
        out.writeInt(objectId);
        out.writeBoolean(includeChildren);
    }

    /**
     * Find the relative path from the scene file containing this object to the external scene.
     */
    private String findRelativePath(Scene theScene) {
        String scenePath = null;
        String externalPath = null;
        try {
            scenePath = new File(theScene.getDirectory()).getCanonicalPath();
            externalPath = externalFile.getCanonicalPath();
        } catch (IOException ex) {
            // We couldn't get the canonical name for one of the files.

            return "";
        }

        // Break each path into pieces, and find how much they share in common.
        String splitExpr = File.separator;
        if ("\\".equals(splitExpr)) {
            splitExpr = "\\\\";
        }
        String[] scenePathParts = scenePath.split(splitExpr);
        String[] externalPathParts = externalPath.split(splitExpr);
        int numCommon;
        for (numCommon = 0; numCommon < scenePathParts.length && numCommon < externalPathParts.length && scenePathParts[numCommon].equals(externalPathParts[numCommon]); numCommon++);
        StringBuilder relPath = new StringBuilder();
        for (int i = numCommon; i < scenePathParts.length; i++) {
            relPath.append("..").append(File.separator);
        }
        for (int i = numCommon; i < externalPathParts.length; i++) {
            if (i > numCommon) {
                relPath.append(File.separator);
            }
            relPath.append(externalPathParts[i]);
        }
        return relPath.toString();
    }

    /**
     * Recreate an ExternalObject by reading in the serialized representation written by writeToFile().
     */
    public ExternalObject(DataInputStream in, Scene theScene) throws IOException {
        super(in, theScene);
        short version = in.readShort();
        if (version < 0 || version > 1) {
            throw new InvalidObjectException("Unknown version: " + version);
        }
        File f = new File(in.readUTF());
        String relPath = in.readUTF();
        externalFile = new File(theScene.getDirectory(), relPath);
        if (!externalFile.isFile() && f.isFile()) {
            externalFile = f;
        }
        objectName = in.readUTF();
        objectId = (version > 0 ? in.readInt() : -1);
        includeChildren = (version > 0 ? in.readBoolean() : false);
        reloadObject();
    }

    /**
     * This class is the ObjectCollection used to represent a set of external objects.
     */
    private class ExternalObjectCollection extends ObjectCollection {

        private ArrayList<ObjectInfo> objects;

        ExternalObjectCollection(ArrayList<ObjectInfo> objects) {
            this.objects = objects;
        }

        @Override
        protected Enumeration<ObjectInfo> enumerateObjects(ObjectInfo info, boolean interactive, Scene scene) {
            return Collections.enumeration(objects);
        }

        @Override
        public ExternalObjectCollection duplicate() {
            return new ExternalObjectCollection(objects);
        }

        @Override
        public void copyObject(Object3D obj) {
            objects = ((ExternalObjectCollection) obj).objects;
        }

        @Override
        public void setSize(double xsize, double ysize, double zsize) {
        }

        @Override
        public Keyframe getPoseKeyframe() {
            return null;
        }

        @Override
        public void applyPoseKeyframe(Keyframe k) {
        }
    }
}
