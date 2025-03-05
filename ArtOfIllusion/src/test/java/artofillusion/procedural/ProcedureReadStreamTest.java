/* Copyright (C) 2018-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import artofillusion.math.RGBColor;
import artofillusion.test.util.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @author maksim.khramov
 */
@Slf4j
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
            new Procedure().readFromStream(new DataInputStream(new ByteArrayInputStream(wrap.array())), null);
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
        new Procedure().readFromStream(new DataInputStream(new ByteArrayInputStream(wrap.array())), null);
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
            // Module 0 Point
            {
                wrap.putInt(123);
                wrap.putInt(456);
            }
            new Procedure().readFromStream(new DataInputStream(new ByteArrayInputStream(wrap.array())), null);
        });
    }

    @Test
    @Disabled
    @DisplayName("Test Read Procedure With Bad Module Constructor")
    void testReadProcedureWithBadModuleConstructor() {
        assertThrows(IOException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Procedure Version 1. Expected exception to be thrown
            wrap.putShort((short) 0);
            // One Module But bad Name
            wrap.putInt(1);
            ProcedureReadStreamTest.createModule(wrap, DummyModuleNoPointConstructor.class, 123, 456);

            Procedure proc = new Procedure();
            proc.readFromStream(StreamUtil.stream(wrap), null);
        });
    }

    @Test
    @DisplayName("Test Read Procedure With Single Module")
    void testReadProcedureWithSingleModule() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);

        wrap.putShort((short) 0);

        wrap.putInt(1);
        ProcedureReadStreamTest.createModule(wrap, DummyModule.class, 123, 456);

        Procedure proc = new Procedure();
        proc.readFromStream(StreamUtil.stream(wrap), null);

        Assertions.assertEquals(1, proc.getModules().size());
        var module = proc.getModules().get(0);
        Assertions.assertEquals("DummyModule", module.getName());
        Assertions.assertEquals(123, module.getBounds().x);
        Assertions.assertEquals(456, module.getBounds().y);
    }

    @Test
    @DisplayName("Test Read Procedure With Single Module 2")
    void testReadProcedureWithSingleModule2() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);

        wrap.putShort((short) 0);

        wrap.putInt(1);
        ProcedureReadStreamTest.createModule(wrap, DummyModule.class, 123, 456);

        Procedure proc = new Procedure(new OutputModule("Out", "Label", 0.0, new RGBColor(), IOPort.NUMBER));
        proc.readFromStream(StreamUtil.stream(wrap), null);

        Assertions.assertEquals(1, proc.getModules().size());
        var module = proc.getModules().get(0);
        Assertions.assertEquals("DummyModule", module.getName());
        Assertions.assertEquals(123, module.getBounds().x);
        Assertions.assertEquals(456, module.getBounds().y);
    }

    @Test
    @DisplayName("Test Read Procedure With Two Modules No Links")
    void testReadProcedureWitTwoModulesNoLinks() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);

        wrap.putShort((short) 0);

        wrap.putInt(2);
        ProcedureReadStreamTest.createModule(wrap, DummyModule.class, 123, 456);
        ProcedureReadStreamTest.createModule(wrap, DummyModule.class, 456, 123);

        Procedure proc = new Procedure(new OutputModule("Out", "Label", 0.0, new RGBColor(), IOPort.NUMBER));
        proc.readFromStream(StreamUtil.stream(wrap), null);

        Assertions.assertEquals(2, proc.getModules().size());
        var module = proc.getModules().get(0);
        Assertions.assertEquals("DummyModule", module.getName());
        Assertions.assertEquals(123, module.getBounds().x);
        Assertions.assertEquals(456, module.getBounds().y);

        module = proc.getModules().get(1);
        Assertions.assertEquals("DummyModule", module.getName());
        Assertions.assertEquals(456, module.getBounds().x);
        Assertions.assertEquals(123 , module.getBounds().y);
    }

    @Test
    @DisplayName("Test Read Procedure With Two Linked Modules")
    public void testTwoLinkedModules() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);

        wrap.putShort((short) 0);

        wrap.putInt(2);

        ProcedureReadStreamTest.createModule(wrap, DummyModule.class, 123, 456);
        ProcedureReadStreamTest.createModule(wrap, DummyModule.class, 456, 123);

        wrap.putInt(1); // Links count

        //tie second module second output to first module second input
        wrap.putInt(1);  //Module index
        wrap.putInt(1);  //Port index

        wrap.putInt(0);  //Module index
        wrap.putInt(1);  //Port index

        Procedure proc = new Procedure();
        proc.readFromStream(StreamUtil.stream(wrap), null);
        Assertions.assertEquals(2, proc.getModules().size());
        Assertions.assertEquals(1, proc.getLinks().length);

        var link = proc.getLinks()[0];
        var outPort = proc.getModules().get(1).getOutputPorts()[1];
        var inPort = proc.getModules().get(0).getInputPorts()[1];

        log.info("Link from: {} to {} ", link.from, link.to);
        log.info("Out port: {}", outPort);
        log.info("In port: {}", inPort);

        Assertions.assertEquals(link.from, outPort);
        Assertions.assertEquals(link.to, inPort);

    }
    @Test
    @DisplayName("Test Read Procedure With Linked Module and Output Module")
    public void testLinkModuleToOutput()  throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);

        wrap.putShort((short) 0);
        wrap.putInt(2); // Modules count
        ProcedureReadStreamTest.createModule(wrap, DummyModule.class, 123, 456);
        ProcedureReadStreamTest.createModule(wrap, DummyModule.class, 456, 123);

        wrap.putInt(1); // Links count

        //tie second module second output to output module second input
        wrap.putInt(1);  //Module index
        wrap.putInt(1);  //Port index

        wrap.putInt(-1);  //Output Module index; Negative value means that target module is a Procedure output module
        // As it output module it always has only onу input port. So we don't need to read it

        OutputModule pOut0 = new OutputModule("Output 0", "Label 0", 0.0, new RGBColor(), IOPort.NUMBER);
        OutputModule pOut1 = new OutputModule("Output 1", "Label 1", 0.0, new RGBColor(), IOPort.NUMBER);

        Procedure proc = new Procedure(pOut0, pOut1);

        proc.readFromStream(StreamUtil.stream(wrap), null);

        proc.readFromStream(StreamUtil.stream(wrap), null);
        Assertions.assertEquals(2, proc.getModules().size());
        Assertions.assertEquals(1, proc.getLinks().length);

        var link = proc.getLinks()[0];
        var outPort = proc.getModules().get(1).getOutputPorts()[1];
        var inPort = proc.getOutputModules()[0].getInputPorts()[0];

        log.info("Link from: {} to {} ", link.from, link.to);
        log.info("Out port: {}", outPort);
        log.info("In port: {}", inPort);

        Assertions.assertEquals(link.from, outPort);
        Assertions.assertEquals(link.to, inPort);
    }

    private static void createModule(ByteBuffer wrap, Class<? extends Module> mc, int X, int Y) {
        String className = mc.getTypeName();
        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        {
            wrap.putInt(X);
            wrap.putInt(Y);
        }
    }

    @DisplayName("Dummy Module No Point Constructor")
    static class DummyModuleNoPointConstructor extends Module<DummyModuleNoPointConstructor> {

        public DummyModuleNoPointConstructor() {
            super("NPC", new IOPort[0], new IOPort[0], new Point(0, 0));
        }
    }

    @DisplayName("Dummy Module")
    static class DummyModule extends Module<DummyModule> {
        public DummyModule() {
            this(new Point());
        }
        public DummyModule(Point modulePoint) {
            super("DummyModule", new IOPort[] {
                            new NumericInputPort(IOPort.LEFT, "Input0", "(0)"),
                            new NumericInputPort(IOPort.LEFT, "Input1", "(0)")
                    },
                    new IOPort[] {
                            new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Output0"),
                            new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, "Output1")
                    }, modulePoint);
        }
    }
}
