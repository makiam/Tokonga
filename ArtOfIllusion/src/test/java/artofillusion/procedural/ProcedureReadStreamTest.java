/* Copyright (C) 2018-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.Scene;
import artofillusion.test.util.StreamUtil;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

/**
 *
 * @author maksim.khramov
 */
public class ProcedureReadStreamTest {

    @Test(expected = InvalidObjectException.class)
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testReadProcedureBadVersion() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 1); // Procedure Version 1. Expected exception to be thrown

        new Procedure().readFromStream(StreamUtil.stream(wrap), (Scene) null);

    }

    @Test
    public void testReadEmptyProcedure() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 0); // Procedure Version 0. Good version here
        wrap.putInt(0); // No Modules
        wrap.putInt(0); // No Links
        new Procedure().readFromStream(StreamUtil.stream(wrap), (Scene) null);

    }

    @Test(expected = IOException.class)
    public void testReadProcedureWithBadModuleName() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 0); // Procedure Version 0. Good version here
        wrap.putInt(1); // One Module But bad Name

        String className = "module.module.BadModule";
        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        // Module's Point
        {
            wrap.putInt(123);
            wrap.putInt(456);
        }
        new Procedure().readFromStream(StreamUtil.stream(wrap), (Scene) null);
    }

    @Test(expected = IOException.class)
    public void testReadProcedureWithBadModuleConstructor() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 0); // Procedure Version 0. Good version here
        wrap.putInt(1); // One Module But bad Name

        String className = DummyModuleNoPointConstructor.class.getTypeName();

        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        // Module's Point
        {
            wrap.putInt(123);
            wrap.putInt(456);
        }
        Procedure proc = new Procedure();
        proc.readFromStream(StreamUtil.stream(wrap), (Scene) null);
    }

    @Test
    public void testReadProcedureWithSingleModule() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 0); // Procedure Version 1. Expected exception to be thrown
        wrap.putInt(1); // One Module But bad Name

        String className = DummyModule.class.getTypeName();

        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        // Module's Point
        {
            wrap.putInt(123);
            wrap.putInt(456);
        }
        Procedure proc = new Procedure();
        proc.readFromStream(StreamUtil.stream(wrap), (Scene) null);

        Assert.assertEquals(1, proc.getModules().length);
        var module = proc.getModules()[0];
        Assert.assertEquals("DummyModule", module.getName());

    }

    public static class DummyModuleNoPointConstructor extends ProceduralModule {

        public DummyModuleNoPointConstructor() {
            super("NPC", new IOPort[0], new IOPort[0], new Point(0, 0));
        }
    }

    public static class DummyModule extends ProceduralModule {

        public DummyModule(Point modulePoint) {
            super("DummyModule", new IOPort[0], new IOPort[0], modulePoint);
        }

    }
}
