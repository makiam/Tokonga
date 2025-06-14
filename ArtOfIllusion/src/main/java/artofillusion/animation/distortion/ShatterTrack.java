/* Copyright (C) 2002-2004 by Peter Eastman
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
 * This is a Track which applies a ShatterDistortion to an object.
 */
public class ShatterTrack extends Track<ShatterTrack> {

    ObjectInfo info;
    double startTime;
    double size;
    double speed;
    double randomness;
    double gravity;
    double spin;
    double disappear;
    int gravityAxis;
    boolean worldCoords;

    public ShatterTrack(ObjectInfo info) {
        super("Shatter");
        this.info = info;
        gravityAxis = ShatterDistortion.Y_AXIS;
        startTime = 0.0;
        size = 0.2;
        speed = 1.0;
        randomness = 0.2;
        gravity = 1.0;
        spin = 5.0;
        disappear = 0.0;
        worldCoords = true;
    }

    /* Modify the scale of the object. */
    @Override
    public void apply(double time) {
        if (time <= startTime) {
            return;
        }
        if (worldCoords) {
            info.addDistortion(new ShatterDistortion(time - startTime, size, speed, randomness, gravity, spin, disappear, gravityAxis, info.getCoords().toLocal()));
        } else {
            info.addDistortion(new ShatterDistortion(time - startTime, size, speed, randomness, gravity, spin, disappear, gravityAxis, null));
        }
    }

    /* Create a duplicate of this track. */
    @Override
    public ShatterTrack duplicate(Object obj) {
        ShatterTrack t = new ShatterTrack((ObjectInfo) obj);

        t.name = name;
        t.enabled = enabled;

        t.startTime = startTime;
        t.size = size;
        t.speed = speed;
        t.randomness = randomness;
        t.gravity = gravity;
        t.spin = spin;
        t.disappear = disappear;
        t.gravityAxis = gravityAxis;
        t.worldCoords = worldCoords;
        return t;
    }

    /* Make this track identical to another one. */
    @Override
    public void copy(ShatterTrack track) {

        name = track.name;
        enabled = track.enabled;

        startTime = track.startTime;
        size = track.size;
        speed = track.speed;
        randomness = track.randomness;
        gravity = track.gravity;
        spin = track.spin;
        disappear = track.disappear;
        gravityAxis = track.gravityAxis;
        worldCoords = track.worldCoords;
    }

    /* Get a list of all keyframe times for this track. */
    @Override
    public double[] getKeyTimes() {
        return new double[0];
    }

    /* Move a keyframe to a new time, and return its new position in the list. */
    @Override
    public int moveKeyframe(int which, double time) {
        return -1;
    }

    /* Delete the specified keyframe. */
    @Override
    public void deleteKeyframe(int which) {
    }

    /* Shatter tracks are never null. */
    @Override
    public boolean isNullTrack() {
        return false;
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

    /* Write a serialized representation of this track to a stream. */
    @Override
    public void writeToStream(DataOutputStream out, Scene scene) throws IOException {
        out.writeShort(0); // Version number
        out.writeUTF(name);
        out.writeBoolean(enabled);

        out.writeDouble(startTime);
        out.writeDouble(size);
        out.writeDouble(speed);
        out.writeDouble(randomness);
        out.writeDouble(gravity);
        out.writeDouble(spin);
        out.writeDouble(disappear);
        out.writeInt(gravityAxis);
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
        startTime = in.readDouble();
        size = in.readDouble();
        speed = in.readDouble();
        randomness = in.readDouble();
        gravity = in.readDouble();
        spin = in.readDouble();
        disappear = in.readDouble();
        gravityAxis = in.readInt();
    }

    /**
     * This method presents a window in which the user can edit the track.
     */
    @Override
    public void edit(LayoutWindow win) {
        BTextField nameField = new BTextField(ShatterTrack.this.getName());
        BComboBox axisChoice = new BComboBox(new String[]{"X", "Y", "Z"});
        axisChoice.setSelectedIndex(gravityAxis);
        BComboBox coordsChoice = new BComboBox(new String[]{
            Translate.text("Local"),
            Translate.text("World")
        });
        coordsChoice.setSelectedIndex(worldCoords ? 1 : 0);
        ValueField timeField = new ValueField(startTime, ValueField.NONE, 5);
        ValueField sizeField = new ValueField(size, ValueField.NONE, 5);
        ValueField speedField = new ValueField(speed, ValueField.NONE, 5);
        ValueSlider randomSlider = new ValueSlider(0.0, 1.0, 100, randomness);
        ValueField gravityField = new ValueField(gravity, ValueField.NONE, 5);
        ValueField spinField = new ValueField(spin, ValueField.NONNEGATIVE, 5);
        ValueField disappearField = new ValueField(disappear, ValueField.NONNEGATIVE, 5);
        ComponentsDialog dlg = new ComponentsDialog(win, Translate.text("shatterTrackTitle"), new Widget[]{nameField, timeField, sizeField, speedField, spinField, disappearField, gravityField, axisChoice, randomSlider, coordsChoice}, new String[]{Translate.text("trackName"), Translate.text("StartTime"), Translate.text("maxFragmentSize"), Translate.text("explodeSpeed"), Translate.text("fragmentSpinRate"),
            Translate.text("disappearanceTime"), Translate.text("gravity"), Translate.text("gravityAxis"), Translate.text("randomness"), Translate.text("CoordinateSystem")});
        if (!dlg.clickedOk()) {
            return;
        }
        win.setUndoRecord(new UndoRecord(win, false, UndoRecord.COPY_OBJECT_INFO, info, info.duplicate()));
        this.setName(nameField.getText());
        startTime = timeField.getValue();
        size = sizeField.getValue();
        speed = speedField.getValue();
        randomness = randomSlider.getValue();
        spin = spinField.getValue();
        disappear = disappearField.getValue();
        gravity = gravityField.getValue();
        gravityAxis = axisChoice.getSelectedIndex();
        worldCoords = (coordsChoice.getSelectedIndex() == 1);
    }
}
