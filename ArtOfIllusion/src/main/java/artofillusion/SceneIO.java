/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion;

import artofillusion.image.ImageMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.tools.shell.IO;
import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
final class SceneIO {

    private static EventBus bus = org.greenrobot.eventbus.EventBus.getDefault();

    public static void readImages(DataInputStream in, Scene scene) throws IOException {
        int images = in.readInt();
        log.debug("Read images: {}", images);

        Constructor<?> con;
        try {
            for (int i = 0; i < images; i++) {
                String className = in.readUTF();
                Class<?> cls = ArtOfIllusion.getClass(className);
                if (cls == null) {
                    throw new IOException("Unknown ImageMap class: " + className);
                }
                scene.add((ImageMap) cls.getConstructor(DataInputStream.class).newInstance(in));
            }
        } catch(ReflectiveOperationException ex) {
            throw new IOException("Error loading image: " + ex.getMessage(), ex);
        }

    }
}
