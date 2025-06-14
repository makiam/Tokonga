/* Copyright (C) 2004-2006 by Peter Eastman
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation.distortion;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.widget.*;
import java.io.*;

/**
 * This is a Track which applies a SkeletonDistortion to an object.
 */
public class SkeletonShapeTrack extends Track<SkeletonShapeTrack> {

    private ObjectInfo info;
    private Timecourse tc;
    private int smoothingMethod;
    private boolean useGestures;
    private WeightTrack theWeight;

    public SkeletonShapeTrack(ObjectInfo info) {
        super("Skeleton Shape");
        this.info = info;
        tc = new Timecourse(new Keyframe[0], new double[0], new Smoothness[0]);
        smoothingMethod = Timecourse.INTERPOLATING;
        useGestures = true;
        theWeight = new WeightTrack(this);
    }

    /**
     * Get whether to reshape the mesh based on its gestures.
     */
    public boolean getUseGestures() {
        return useGestures;
    }

    /**
     * Set whether to reshape the mesh based on its gestures.
     */
    public void setUseGestures(boolean use) {
        useGestures = use;
    }

    /**
     * Reshape the object based on this track.
     */
    @Override
    public void apply(double time) {
        SkeletonShapeKeyframe key = (SkeletonShapeKeyframe) tc.evaluate(time, smoothingMethod);
        if (key == null) {
            return;
        }
        double weight = theWeight.getWeight(time);
        Actor actor = null;
        if (useGestures) {
            actor = Actor.getActor(info.getObject());
        }
        if (weight > 0.0) {
            info.addDistortion(new SkeletonShapeDistortion(key.getSkeleton().duplicate(), weight, actor));
        }
    }

    /**
     * Create a duplicate of this track.
     */
    @Override
    public SkeletonShapeTrack duplicate(Object obj) {
        SkeletonShapeTrack t = new SkeletonShapeTrack((ObjectInfo) obj);
        t.name = name;
        t.enabled = enabled;

        t.smoothingMethod = smoothingMethod;
        t.useGestures = useGestures;
        t.tc = tc.duplicate(((ObjectInfo) obj).getObject());
        t.theWeight = theWeight.duplicate(t);
        return t;
    }

    /**
     * Make this track identical to another one.
     */
    @Override
    public void copy(SkeletonShapeTrack track) {

        name = track.name;
        enabled = track.enabled;

        smoothingMethod = track.smoothingMethod;
        useGestures = track.useGestures;
        tc = track.tc.duplicate(info.getObject());
        theWeight = track.theWeight.duplicate(this);
    }

    /**
     * Get a list of all keyframe times for this track.
     */
    @Override
    public double[] getKeyTimes() {
        return tc.getTimes();
    }

    /**
     * Get the timecourse describing this track.
     */
    @Override
    public Timecourse getTimecourse() {
        return tc;
    }

    /**
     * Set a keyframe at the specified time.
     */
    @Override
    public void setKeyframe(double time, Keyframe k, Smoothness s) {
        tc.addTimepoint(k, time, s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Keyframe setKeyframe(double time) {
        Keyframe k = tc.evaluate(time, smoothingMethod);
        if (k == null) {
            k = new SkeletonShapeKeyframe(info.getObject(), info.getObject().getSkeleton().duplicate());
        } else {
            k = k.duplicate();
        }
        tc.addTimepoint(k, time, new Smoothness());
        return k;
    }

    /**
     * Move a keyframe to a new time, and return its new position in the list.
     */
    @Override
    public int moveKeyframe(int which, double time) {
        return tc.moveTimepoint(which, time);
    }

    /**
     * Delete the specified keyframe.
     */
    @Override
    public void deleteKeyframe(int which) {
        tc.removeTimepoint(which);
    }

    /**
     * This track is null if it has no keyframes.
     */
    @Override
    public boolean isNullTrack() {
        return (tc.getTimes().length == 0);
    }

    /**
     * This has a single child track.
     */
    @Override
    public Track[] getSubtracks() {
        return new Track[]{theWeight};
    }

    /**
     * Determine whether this track can be added as a child of an object.
     */
    @Override
    public boolean canAcceptAsParent(Object obj) {
        return (obj instanceof ObjectInfo && ((ObjectInfo) obj).getSkeleton() != null);
    }

    /**
     * Get the parent object of this track.
     */
    @Override
    public Object getParent() {
        return info;
    }

    /**
     * Set the parent object of this track.
     */
    @Override
    public void setParent(Object obj) {
        info = (ObjectInfo) obj;
    }

    /**
     * Get the smoothing method for this track.
     */
    @Override
    public int getSmoothingMethod() {
        return smoothingMethod;
    }

    /**
     * Set the smoothing method for this track.
     */
    public void setSmoothingMethod(int method) {
        smoothingMethod = method;
    }

    /**
     * Get the allowed range for graphable values. This returns a 2D array, where elements
     * [n][0] and [n][1] are the minimum and maximum allowed values, respectively, for
     * the nth graphable value.
     */
    @Override
    public double[][] getValueRange() {
        return new double[][]{{-Double.MAX_VALUE, Double.MAX_VALUE}};
    }

    /**
     * Write a serialized representation of this track to a stream.
     */
    @Override
    public void writeToStream(DataOutputStream out, Scene scene) throws IOException {


        out.writeShort(0); // Version number
        out.writeUTF(name);
        out.writeBoolean(enabled);

        out.writeInt(smoothingMethod);
        out.writeBoolean(useGestures);

        double[] t = tc.getTimes();
        Smoothness[] s = tc.getSmoothness();
        Keyframe[] v = tc.getValues();

        out.writeInt(t.length);
        for (int i = 0; i < t.length; i++) {
            out.writeDouble(t[i]);
            v[i].writeToStream(out);
            s[i].writeToStream(out);
        }

        theWeight.writeToStream(out, scene);
    }

    /**
     * Initialize this tracked based on its serialized representation as written by writeToStream().
     */
    @Override
    public void initFromStream(DataInputStream in, Scene scene) throws IOException {
        short version = in.readShort();
        if (version != 0) {
            throw new InvalidObjectException("");
        }
        name = in.readUTF();
        enabled = in.readBoolean();
        smoothingMethod = in.readInt();
        useGestures = in.readBoolean();
        int keys = in.readInt();
        double[] t = new double[keys];
        Smoothness[] s = new Smoothness[keys];
        Keyframe[] v = new Keyframe[keys];
        for (int i = 0; i < keys; i++) {
            t[i] = in.readDouble();
            v[i] = new SkeletonShapeKeyframe(in, info.getObject());
            s[i] = new Smoothness(in);
        }
        tc = new Timecourse(v, t, s);
        theWeight.initFromStream(in, scene);
    }

    /**
     * Present a window in which the user can edit the specified keyframe.
     */
    @Override
    public void editKeyframe(final LayoutWindow win, int which) {
        Runnable callback = () -> win.getScore().tracksModified(true);
        SkeletonShapeEditorWindow ed = new SkeletonShapeEditorWindow(win, Translate.text("editKeyframe"), this, which, callback);

        for (ViewerCanvas view : ed.getAllViews()) {
            ((MeshViewer) view).setScene(win.getScene(), info);
        }
        ed.setVisible(true);
    }

    /**
     * This method presents a window in which the user can edit the track.
     */
    @Override
    public void edit(LayoutWindow win) {
        final BComboBox smoothChoice;
        BTextField nameField = new BTextField(getName());
        smoothChoice = new BComboBox(new String[]{
            Translate.text("Discontinuous"),
            Translate.text("Linear"),
            Translate.text("Interpolating"),
            Translate.text("Approximating")
        });
        smoothChoice.setSelectedIndex(smoothingMethod);
        BCheckBox gesturesBox = new BCheckBox(Translate.text("useGesturesToShapeMesh"), useGestures);
        gesturesBox.setEnabled(Actor.getActor(info.getObject()) != null);
        ComponentsDialog dlg = new ComponentsDialog(win, Translate.text("skeletonShapeTrackTitle"), new Widget[]{nameField, smoothChoice, gesturesBox}, new String[]{Translate.text("trackName"), Translate.text("SmoothingMethod"), null});
        if (!dlg.clickedOk()) {
            return;
        }
        win.setUndoRecord(new UndoRecord(win, false, UndoRecord.COPY_OBJECT_INFO, info, info.duplicate()));
        this.setName(nameField.getText());
        smoothingMethod = smoothChoice.getSelectedIndex();
        useGestures = gesturesBox.getState();
    }
}
