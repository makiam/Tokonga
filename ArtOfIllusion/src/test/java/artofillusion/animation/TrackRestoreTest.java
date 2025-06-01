/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.Scene;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;


@Slf4j
public class TrackRestoreTest {

    @Test
    void testWriteAndRestoreTrack() throws IOException {
        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack track = new PoseTrack(owner);
        track.setName("My Pose");
        track.setSmoothingMethod(Timecourse.APPROXIMATING);
        Assertions.assertEquals(0, owner.getTracks().length);

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);
        data.writeInt(1);
        TrackIO.INSTANCE.writeTrack(data, scene, track, (short)5);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        TrackIO.INSTANCE.readTracks(new DataInputStream(in), scene, owner, (short)5);

        Assertions.assertEquals(1, owner.getTracks().length);
        PoseTrack restored = (PoseTrack)owner.getTracks()[0];

        Assertions.assertEquals("My Pose", restored.getName());
        Assertions.assertEquals(Timecourse.APPROXIMATING, restored.getSmoothingMethod());

    }

    @Test
    void testWriteAndRestoreSceneItemTracksV6() throws IOException {
        short version = 6;

        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack track = new PoseTrack(owner);
        track.setName("My Pose");
        track.setSmoothingMethod(Timecourse.APPROXIMATING);

        owner.addTrack(track);
        owner.addTrack(track);

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);

        TrackIO.INSTANCE.writeTracks(data, scene, owner, version);
        owner.removeTrack(0);
        owner.removeTrack(0);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        TrackIO.INSTANCE.readTracks(new DataInputStream(in), scene, owner, version);

        Assertions.assertEquals(2, owner.getTracks().length);
        PoseTrack restored = (PoseTrack)owner.getTracks()[0];
        Assertions.assertEquals("My Pose", restored.getName());
        Assertions.assertEquals(Timecourse.APPROXIMATING, restored.getSmoothingMethod());
    }


    @Test
    void testWriteAndRestoreSceneItemTrack() throws IOException {
        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack track = new PoseTrack(owner);
        track.setName("My Pose");
        track.setSmoothingMethod(Timecourse.APPROXIMATING);

        owner.addTrack(track);

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);

        TrackIO.INSTANCE.writeTracks(data, scene, owner, (short)5);
        owner.removeTrack(0);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        TrackIO.INSTANCE.readTracks(new DataInputStream(in), scene, owner, (short)5);

        Assertions.assertEquals(1, owner.getTracks().length);
        PoseTrack restored = (PoseTrack)owner.getTracks()[0];

        Assertions.assertEquals("My Pose", restored.getName());
        Assertions.assertEquals(Timecourse.APPROXIMATING, restored.getSmoothingMethod());
    }

    @Test
    @DisplayName("Error to Restore track as no appropriate constructor")
    void testTryToRestoreBadTrackOldVersion() throws IOException {
        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Likörflasche PM");
        PoseTrack track = new LocalPoseTrack(owner);
        track.setName("My Pose");
        track.setSmoothingMethod(Timecourse.APPROXIMATING);

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);
        data.writeInt(1);
        TrackIO.INSTANCE.writeTrack(data, scene, track, (short)5);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        Assertions.assertThrows(IOException.class, () -> {
            TrackIO.INSTANCE.readTracks(new DataInputStream(in), scene, owner, (short)5);
        });

    }

    @Test
    void testRestoreStringContainer() throws IOException {
        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Cube");

        Track track = new StringContainerTrack(owner);

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);
        data.writeInt(1);
        TrackIO.INSTANCE.writeTrack(data, scene, track, (short)5);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        TrackIO.INSTANCE.readTracks(new DataInputStream(in), scene, owner, (short)5);

        Assertions.assertEquals(1, owner.getTracks().length);
        StringContainerTrack restored = (StringContainerTrack)owner.getTracks()[0];

        Assertions.assertEquals("А роза упала на лапу азора", restored.getPalindrome());
    }

    static class StringContainerTrack extends DummyTrack {

        @Getter
        private String palindrome = "";

        public StringContainerTrack(ObjectInfo info) {

        }

        @Override
        public void writeToStream(DataOutputStream out, Scene scene) throws IOException {
            out.writeUTF("А роза упала на лапу азора");
        }

        @Override
        public void initFromStream(DataInputStream in, Scene scene) throws IOException {
            palindrome = in.readUTF();
        }
    }

    private class LocalPoseTrack extends PoseTrack {

        public LocalPoseTrack(ObjectInfo info) {
            super(info);
        }
    }
}
