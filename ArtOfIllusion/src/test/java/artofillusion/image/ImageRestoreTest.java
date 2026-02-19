/* Copyright 2025-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.image;

import artofillusion.Scene;
import artofillusion.test.util.ReadBypassEventListener;
import artofillusion.test.util.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class ImageRestoreTest {

    private static ReadBypassEventListener listener;

    @BeforeAll
    static void setupClass() {
        listener = new ReadBypassEventListener();
    }

    @BeforeEach
    void resetCounterBefore() {
        listener.reset();
    }

    @Test
    void testRestoreImage() throws IOException {

        var scene = new Scene();
        ImageMap map = new DummyImageGood();
        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream(target -> {
            map.writeToStream(target, scene);
        });
        var bb = StreamUtil.getUTFNameAsByteArray(map.getClass());

        wrap.putInt(1); //Images counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);


        artofillusion.SceneIO.readImages(StreamUtil.stream(wrap), scene, (short) 0);
        Assertions.assertEquals(1, scene.getImages().size());

    }

    @Test
    void testRestoreMoreImages() throws IOException {

        var scene = new Scene();
        ImageMap map = new DummyImageGood();
        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream(target -> {
            map.writeToStream(target, scene);
        });
        var bb = StreamUtil.getUTFNameAsByteArray(map.getClass());

        wrap.putInt(2); //Images counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);

        artofillusion.SceneIO.readImages(StreamUtil.stream(wrap), scene, (short) 0);
        Assertions.assertEquals(2, scene.getImages().size());

    }

    @Test
    void testRestoreImageBadClassName() throws IOException {

        var scene = new Scene();
        ImageMap map = new DummyImageGood();
        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream((target) -> {
            map.writeToStream(target, scene);
        });
        var bb = StreamUtil.getUTFNameAsByteArray(map.getClass() + "Bad");

        wrap.putInt(1); //Images counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);


        Assertions.assertThrows(IOException.class, () -> {
            artofillusion.SceneIO.readImages(StreamUtil.stream(wrap), scene, (short) 0);
        });

    }

    @Test
    void testRestoreImageMissedExpectedTrackConstructor() throws IOException {

        var scene = new Scene();
        ImageMap map = new DummyImageBad();
        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream((target) -> {
            map.writeToStream(target, scene);
        });
        var bb = StreamUtil.getUTFNameAsByteArray(map.getClass());

        wrap.putInt(1); //Images counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);

        Assertions.assertThrows(IOException.class, () -> {
            artofillusion.SceneIO.readImages(StreamUtil.stream(wrap), scene, (short) 0);
        });

    }

}
