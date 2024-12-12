/* Copyright (C) 2002-2013 by Peter Eastman
   Changes copyright (C) 2020-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.awt.Dimension;

import java.io.*;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 * This is a Track which controls the value of a texture parameter.
 */
public class TextureTrack extends Track {

    @Getter(AccessLevel.PRIVATE) ObjectInfo info;
    Timecourse tc;
    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE) int smoothingMethod;

    WeightTrack theWeight;
    TextureParameter[] param;

    public TextureTrack(ObjectInfo info) {
        super("Texture");
        this.info = info;
        tc = new Timecourse(new Keyframe[0], new double[0], new Smoothness[0]);
        smoothingMethod = Timecourse.INTERPOLATING;
        theWeight = new WeightTrack(this);
        param = info.getObject().getParameters();
    }

    /* Modify the parameters of the object. */
    @Override
    public void apply(double time) {
        ArrayKeyframe val = (ArrayKeyframe) tc.evaluate(time, smoothingMethod);
        if (val == null) {
            return;
        }
        TextureParameter[] texParam = info.getObject().getParameters();
        ParameterValue[] paramValue = info.getObject().getParameterValues();
        double weight = theWeight.getWeight(time);
        for (int i = 0; i < texParam.length; i++) {
            for (int j = 0; j < param.length; j++) {
                if (!texParam[i].equals(param[j])) {
                    continue;
                }
                double v;
                if (weight == 1.0) {
                    v = val.val[j];
                } else {
                    v = (1.0 - weight) * paramValue[i].getAverageValue() + weight * val.val[j];
                }
                if (v < texParam[i].minVal) {
                    v = texParam[i].minVal;
                }
                if (v > texParam[i].maxVal) {
                    v = texParam[i].maxVal;
                }
                paramValue[i] = new ConstantParameterValue(v);
            }
        }
        info.getObject().setParameterValues(paramValue);
    }

    /* Create a duplicate of this track. */
    @Override
    public TextureTrack duplicate(Object obj) {
        TextureTrack t = new TextureTrack((ObjectInfo) obj);

        t.name = name;
        t.enabled = enabled;
        t.quantized = quantized;
        t.smoothingMethod = smoothingMethod;
        t.tc = tc.duplicate((ObjectInfo) obj);
        t.theWeight = theWeight.duplicate(t);
        t.param = param;
        return t;
    }

    /* Make this track identical to another one. */
    @Override
    public void copy(Track tr) {
        TextureTrack t = (TextureTrack) tr;

        name = t.name;
        enabled = t.enabled;
        quantized = t.quantized;
        smoothingMethod = t.smoothingMethod;
        tc = t.tc.duplicate(info);
        theWeight = t.theWeight.duplicate(this);
        param = t.param;
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
        TextureParameter[] texParam = info.getObject().getParameters();
        ParameterValue[] paramValue = info.getObject().getParameterValues();
        double[] d = new double[param.length];

        for (int i = 0; i < texParam.length; i++) {
            for (int j = 0; j < param.length; j++) {
                if (texParam[i].equals(param[j])) {
                    d[j] = paramValue[i].getAverageValue();
                }
            }
        }
        Keyframe k = new ArrayKeyframe(d);
        tc.addTimepoint(k, time, new Smoothness());
        return k;
    }

    /**
     * Set a keyframe at the specified time, based on the current state of the Scene,
     * if and only if the Scene does not match the current state of the track. Return
     * the new Keyframe, or null if none was set.
     */
    @Override
    public Keyframe setKeyframeIfModified(double time) {
        TextureParameter[] texParam = info.getObject().getParameters();
        ParameterValue[] paramValue = info.getObject().getParameterValues();
        double[] d = new double[param.length];
        boolean change = false;
        ArrayKeyframe key = (ArrayKeyframe) tc.evaluate(time, smoothingMethod);

        for (int i = 0; i < texParam.length; i++) {
            for (int j = 0; j < param.length; j++) {
                if (texParam[i].equals(param[j])) {
                    d[j] = paramValue[i].getAverageValue();
                    if (key == null || d[j] != key.val[j]) {
                        change = true;
                    }
                }
            }
        }
        if (change) {
            Keyframe k = new ArrayKeyframe(d);
            tc.addTimepoint(k, time, new Smoothness());
            return k;
        }
        return null;
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

    /* This track is null if it has no keyframes. */
    @Override
    public boolean isNullTrack() {
        return (tc.getTimes().length == 0);
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
        String[] names = new String[param.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = param[i].name;
        }
        return names;
    }

    /* Get the default list of graphable values (for a track which has no keyframes). */
    @Override
    public double[] getDefaultGraphValues() {
        TextureParameter[] texParam = info.getObject().getParameters();
        ParameterValue[] paramValue = info.getObject().getParameterValues();
        double[] d = new double[param.length];

        for (int i = 0; i < texParam.length; i++) {
            for (int j = 0; j < param.length; j++) {
                if (texParam[i].equals(param[j])) {
                    d[j] = paramValue[i].getAverageValue();
                }
            }
        }
        return d;
    }

    /* Get the allowed range for graphable values.  This returns a 2D array, where elements
     [n][0] and [n][1] are the minimum and maximum allowed values, respectively, for
     the nth graphable value. */
    @Override
    public double[][] getValueRange() {
        double[][] range = new double[param.length][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = param[i].minVal;
            range[i][1] = param[i].maxVal;
        }
        return range;
    }

    /* This should be called whenever the list of texture parameters for the object
     changes.  It updates this track's list of parameters to remove any that no
     longer exist. */
    public void parametersChanged() {
        TextureParameter[] texParam = info.getObject().getParameters();
        boolean[] exists = new boolean[param.length];
        int num = 0;

        // Find which parameters still exist.
        for (int i = 0; i < param.length; i++) {
            for (TextureParameter textureParameter : texParam) {
                if (param[i].equals(textureParameter)) {
                    exists[i] = true;
                    num++;
                    break;
                }
            }
        }

        // Update this track's list of parameters.
        TextureParameter[] newparam = new TextureParameter[num];
        for (int i = 0, j = 0; i < exists.length; i++) {
            if (exists[i]) {
                newparam[j++] = param[i];
            }
        }
        param = newparam;

        // Update the value arrays for all keyframes.
        Keyframe[] key = tc.getValues();
        for (Keyframe keyframe : key) {
            double[] newval = new double[num];
            for (int i = 0, j = 0; i < exists.length; i++) {
                if (exists[i]) {
                    newval[j++] = ((ArrayKeyframe) keyframe).val[i];
                }
            }
            ((ArrayKeyframe) keyframe).val = newval;
        }
    }

    /* Write a serialized representation of this track to a stream. */
    @Override
    public void writeToStream(DataOutputStream out, Scene scene) throws IOException {
        TextureParameter[] texParam = info.getObject().getParameters();
        double[] t = tc.getTimes();
        Smoothness[] s = tc.getSmoothness();
        Keyframe[] v = tc.getValues();

        out.writeShort(0); // Version number
        out.writeUTF(name);
        out.writeBoolean(enabled);
        out.writeInt(smoothingMethod);
        out.writeShort(param.length);
        int[] index = new int[param.length];
        for (int i = 0; i < param.length; i++) {
            for (int j = 0; j < texParam.length; j++) {
                if (param[i].equals(texParam[j])) {
                    index[i] = j;
                }
            }
        }
        for (int j : index) {
            out.writeShort(j);
        }
        out.writeInt(t.length);
        for (int i = 0; i < t.length; i++) {
            out.writeDouble(t[i]);
            ((ArrayKeyframe) v[i]).writeToStream(out);
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
        int numParams = in.readShort();
        param = new TextureParameter[numParams];
        TextureParameter[] texParam = info.getObject().getParameters();
        for (int i = 0; i < param.length; i++) {
            param[i] = texParam[in.readShort()];
        }
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
    }

    /**
     * Present a window in which the user can edit the specified keyframe.
     */
    @Override
    public void editKeyframe(LayoutWindow win, int which) {
        ArrayKeyframe key = (ArrayKeyframe) tc.getValues()[which];
        Smoothness s = tc.getSmoothness()[which];
        double time = tc.getTimes()[which];
        ValueField timeField = new ValueField(time, ValueField.NONE, 5);
        ValueSlider s1Slider = new ValueSlider(0.0, 1.0, 100, s.getLeftSmoothness());
        final ValueSlider s2Slider = new ValueSlider(0.0, 1.0, 100, s.getRightSmoothness());
        final BCheckBox sameBox = new BCheckBox(Translate.text("separateSmoothness"), !s.isForceSame());
        Widget[] widget = new Widget[param.length + 5];
        String[] label = new String[param.length + 5];

        for (int i = 0; i < param.length; i++) {
            widget[i] = param[i].getEditingWidget(key.val[i]);
            label[i] = param[i].name;
        }
        sameBox.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent() {
                s2Slider.setEnabled(sameBox.getState());
            }
        });
        s2Slider.setEnabled(sameBox.getState());
        int n = param.length;
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
        for (int i = 0; i < param.length; i++) {
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
    private static void selectTrackableParameters(LayoutWindow layout, TextureTrack track) {
        selectTrackableParameters(layout, track, track.getInfo());
    }

    private static void selectTrackableParameters(LayoutWindow layout, TextureTrack track, ObjectInfo info) {
        BTextField nameField = new BTextField(track.getName());
        BComboBox smoothChoice = new BComboBox(new String[]{
                Translate.text("Discontinuous"),
                Translate.text("Linear"),
                Translate.text("Interpolating"),
                Translate.text("Approximating")
        });
        smoothChoice.setSelectedIndex(track.getSmoothingMethod());
        TreeList tree = new TreeList(layout);
        BScrollPane sp = new BScrollPane(tree);
        List<TreeElement> elements = new Vector<>();

        // Create a tree of all the texture parameters.
        TextureParameter[] texParam = info.getObject().getParameters();
        ParameterValue[] paramValue = info.getObject().getParameterValues();
        if (info.getObject().getTextureMapping() instanceof LayeredMapping) {
            LayeredMapping map = (LayeredMapping) info.getObject().getTextureMapping();
            Texture[] layer = map.getLayers();
            for (int i = 0; i < layer.length; i++) {
                List<TreeElement> v = new Vector<>();
                for (TextureParameter p : map.getLayerParameters(i)) {
                    int k;
                    for (k = 0; !texParam[k].equals(p); k++);
                    if (!(paramValue[k] instanceof ConstantParameterValue)) {
                        continue;
                    }
                    TreeElement el = new GenericTreeElement(tree, p.name, p.duplicate());
                    for (k = 0; k < track.param.length; k++) {
                        if (track.param[k].equals(p)) {
                            el.setSelected(true);
                        }
                    }
                    v.add(el);
                }
                if (v.isEmpty()) {
                    TreeElement el = new GenericTreeElement(tree, Translate.text("noAdjustableParams"), null);
                    el.setSelectable(false);
                    v.add(el);
                }
                TreeElement el = new GenericTreeElement(tree, Translate.text("layerLabel", Integer.toString(i + 1), layer[i].getName()), null, null, v);
                el.setSelectable(false);
                el.setExpanded(true);
                elements.add(el);
            }
        } else {
            for (int i = 0; i < texParam.length; i++) {
                if (paramValue[i] instanceof ConstantParameterValue) {
                    TreeElement el = new GenericTreeElement(tree, texParam[i].name, texParam[i]);
                    for (TextureParameter p : track.param) {
                        if (p.equals(texParam[i])) {
                            el.setSelected(true);
                        }
                    }
                    elements.add(el);
                }
            }
        }
        if (elements.isEmpty()) {
            TreeElement el = new GenericTreeElement(tree, Translate.text("noAdjustableParams"), null);
            el.setSelectable(false);
            elements.add(el);
        }
        TreeElement texElem = new GenericTreeElement(tree, Translate.text("Texture"), null, null, elements);
        texElem.setSelectable(false);
        texElem.setExpanded(true);
        tree.addElement(texElem);
        tree.setPreferredSize(new Dimension(150, 100));
        sp.setPreferredViewSize(new Dimension(150, 250));
        sp.setForceWidth(true);
        sp.setForceHeight(true);
        tree.setBackground(Color.white);
        ComponentsDialog dlg = new ComponentsDialog(layout, Translate.text("paramTrackTitle"), new Widget[]{nameField, smoothChoice, Translate.label("selectTrackParams"), sp}, new String[]{Translate.text("trackName"), Translate.text("SmoothingMethod"), null, null});
        if (!dlg.clickedOk()) {
            return;
        }

        // Update the list of parameters and other info.
        layout.setUndoRecord(new UndoRecord(layout, false, UndoRecord.COPY_OBJECT_INFO, info, info.duplicate()));
        track.setName(nameField.getText());
        track.setSmoothingMethod(smoothChoice.getSelectedIndex());
        Object[] selected = tree.getSelectedObjects();
        int[] index = new int[selected.length];
        for (int i = 0; i < selected.length; i++) {
            index[i] = -1;
            for (int j = 0; j < track.param.length; j++) {
                if (track.param[j].equals((TextureParameter) selected[i])) {
                    index[i] = j;
                }
            }
        }
        track.param = new TextureParameter[selected.length];
        System.arraycopy(selected, 0, track.param, 0, selected.length);
        Keyframe[] key = track.tc.getValues();
        for (Keyframe keyframe : key) {
            double[] newval = new double[track.param.length];
            for (int j = 0; j < newval.length; j++) {
                if (index[j] > -1) {
                    newval[j] = ((ArrayKeyframe) keyframe).val[index[j]];
                } else {
                    for (int k = 0; k < texParam.length; k++) {
                        if (texParam[k].equals(track.param[j])) {
                            newval[j] = paramValue[k].getAverageValue();
                        }
                    }
                }
            }
            ((ArrayKeyframe) keyframe).val = newval;
        }
    }
    /**
     * This method presents a window in which the user can edit the track.
     */
    @Override
    public void edit(LayoutWindow win) {
        SwingUtilities.invokeLater(() -> new TextureTrackParametersSelector(win.getComponent(), this).setVisible(true));
        TextureTrack.selectTrackableParameters(win, this);
    }
}
