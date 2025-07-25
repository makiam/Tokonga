/* Copyright (C) 2002-2004 by Peter Eastman
   Changes copyright (C) 2020-2023 by Maksim Khramov

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
import artofillusion.procedural.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.io.*;

/**
 * This is a Track which uses a procedure to deform object.
 */
public class CustomDistortionTrack extends Track<CustomDistortionTrack> implements ProcedureOwner {

    ObjectInfo info;
    final Procedure proc;
    Timecourse tc;
    TextureParameter[] parameter;
    int smoothingMethod;
    int procVersion;
    WeightTrack theWeight;
    boolean worldCoords;

    public CustomDistortionTrack(ObjectInfo info) {
        super("Deform");
        this.info = info;
        proc = new Procedure(new OutputModule("X", "X", 0.0),
                new OutputModule("Y", "Y", 0.0),
                new OutputModule("Z", "Z", 0.0));
        parameter = new TextureParameter[0];
        tc = new Timecourse(new Keyframe[0], new double[0], new Smoothness[0]);
        smoothingMethod = Timecourse.INTERPOLATING;
        worldCoords = false;
        theWeight = new WeightTrack(this);
    }

    /* Modify the position of the object. */
    @Override
    public void apply(double time) {
        PointInfo point = new PointInfo();

        ArrayKeyframe params = (ArrayKeyframe) tc.evaluate(time, smoothingMethod);

        point.t = time;
        if (params != null) {
            point.param = params.val;
        }
        double weight = theWeight.getWeight(time);
        if (worldCoords) {
            info.addDistortion(new CustomDistortion(proc, procVersion, point, weight, info.getCoords().fromLocal(), info.getCoords().toLocal()));
        } else {
            info.addDistortion(new CustomDistortion(proc, procVersion, point, weight, null, null));
        }
    }

    /* Create a duplicate of this track. */
    @Override
    public CustomDistortionTrack duplicate(Object obj) {
        CustomDistortionTrack t = new CustomDistortionTrack((ObjectInfo) obj);

        t.name = name;
        t.enabled = enabled;

        t.proc.copy(proc);
        t.worldCoords = worldCoords;
        t.smoothingMethod = smoothingMethod;
        t.tc = tc.duplicate(obj);
        t.theWeight = theWeight.duplicate(t);
        return t;
    }

    /* Make this track identical to another one. */
    @Override
    public void copy(CustomDistortionTrack track) {

        name = track.name;
        enabled = track.enabled;

        proc.copy(track.proc);
        worldCoords = track.worldCoords;
        smoothingMethod = track.smoothingMethod;
        tc = track.tc.duplicate(info);
        theWeight = track.theWeight.duplicate(this);
    }

    /* Get a list of all keyframe times for this track. */
    @Override
    public double[] getKeyTimes() {
        return tc.getTimes();
    }

    /* Get the timecourse describing this track. */
    @Override
    public Timecourse getTimecourse() {
        return tc;
    }

    /* Set a keyframe at the specified time. */
    @Override
    public void setKeyframe(double time, Keyframe k, Smoothness s) {
        tc.addTimepoint(k, time, s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Keyframe setKeyframe(double time) {
        if (parameter.length == 0) {
            return null; // There are no parameters to keyframe.
        }
        ArrayKeyframe params = (ArrayKeyframe) tc.evaluate(time, smoothingMethod);
        double[] p;
        if (params == null) {
            p = getDefaultGraphValues();
        } else {
            p = new double[params.val.length];
            System.arraycopy(params.val, 0, p, 0, p.length);
        }
        ArrayKeyframe k = new ArrayKeyframe(p);
        tc.addTimepoint(k, time, new Smoothness());
        return k;
    }

    /* Move a keyframe to a new time, and return its new position in the list. */
    @Override
    public int moveKeyframe(int which, double time) {
        return tc.moveTimepoint(which, time);
    }

    /* Delete the specified keyframe. */
    @Override
    public void deleteKeyframe(int which) {
        tc.removeTimepoint(which);
    }

    /* Procedural tracks are never null. */
    @Override
    public boolean isNullTrack() {
        return false;
    }

    /* This has a single child track. */
    @Override
    public Track[] getSubtracks() {
        return new Track[]{theWeight};
    }

    /* Get the parent object of this track. */
    @Override
    public Object getParent() {
        return info;
    }

    /* Set the parent object of this track. */
    @Override
    public void setParent(Object obj) {
        info = (ObjectInfo) obj;
    }

    /* Get the smoothing method for this track. */
    @Override
    public int getSmoothingMethod() {
        return smoothingMethod;
    }

    /* Set the smoothing method for this track. */
    public void setSmoothingMethod(int method) {
        smoothingMethod = method;
    }

    /* Get the names of all graphable values for this track. */
    @Override
    public String[] getValueNames() {
        String[] names = new String[parameter.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = parameter[i].name;
        }
        return names;
    }

    /* Get the default list of graphable values (for a track which has no keyframes). */
    @Override
    public double[] getDefaultGraphValues() {
        double[] val = new double[parameter.length];
        for (int i = 0; i < val.length; i++) {
            val[i] = parameter[i].defaultVal;
        }
        return val;
    }

    /* Get the allowed range for graphable values.  This returns a 2D array, where elements
     [n][0] and [n][1] are the minimum and maximum allowed values, respectively, for
     the nth graphable value. */
    @Override
    public double[][] getValueRange() {
        double[][] range = new double[parameter.length][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = parameter[i].minVal;
            range[i][1] = parameter[i].maxVal;
        }
        return range;
    }

    /**
     * Find all the parameters for the procedure.
     */
    private TextureParameter[] findParameters() {
        var  modules = proc.getModules();
        int count = 0;

        for (var module : modules) {
            if (module instanceof ParameterModule) {
                count++;
            }
        }
        TextureParameter[] params = new TextureParameter[count];
        count = 0;
        for (var module : modules) {
            if (module instanceof ParameterModule) {
                params[count] = ((ParameterModule) module).getParameter(this);
                ((ParameterModule) module).setIndex(count++);
            }
        }
        return params;
    }

    /* Write a serialized representation of this track to a stream. */
    @Override
    public void writeToStream(DataOutputStream out, Scene scene) throws IOException {

        out.writeShort(0); // Version number
        out.writeUTF(name);
        out.writeBoolean(enabled);

        proc.writeToStream(out, scene);
        out.writeInt(smoothingMethod);
        out.writeBoolean(worldCoords);

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
        proc.readFromStream(in, scene);
        smoothingMethod = in.readInt();
        worldCoords = in.readBoolean();
        int keys = in.readInt();
        double[] t = new double[keys];
        Smoothness[] s = new Smoothness[keys];
        Keyframe[] v = new Keyframe[keys];
        for (int i = 0; i < keys; i++) {
            t[i] = in.readDouble();
            v[i] = new ArrayKeyframe(in, this);
            s[i] = new Smoothness(in);
        }
        tc = new Timecourse(v, t, s);
        theWeight.initFromStream(in, scene);
        parameter = findParameters();
    }

    /* Present a window in which the user can edit the specified keyframe. */
    @Override
    public void editKeyframe(LayoutWindow win, int which) {
        ArrayKeyframe key = (ArrayKeyframe) tc.getValues()[which];
        Smoothness s = tc.getSmoothness()[which];
        double time = tc.getTimes()[which];
        ValueField timeField = new ValueField(time, ValueField.NONE, 5);
        ValueSlider s1Slider = new ValueSlider(0.0, 1.0, 100, s.getLeftSmoothness());
        final ValueSlider s2Slider = new ValueSlider(0.0, 1.0, 100, s.getRightSmoothness());
        final BCheckBox sameBox = new BCheckBox(Translate.text("separateSmoothness"), !s.isForceSame());
        Widget[] widget = new Widget[parameter.length + 5];
        String[] label = new String[parameter.length + 5];

        for (int i = 0; i < parameter.length; i++) {
            widget[i] = parameter[i].getEditingWidget(key.val[i]);
            label[i] = parameter[i].name;
        }
        sameBox.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent() {
                s2Slider.setEnabled(sameBox.getState());
            }
        });
        s2Slider.setEnabled(sameBox.getState());
        int n = parameter.length;
        widget[n] = timeField;
        widget[n + 1] = sameBox;
        widget[n + 2] = new BLabel(Translate.text("Smoothness") + ':');
        widget[n + 3] = s1Slider;
        widget[n + 4] = s2Slider;
        label[n] = Translate.text("Time");
        label[n + 3] = "(" + Translate.text("left") + ")";
        label[n + 4] = "(" + Translate.text("right") + ")";
        ComponentsDialog dlg = new ComponentsDialog(win, Translate.text("editKeyframe"), widget, label);
        if (!dlg.clickedOk()) {
            return;
        }
        win.setUndoRecord(new UndoRecord(win, false, UndoRecord.COPY_TRACK, this, duplicate(info)));
        for (int i = 0; i < parameter.length; i++) {
            if (widget[i] instanceof ValueField) {
                key.val[i] = ((ValueField) widget[i]).getValue();
            } else {
                key.val[i] = ((ValueSlider) widget[i]).getValue();
            }
        }
        if (sameBox.getState()) {
            s.setSmoothness(s1Slider.getValue(), s2Slider.getValue());
        } else {
            s.setSmoothness(s1Slider.getValue());
        }
        moveKeyframe(which, timeField.getValue());
    }

    /**
     * This method presents a window in which the user can edit the track.
     */
    @Override
    public void edit(LayoutWindow win) {
        ProcedureEditor editor = new ProcedureEditor(proc, this, win.getScene());
        editor.setEditingWindow(win);
    }

    /**
     * Get the title of the procedure's editing window.
     */
    @Override
    public String getWindowTitle() {
        return "Custom Deformation Track";
    }

    /**
     * Determine whether the procedure may contain View Angle modules.
     */
    @Override
    public boolean allowViewAngle() {
        return true;
    }

    /**
     * This is called when the user clicks OK in the procedure editor.
     */
    @Override
    public void acceptEdits(ProcedureEditor editor) {
        EditingWindow win = editor.getEditingWindow();
        win.setUndoRecord(new UndoRecord(win, false, UndoRecord.COPY_OBJECT_INFO, info, info.duplicate()));
        TextureParameter[] newparams = findParameters();
        int[] index = new int[newparams.length];
        for (int i = 0; i < newparams.length; i++) {
            index[i] = -1;
            for (int j = 0; j < parameter.length; j++) {
                if (parameter[j].equals(newparams[i])) {
                    index[i] = j;
                }
            }
        }
        parameter = newparams;
        Keyframe[] key = tc.getValues();
        for (int i = 0; i < key.length; i++) {
            double[] newval = new double[parameter.length];
            for (int j = 0; j < newval.length; j++) {
                if (index[j] > -1) {
                    newval[j] = ((ArrayKeyframe) key[i]).val[index[j]];
                } else {
                    newval[j] = parameter[j].defaultVal;
                }
            }
            ((ArrayKeyframe) key[i]).val = newval;
        }
        procVersion++;
        ((LayoutWindow) win).getScore().finishEditingTrack(this);
    }

    /**
     * Display the Properties dialog.
     */
    @Override
    public void editProperties(ProcedureEditor editor) {
        BComboBox smoothChoice = new BComboBox(new String[]{
            Translate.text("Discontinuous"),
            Translate.text("Linear"),
            Translate.text("Interpolating"),
            Translate.text("Approximating")
        });
        smoothChoice.setSelectedIndex(smoothingMethod);
        BComboBox coordsChoice = new BComboBox(new String[]{
            Translate.text("Local"),
            Translate.text("World")
        });
        coordsChoice.setSelectedIndex(worldCoords ? 1 : 0);
        ComponentsDialog dlg = new ComponentsDialog(editor.getParentFrame(), Translate.text("deformTrackTitle"), new Widget[]{smoothChoice, coordsChoice}, new String[]{Translate.text("SmoothingMethod"), Translate.text("CoordinateSystem")});
        if (!dlg.clickedOk()) {
            return;
        }
        editor.saveState(false);
        smoothingMethod = smoothChoice.getSelectedIndex();
        worldCoords = (coordsChoice.getSelectedIndex() == 1);
    }
}
