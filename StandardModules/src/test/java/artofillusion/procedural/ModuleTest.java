/* Copyright (C) 2020 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Point;


/**
 *
 * @author MaksK
 */
public class ModuleTest {

    @Test
    public void testModuleDuplicate() {
        var source = new ColorScaleModule(new Point(128, 64));
        var target = source.duplicate();

        Assertions.assertTrue(target instanceof ColorScaleModule);
        Assertions.assertNotNull(target);
        Assertions.assertEquals(128, target.getBounds().x);
        Assertions.assertEquals(64, target.getBounds().y);

    }

    @Test
    public void testLookupNoInputs() {
        var port = new NumericInputPort(IOPort.LEFT, "Description");
        var mm = new TestInputsPortModule();
        Assertions.assertNotNull(mm);
        Assertions.assertEquals(0, mm.getInputPorts().length);
        Assertions.assertEquals(-1, mm.getInputIndex(port));
    }

    @Test
    public void testLookupNoOutputs() {
        var port = new NumericInputPort(IOPort.LEFT, "Description");
        var mm = new TestOutputsPortModule();
        Assertions.assertNotNull(mm);
        Assertions.assertEquals(0, mm.getOutputPorts().length);
        Assertions.assertEquals(-1, mm.getOutputIndex(port));
    }

    @Test
    public void testInputPortLookup1() {
        var port = new NumericInputPort(IOPort.LEFT, "Description");
        var mm = new TestInputsPortModule(port);
        Assertions.assertNotNull(mm);
        Assertions.assertEquals(0, mm.getInputIndex(port));
    }

    @Test
    public void testInputPortLookup2() {
        var port1 = new NumericInputPort(IOPort.LEFT, "Description 1");
        var port2 = new NumericInputPort(IOPort.LEFT, "Description 2");
        var mm = new TestInputsPortModule(port1, port2);
        Assertions.assertNotNull(mm);
        Assertions.assertEquals(2, mm.getInputPorts().length);
        Assertions.assertEquals(1, mm.getInputIndex(port2));
    }


    public void testOutputPortLookup1() {
        var port = new NumericInputPort(IOPort.LEFT, "Description");
        var mm = new TestOutputsPortModule(port);
        Assertions.assertNotNull(mm);
        Assertions.assertEquals(0, mm.getOutputIndex(port));
    }

    @Test
    public void testOutputPortLookup2() {
        var port1 = new NumericInputPort(IOPort.LEFT, "Description 1");
        var port2 = new NumericInputPort(IOPort.LEFT, "Description 2");
        var mm = new TestOutputsPortModule(port1, port2);
        Assertions.assertNotNull(mm);
        Assertions.assertEquals(2, mm.getOutputPorts().length);
        Assertions.assertEquals(1, mm.getOutputIndex(port2));
    }


    class TestInputsPortModule extends ProceduralModule<TestInputsPortModule> {
        public TestInputsPortModule(IOPort... ports) {
            super("ManyInputsPortModule", ports, new IOPort[]{},  new Point());
        }
    }

    class TestOutputsPortModule extends ProceduralModule<TestOutputsPortModule> {
        public TestOutputsPortModule(IOPort... ports) {
            super("ManyInputsPortModule", new IOPort[]{}, ports,  new Point());
        }
    }
}
