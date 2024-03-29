/* Copyright (C) 2018 by Maksim Khramov

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
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class ActorTest {

    @Test(expected = InvalidObjectException.class)
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testLoadActorBadVersion() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 1); // Actor Version 1. Expected exception to be thrown

        new Actor(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);

    }

    @Test
    public void testCreateActorForObject() {

        Object3D tube = new Tube(new Curve(new Vec3[]{new Vec3(), new Vec3()}, new float[]{0f, 1f}, Mesh.APPROXIMATING, false), new double[]{0f, 1f}, Tube.CLOSED_ENDS);
        Actor actor = new Actor(tube);

        Assert.assertNotNull(actor);
        Assert.assertEquals(1, actor.getNumGestures());

    }
}
