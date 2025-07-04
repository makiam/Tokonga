/* Copyright (C) 1999-2012 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.animation.Skeleton;
import artofillusion.animation.Track;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import java.awt.*;
import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The UndoRecord class records a series of commands, allowing the user to undo a previous
 * action.
 */
@Slf4j
public class UndoRecord {

    private final List<Map.Entry<Integer, Object[]>> records = new ArrayList<>();

    private List<SoftReference<?>[]> dataRef;

    private File cacheFile;
    private boolean redo;

    public EditingWindow getView() {
        return view;
    }

    private final EditingWindow view;

    public static final int COPY_OBJECT = 0;
    public static final int COPY_COORDS = 1;
    public static final int COPY_OBJECT_INFO = 2;
    public static final int SET_OBJECT = 3;
    public static final int ADD_OBJECT = 4;
    public static final int DELETE_OBJECT = 5;
    public static final int RENAME_OBJECT = 6;
    public static final int ADD_TO_GROUP = 7;
    public static final int REMOVE_FROM_GROUP = 8;
    public static final int SET_GROUP_CONTENTS = 9;
    public static final int SET_TRACK = 10;
    public static final int SET_TRACK_LIST = 11;
    public static final int COPY_TRACK = 12;
    public static final int COPY_VERTEX_POSITIONS = 13;
    public static final int COPY_SKELETON = 14;
    public static final int SET_MESH_SELECTION = 15;
    public static final int SET_SCENE_SELECTION = 16;
    public static final int USER_DEFINED_ACTION = 1000;

    private static final List<Integer> commandsToCache = Arrays.asList(COPY_OBJECT, COPY_VERTEX_POSITIONS);

    /**
     * Create a new UndoRecord. Initially it represents an empty script. Commands can be added by calling
     * {@link #addCommand addCommand()} or {@link #addCommandAtBeginning addCommandAtBeginning()}.
     *
     * @param win the EditingWindow this record belongs to
     */
    public UndoRecord(EditingWindow win) {
        view = win;
    }

    /**
     * Create a new UndoRecord. Initially it represents an empty script. Commands can be added by calling
     * {@link #addCommand addCommand()} or {@link #addCommandAtBeginning addCommandAtBeginning()}.
     *
     * @param win the EditingWindow this record belongs to
     * @param isRedo whether this record represents "redoing" a previously undone operation
     */
    public UndoRecord(EditingWindow win, boolean isRedo) {
        this(win);
        redo = isRedo;
    }

    /**
     * Create a new UndoRecord whose script contains a single command. Additional commands can be added by calling
     * {@link #addCommand addCommand()} or {@link #addCommandAtBeginning addCommandAtBeginning()}.
     *
     * @param win the EditingWindow this record belongs to
     * @param isRedo whether this record represents "redoing" a previously undone operation
     * @param theCommand the command to add to the script
     * @param commandData data to include as arguments to the command
     */
    public UndoRecord(EditingWindow win, boolean isRedo, int theCommand, Object... commandData) {
        this(win, isRedo);
        addCommand(theCommand, commandData);
    }

    public UndoRecord(EditingWindow win, boolean isRedo, UndoableEdit edit) {
        this(win, isRedo, USER_DEFINED_ACTION, edit);
    }

    /**
     * Get whether this record represents "redoing" a previously undone operation.
     */
    public boolean isRedo() {
        return redo;
    }

    /**
     * Get the list of commands in this record's script.
     */
    public List<Integer> getCommands() {
        return records.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Add a command to the end of this record's script.
     *
     * @param theCommand the command to add to the script
     * @param commandData data to include as arguments to the command
     */
    public final void addCommand(int theCommand, Object... commandData) {
        records.add(new AbstractMap.SimpleEntry<>(theCommand, commandData));
    }

    /**
     * Add a command to the beginning of this record's script.
     *
     * @param theCommand the command to add to the script
     * @param commandData data to include as arguments to the command
     */
    public void addCommandAtBeginning(int theCommand, Object... commandData) {
        records.add(0, new AbstractMap.SimpleEntry<>(theCommand, commandData));
    }

    /**
     * Execute the record's script.
     */
    public UndoRecord execute() {
        UndoRecord redoRecord = new UndoRecord(view, !redo);
        int[] selection = view.getScene().getSelection();
        boolean needRestoreSelection = false;

        try {
            loadFromCache();
        } catch (Exception ex) {
            log.atError().setCause(ex).log("Unable to load data from cache {}", ex.getMessage());
            return redoRecord;
        }
        for (Map.Entry<Integer, Object[]> entry : records) {
            Object[] d = entry.getValue();
            switch (entry.getKey()) {
                case COPY_OBJECT: {
                    Object3D obj1 = (Object3D) d[0];
                    Object3D obj2 = (Object3D) d[1];
                    redoRecord.addCommandAtBeginning(COPY_OBJECT, obj1, obj1.duplicate());
                    obj1.copyObject(obj2);
                    if (view.getScene() != null) {
                        view.getScene().objectModified(obj1);
                    }
                    break;
                }
                case COPY_COORDS: {
                    CoordinateSystem coords1 = (CoordinateSystem) d[0], coords2 = (CoordinateSystem) d[1];
                    redoRecord.addCommandAtBeginning(COPY_COORDS, coords1, coords1.duplicate());
                    coords1.copyCoords(coords2);
                    break;
                }
                case COPY_OBJECT_INFO: {
                    ObjectInfo info1 = (ObjectInfo) d[0];
                    ObjectInfo info2 = (ObjectInfo) d[1];
                    redoRecord.addCommandAtBeginning(COPY_OBJECT_INFO, info1, info1.duplicate());
                    info1.copyInfo(info2);
                    break;
                }
                case SET_OBJECT: {
                    ObjectInfo info = (ObjectInfo) d[0];
                    Object3D obj = (Object3D) d[1];
                    redoRecord.addCommandAtBeginning(SET_OBJECT, info, info.getObject());
                    info.setObject(obj);
                    break;
                }
                case ADD_OBJECT: {
                    ObjectInfo info = (ObjectInfo) d[0];
                    LayoutWindow win = (LayoutWindow) view;
                    int index = (Integer) d[1];
                    win.addObject(info, index, redoRecord);
                    if (info.selected) {
                        win.getScene().addToSelection(index);
                    }
                    needRestoreSelection = true;
                    break;
                }
                case DELETE_OBJECT: {
                    int which = (Integer) d[0];
                    LayoutWindow win = (LayoutWindow) view;
                    win.removeObject(which, redoRecord);
                    needRestoreSelection = true;
                    break;
                }
                case RENAME_OBJECT: {
                    LayoutWindow win = (LayoutWindow) view;
                    int which = (Integer) d[0];
                    String oldName = (win.getScene().getObject(which)).getName();
                    redoRecord.addCommandAtBeginning(RENAME_OBJECT, d[0], oldName);
                    win.setObjectName(which, (String) d[1]);
                    break;
                }
                case ADD_TO_GROUP: {
                    int pos = (Integer) d[2];
                    ObjectInfo group = (ObjectInfo) d[0];
                    ObjectInfo child = (ObjectInfo) d[1];
                    redoRecord.addCommandAtBeginning(REMOVE_FROM_GROUP, group, child);
                    group.addChild(child, pos);
                    break;
                }
                case REMOVE_FROM_GROUP: {
                    ObjectInfo group = (ObjectInfo) d[0];
                    ObjectInfo child = (ObjectInfo) d[1];
                    int pos;
                    for (pos = 0; pos < group.getChildren().length && group.getChildren()[pos] != child; pos++);
                    if (pos == group.getChildren().length) {
                        break;
                    }
                    redoRecord.addCommandAtBeginning(ADD_TO_GROUP, group, child, pos);
                    group.removeChild(child);
                    break;
                }
                case SET_GROUP_CONTENTS: {
                    ObjectInfo group = (ObjectInfo) d[0];
                    var oldObj = group.getChildren();
                    var newObj = (ObjectInfo[]) d[1];
                    redoRecord.addCommandAtBeginning(SET_GROUP_CONTENTS, group, oldObj);
                    for (ObjectInfo objectInfo : oldObj) {
                        objectInfo.setParent(null);
                    }
                    group.setChildren(newObj);
                    break;
                }
                case SET_TRACK: {
                    ObjectInfo info = (ObjectInfo) d[0];
                    int which = (Integer) d[1];
                    redoRecord.addCommandAtBeginning(SET_TRACK, info, d[1], info.getTracks()[which]);
                    info.getTracks()[which] = (Track) d[2];
                    break;
                }
                case SET_TRACK_LIST: {
                    ObjectInfo info = (ObjectInfo) d[0];
                    redoRecord.addCommandAtBeginning(SET_TRACK_LIST, info, info.getTracks());
                    info.setTracks((Track[]) d[1]);
                    break;
                }
                case COPY_TRACK: {
                    Track tr1 = (Track) d[0];
                    Track tr2 = (Track) d[1];
                    redoRecord.addCommandAtBeginning(COPY_TRACK, tr1, tr1.duplicate(tr1.getParent()));
                    tr1.copy(tr2);
                    break;
                }
                case COPY_VERTEX_POSITIONS: {
                    Mesh mesh = (Mesh) d[0];
                    Vec3[] pos = (Vec3[]) d[1];
                    redoRecord.addCommandAtBeginning(COPY_VERTEX_POSITIONS, mesh, mesh.getVertexPositions());
                    mesh.setVertexPositions(pos);
                    if (view.getScene() != null) {
                        view.getScene().objectModified((Object3D) mesh);
                    }
                    break;
                }
                case COPY_SKELETON: {
                    Skeleton s1 = (Skeleton) d[0];
                    Skeleton s2 = (Skeleton) d[1];
                    redoRecord.addCommandAtBeginning(COPY_SKELETON, s1, s1.duplicate());
                    s1.copy(s2);
                    break;
                }
                case SET_MESH_SELECTION: {
                    MeshEditController controller = (MeshEditController) d[0];
                    int mode = (Integer) d[1];
                    boolean[] selected = (boolean[]) d[2];
                    redoRecord.addCommandAtBeginning(SET_MESH_SELECTION, controller, controller.getSelectionMode(), controller.getSelection().clone());
                    controller.setSelectionMode(mode);
                    controller.setSelection(selected);
                    break;
                }
                case SET_SCENE_SELECTION: {
                    int[] selected = (int[]) d[0];
                    needRestoreSelection = true;
                    if (view instanceof LayoutWindow) {
                        ((LayoutWindow) view).setSelection(selected);
                    } else {
                        view.getScene().setSelection(selected);
                    }
                    break;
                }
                case USER_DEFINED_ACTION: {
                    UndoableEdit edit = (UndoableEdit) d[0];
                    if (redo) {
                        edit.redo();
                    } else {
                        edit.undo();
                    }
                    redoRecord.addCommand(UndoRecord.USER_DEFINED_ACTION, edit);
                    break;
                }
            }
        }
        if (needRestoreSelection) {
            redoRecord.addCommand(SET_SCENE_SELECTION, selection);
        }
        view.setModified();
        redoRecord.cacheToDisk();
        return redoRecord;
    }

    /**
     * Cache the data in this record to disk, allowing it to potentially be unloaded from memory.
     */
    protected void cacheToDisk() {
        // We need to make sure the caching gets done 1) after we're certain no more commands will be added, and 2) on a
        // separate thread, so we don't slow down the UI.

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        writeCache();
                    }
                };
                thread.setPriority(Thread.NORM_PRIORITY);
                thread.start();
            }
        });
    }

    /**
     * This routine does the actual caching.
     */
    private synchronized void writeCache() {
        boolean anyToCache = false;
        for (Map.Entry<Integer, Object[]> entry : records) {
            if (commandsToCache.contains(entry.getKey())) {
                anyToCache = true;
            }
        }
        if (!anyToCache) {
            return;
        }

        try {
            dataRef = new ArrayList<>();
            cacheFile = File.createTempFile("undoCache", "dat");
            cacheFile.deleteOnExit();
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
            for (Map.Entry<Integer, Object[]> entry : records) {
                Object[] d = entry.getValue();
                SoftReference<?>[] ref = new SoftReference<?>[d.length];
                dataRef.add(ref);
                int c = entry.getKey();
                if (c == COPY_OBJECT && view.getScene() != null) {
                    out.writeUTF(d[1].getClass().getName());
                    ((Object3D) d[1]).writeToFile(out, view.getScene());
                    ref[1] = new SoftReference<>(d[1]);
                    d[1] = null;
                } else if (c == COPY_VERTEX_POSITIONS) {
                    Vec3[] positions = (Vec3[]) d[1];
                    out.writeInt(positions.length);
                    for (Vec3 v : positions) {
                        v.writeToFile(out);
                    }
                    ref[1] = new SoftReference<>(d[1]);
                    d[1] = null;
                }
            }
            out.close();
        } catch (IOException ex) {
            // Ignore errors, and just keep the data in memory.
        }
    }

    /**
     * Ensure all data is in memory, loading from disk if necessary.
     */
    private synchronized void loadFromCache() throws Exception {
        // Restore the soft references, and see if any data needs to be loaded.

        if (cacheFile == null) {
            return;
        }
        boolean anyToLoad = false;
        for (int i = 0; i < dataRef.size(); i++) {
            Object[] d = records.get(i).getValue();
            SoftReference<?>[] ref = dataRef.get(i);
            if (ref != null) {
                for (int j = 0; j < ref.length; j++) {
                    if (ref[j] != null) {
                        d[j] = ref[j].get();
                        if (d[j] == null) {
                            anyToLoad = true;
                        }
                    }
                }
            }
        }

        // Load the data from disk.
        if (anyToLoad) {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));
            for (Map.Entry<Integer, Object[]> entry : records) {
                Object[] d = entry.getValue();
                int c = entry.getKey();
                if (c == COPY_OBJECT && view.getScene() != null) {
                    Class<?> cls = ArtOfIllusion.getClass(in.readUTF());
                    Constructor<?> con = cls.getDeclaredConstructor(DataInputStream.class, Scene.class);
                    d[1] = con.newInstance(in, view.getScene());
                } else if (c == COPY_VERTEX_POSITIONS) {
                    Vec3[] positions = new Vec3[in.readInt()];
                    for (int j = 0; j < positions.length; j++) {
                        positions[j] = new Vec3(in);
                    }
                    d[1] = positions;
                }
            }
            in.close();
        }
        cacheFile.delete();
        dataRef = null;
    }

    public String getName() {
        if (records.isEmpty()) {
            return "";
        }
        int command = records.get(0).getKey();
        if (command != UndoRecord.USER_DEFINED_ACTION) {
            return "";
        }
        return ((UndoableEdit) records.get(0).getValue()[0]).getName();
    }
}
