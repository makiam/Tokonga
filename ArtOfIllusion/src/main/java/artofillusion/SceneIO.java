/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion;

import artofillusion.image.ImageMap;
import artofillusion.util.SearchlistClassLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SceneIO {

    private static final EventBus bus = org.greenrobot.eventbus.EventBus.getDefault();

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

    public static void readSceneMetadata(@NotNull DataInputStream in, Scene scene, Map<String, Object> metadata, short version) throws IOException {
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
}
