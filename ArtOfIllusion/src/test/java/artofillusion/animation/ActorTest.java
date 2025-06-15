/* Copyright (C) 2018-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.animation;

import artofillusion.Scene;
import artofillusion.math.Vec3;
import artofillusion.object.Curve;
import artofillusion.object.Mesh;
import artofillusion.object.Object3D;
import artofillusion.object.Tube;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @author maksim.khramov
 */
@DisplayName("Actor Test")
class ActorTest {

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Load Actor Bad Version")
    void testLoadActorBadVersion() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Actor Version 1. Expected exception to be thrown
            wrap.putShort((short) 1);
            new Actor(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);
        });
    }

    @Test
    @DisplayName("Test Create Actor For Object")
    void testCreateActorForObject() {
        Object3D tube = new Tube(new Curve(new Vec3[]{new Vec3(), new Vec3()}, new float[]{0f, 1f}, Mesh.APPROXIMATING, false), new double[]{0f, 1f}, Tube.CLOSED_ENDS);
        Actor actor = new Actor(tube);
        Assertions.assertNotNull(actor);
        Assertions.assertEquals(1, actor.getNumGestures());
    }
}
