/* Copyright 2025-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.animation.Track;
import artofillusion.image.ImageMap;
import artofillusion.object.ObjectInfo;
import artofillusion.util.SearchlistClassLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.beans.XMLDecoder;
import java.io.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SceneIO {

    private static EventBus bus = EventBus.getDefault();

    public static void readImages(@NotNull DataInputStream in, Scene scene, short version) throws IOException {
        int images = in.readInt();
        log.debug("Scene version: {}. Reading {} images: ", version, images);

        try {
            for (int i = 0; i < images; i++) {
                String className = SceneIO.readString(in);
                var cls = Optional.ofNullable(ArtOfIllusion.getClass(className)).orElseThrow(() -> new IOException("Unknown ImageMap class: " + className));
                scene.add((ImageMap) cls.getConstructor(DataInputStream.class).newInstance(in));
            }
        } catch(ReflectiveOperationException ex) {
            throw new IOException("Error loading image: " + ex.getMessage(), ex);
        }
        log.debug("Read images completed");
    }

    public static void writeImages(@NotNull DataOutputStream out, Scene scene, short version) throws IOException {
        List<ImageMap> items = scene.getImages();
        log.debug("Scene version: {}. Writing images: {}", version, items.size());
        out.writeInt(items.size());
        for (var item: items) {
            writeClass(out, item);
            item.writeToStream(out, scene);
        }
        log.debug("Write images completed");
    }

    public static void writeMaterials(@NotNull DataOutputStream out, Scene scene, short version) throws IOException {
        var items = scene.getMaterials();
        log.debug("Scene version: {}. Writing materials: {}", version, items.size());
        out.writeInt(items.size());
        for (var item: items) {
            SceneIO.writeClass(out, item);
            SceneIO.writeBuffered(out, target -> item.writeToFile(target, scene));
        }
        log.debug("Write materials completed");
    }

    public static void writeTracks(@NotNull DataOutputStream out, Scene scene, ObjectInfo owner, short version) throws IOException {
        var items = owner.getTracks();
        log.debug("Write {} tracks: {}. Version {}", owner.name, items.length, version);
        out.writeInt(items.length);
        for (var item: items) {
            SceneIO.writeClass(out, item);
            SceneIO.writeBuffered(out, target -> item.writeToStream(target, scene));
        }
        log.debug("Write tracks completed");
    }


    public static void writeTextures(@NotNull DataOutputStream out, Scene scene, short version) throws IOException {
        var items = scene.getTextures();
        log.debug("Scene version: {}. Writing textures: {}", version, items.size());
        out.writeInt(items.size());
        for (var item: items) {
            SceneIO.writeClass(out, item);
            SceneIO.writeBuffered(out, target -> item.writeToFile(target, scene));
        }
        log.debug("Write textures completed");
    }

    // Scene metadata supported since Scene version 4 introduced in 2008
    // passing scene version to this method for possible future changes
    public static void readSceneMetadata(@NotNull DataInputStream in, Scene scene, Map<String, Object> metadata, short version) throws IOException {
        if(version < 4) {
            log.debug("Scene version: {}. Reading metadata not supported", version);
            return;
        }
        var count = in.readInt();
        log.debug("Scene version: {}. Reading metadata: {}", version, count);

        SearchlistClassLoader loader = new SearchlistClassLoader(scene.getClass().getClassLoader());
        PluginRegistry.getPluginClassLoaders().forEach(loader::add);
        for (int i = 0; i < count; i++) {
            try {
                String name = SceneIO.readString(in);
                byte[] data = new byte[in.readInt()];
                in.readFully(data);
                XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(data), null, null, loader);
                metadata.put(name, decoder.readObject());
            } catch (IOException ex) {
                log.atError().setCause(ex).log("Metadata reading error: {}", ex.getMessage());
                // Nothing more we can do about it.
            }
        }
        log.debug("Read metadata completed");
    }

    public static String readString(DataInputStream in) throws IOException {
        return in.readUTF();
    }

    public static void writeClass(DataOutputStream out, Object item) throws IOException {
        out.writeUTF(item.getClass().getName());
    }

    public static void writeBuffered(DataOutputStream out, DataWriteProvider writer) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer.write(new DataOutputStream(bos));
        byte[] bytes = bos.toByteArray();
        out.writeInt(bytes.length);
        out.write(bytes, 0, bytes.length);
    }

    public static void readTracks(DataInputStream in, Scene scene, ObjectInfo owner, int version) throws IOException {
        var tracks = in.readInt();
        log.debug("Scene version {}. Reading {} tracks for {}.", version, tracks, owner.getName());
        switch (version) {
            case 6 -> SceneIO.readTracksV6(in, scene, owner, tracks);
            default -> SceneIO.readTracksV5(in, scene, owner, tracks);
        }
        log.debug("Read tracks for {} completed", owner.getName());

    }
    private static void readTracksV6(DataInputStream in, Scene scene, ObjectInfo owner, int tracks) throws IOException {
        String className;
        int dataSize;
        byte[] data;
        Track<?> track;
        for (int i = 0; i < tracks; i++) {
            // At first read binary data from input. If IOException is thrown we cannot recover data and aborting
            try {
                className = SceneIO.readString(in);
                dataSize = in.readInt();
                data = new byte[dataSize];
                in.readFully(data);

            } catch (IOException ex) {
                throw ex;
            }
            //Now try to discover Track class. On exception, we cannot recover track, but can bypass it
            try {
                var cls = ArtOfIllusion.getClass(className);
                if(null == cls) {
                    bus.post(new BypassEvent(scene, "Track class not found: " + className));
                    continue;
                }
                var tc = cls.getConstructor(ObjectInfo.class);
                track = (Track<?>) tc.newInstance(owner);

            } catch(ReflectiveOperationException ex) {
                bus.post(new BypassEvent(scene, "Track class not found: " + className, ex));
                continue;
            }
            //On exception, we cannot recover track, but can bypass it
            try {
                track.initFromStream(new DataInputStream(new ByteArrayInputStream(data)), scene);
            } catch(IOException ex) {
                bus.post(new BypassEvent(scene, "Track initialization error: " + ex.getMessage(),ex));
                continue;
            }
            owner.addTrack(track);
        }
    }

    private static void readTracksV5(DataInputStream in, Scene scene, ObjectInfo owner, int tracks) throws IOException {
        for (int i = 0; i < tracks; i++) {
            var className = SceneIO.readString(in);
            try {
                var cls = Optional.ofNullable(ArtOfIllusion.getClass(className)).orElseThrow(() -> new IOException("Unknown Track class: " + className));
                Track<?> track = (Track) cls.getConstructor(ObjectInfo.class).newInstance(owner);
                track.initFromStream(in, scene);
                owner.addTrack(track);
            } catch(IOException | ReflectiveOperationException ex) {
                log.atError().setCause(ex).log("Track Reading error: {}", ex.getMessage());
                throw new IOException();
                // Nothing more we can do about it.
            }
        }
    }

    @FunctionalInterface
    public interface DataWriteProvider {
        void write(DataOutputStream out) throws IOException;
    }
}
