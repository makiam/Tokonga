/* Copyright (C) 2018-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.procedural;

import artofillusion.Scene;
import artofillusion.procedural.Module;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import artofillusion.test.util.StreamUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author maksim.khramov
 */
@DisplayName("Procedure Read Stream Test")
class ProcedureReadStreamTest {

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Read Procedure Bad Version")
    void testReadProcedureBadVersion() {
        assertThrows(InvalidObjectException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Procedure Version 1. Expected exception to be thrown
            wrap.putShort((short) 1);
            new Procedure(new OutputModule[0]).readFromStream(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);
        });
    }

    @Test
    @DisplayName("Test Read Empty Procedure")
    void testReadEmptyProcedure() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Procedure Version 1. Expected exception to be thrown
        wrap.putShort((short) 0);
        // No Modules
        wrap.putInt(0);
        // No Links
        wrap.putInt(0);
        new Procedure(new OutputModule[0]).readFromStream(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);
    }

    @Test
    @DisplayName("Test Read Procedure With Bad Module Name")
    void testReadProcedureWithBadModuleName() {
        assertThrows(IOException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Procedure Version 1. Expected exception to be thrown
            wrap.putShort((short) 0);
            // One Module But bad Name
            wrap.putInt(1);
            String className = "module.module.BadModule";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            // Module's Point
            {
                wrap.putInt(123);
                wrap.putInt(456);
            }
            new Procedure(new OutputModule[0]).readFromStream(new DataInputStream(new ByteArrayInputStream(wrap.array())), (Scene) null);
        });
    }

    @Test
    @DisplayName("Test Read Procedure With Bad Module Constructor")
    void testReadProcedureWithBadModuleConstructor() {
        assertThrows(IOException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Procedure Version 1. Expected exception to be thrown
            wrap.putShort((short) 0);
            // One Module But bad Name
            wrap.putInt(1);
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
        });
    }

    @Test
    @DisplayName("Test Read Procedure With Single Module")
    void testReadProcedureWithSingleModule() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Procedure Version 1. Expected exception to be thrown
        wrap.putShort((short) 0);
        // One Module But bad Name
        wrap.putInt(1);
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

        Assertions.assertEquals(1, proc.getModules().length);
        var module = proc.getModules()[0];
        Assertions.assertEquals(module.getName(), "DummyModule");
    }

    @DisplayName("Dummy Module No Point Constructor")
    static class DummyModuleNoPointConstructor extends Module {

        public DummyModuleNoPointConstructor() {
            super("NPC", new IOPort[0], new IOPort[0], new Point(0, 0));
        }
    }

    @DisplayName("Dummy Module")
    static class DummyModule extends Module {

        public DummyModule(Point modulePoint) {
            super("DummyModule", new IOPort[0], new IOPort[0], modulePoint);
        }
    }
}
