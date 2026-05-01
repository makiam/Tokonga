/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.Scene;
import artofillusion.SceneIO;
import artofillusion.api.ImplementationVersion;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;

import artofillusion.test.util.ReadBypassEventListener;
import artofillusion.test.util.TrackTestUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.*;


@Slf4j
public class TrackRestoreTest {

    private static ReadBypassEventListener listener;
    private static short sceneVersion = 6;

    @BeforeAll
    public static void setupClass() {
        listener = new ReadBypassEventListener();
    }

    @BeforeEach
    void resetCounterBefore() {
        listener.reset();
    }

    @Test
    void testWriteAndRestoreTrack() throws Exception {
        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack track = new PoseTrack(owner);
        track.setName("My Pose");
        track.setSmoothingMethod(Timecourse.APPROXIMATING);
        Assertions.assertEquals(0, TrackTestUtil.getLength(owner));

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);
        owner.addTrack(track);
        SceneIO.writeTracks(data, scene, owner, sceneVersion);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);

        owner.removeTrack(track);
        SceneIO.readTracks(new DataInputStream(in), scene, owner, sceneVersion);

        Assertions.assertEquals(1, TrackTestUtil.getLength(owner));
        PoseTrack restored = (PoseTrack)TrackTestUtil.getTracks(owner)[0];

        Assertions.assertEquals("My Pose", restored.getName());
        Assertions.assertEquals(Timecourse.APPROXIMATING, restored.getSmoothingMethod());

    }

    @Test
    void testWriteAndRestoreSceneItemTracksV6() throws Exception {
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

        SceneIO.writeTracks(data, scene, owner, version);
        owner.removeTrack(0);
        owner.removeTrack(0);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        SceneIO.readTracks(new DataInputStream(in), scene, owner, version);

        Assertions.assertEquals(2, TrackTestUtil.getLength(owner));
        PoseTrack restored = (PoseTrack)TrackTestUtil.getTracks(owner)[0];
        Assertions.assertEquals("My Pose", restored.getName());
        Assertions.assertEquals(Timecourse.APPROXIMATING, restored.getSmoothingMethod());
    }


    @Test
    void testWriteAndRestoreSceneItemTrack() throws Exception {
        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        PoseTrack track = new PoseTrack(owner);
        track.setName("My Pose");
        track.setSmoothingMethod(Timecourse.APPROXIMATING);

        owner.addTrack(track);

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);

        SceneIO.writeTracks(data, scene, owner, sceneVersion);
        owner.removeTrack(0);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        SceneIO.readTracks(new DataInputStream(in), scene, owner,sceneVersion);

        Assertions.assertEquals(1, TrackTestUtil.getLength(owner));
        PoseTrack restored = (PoseTrack)TrackTestUtil.getTracks(owner)[0];

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

        owner.addTrack(track);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);
        SceneIO.writeTracks(data, scene, owner, (short) 5);

        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        Assertions.assertThrows(IOException.class, () -> {
            SceneIO.readTracks(new DataInputStream(in), scene, owner, (short)5);
        });

    }

    @Test
    void testRestoreStringContainer() throws Exception {
        Scene scene = new Scene();
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo owner = new ObjectInfo(obj, new CoordinateSystem(), "Cube");

        Track track = new StringContainerTrack(owner);
        owner.addTrack(track);

        ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
        DataOutputStream data = new DataOutputStream(out);
        SceneIO.writeTracks(data, scene, owner, sceneVersion);

        owner.removeTrack(track);
        byte[] buffer = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        SceneIO.readTracks(new DataInputStream(in), scene, owner, sceneVersion);

        Assertions.assertEquals(1, TrackTestUtil.getLength(owner));
        StringContainerTrack restored = (StringContainerTrack)TrackTestUtil.getTrack(owner, 0);

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
