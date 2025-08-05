/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion;

import artofillusion.image.ImageMap;
import artofillusion.material.Material;
import artofillusion.texture.Texture;
import artofillusion.util.SearchlistClassLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;

import java.beans.XMLDecoder;
import java.io.*;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SceneIO {

    /*

     */
    public static void readImages(@NotNull DataInputStream in, Scene scene, short version) throws IOException {
        int images = in.readInt();
        log.debug("Scene version: {}. Reading {} images: ", version, images);

        try {
            for (int i = 0; i < images; i++) {
                String className = SceneIO.readString(in);
                Class<?> cls = ArtOfIllusion.getClass(className);
                if (cls == null) {
                    throw new IOException("Unknown ImageMap class: " + className);
                }
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
        List<Material> items = scene.getMaterials();
        log.debug("Scene version: {}. Writing materials: {}", version, items.size());
        out.writeInt(items.size());
        for (var item: items) {
            SceneIO.writeClass(out, item);
            SceneIO.writeBuffered(out, target -> item.writeToFile(target, scene));
        }
        log.debug("Write materials completed");
    }

    public static void writeTextures(@NotNull DataOutputStream out, Scene scene, short version) throws IOException {
        List<Texture> items = scene.getTextures();
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

    private static String readString(DataInputStream in) throws IOException {
        var ts = in.readUTF();
        return ts;
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        out.writeUTF(value);
    }

    public static void writeClass(DataOutputStream out, Object item) throws IOException {
        out.writeUTF(item.getClass().getName());
    }

    public static void writeBuffered(DataOutputStream out, DataWriteProvider writer)throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writer.write(new DataOutputStream(bos));
        byte[] bytes = bos.toByteArray();
        out.writeInt(bytes.length);
        out.write(bytes, 0, bytes.length);
    }

    @FunctionalInterface
    public interface DataWriteProvider {
        void write(DataOutputStream out) throws IOException;
    }
}
