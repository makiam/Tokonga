/* Copyright (C) 2020 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;


/**
 *
 * @author MaksK
 */
public class ModuleTest {

    @Test
    public void testModuleDuplicate0() {
        var source = new ColorScaleModule(new Point(128, 64));
        var target = source.duplicate();

        Assertions.assertTrue(target instanceof ColorScaleModule);
        Assertions.assertNotNull(target);
        Assertions.assertEquals(128, target.getBounds().x);
        Assertions.assertEquals(64, target.getBounds().y);

    }

    @Test
    public void testModuleDuplicate1() {
        var source = new PowerModule(new Point(128, 64));
        var target = source.duplicate();

        Assertions.assertTrue(target instanceof PowerModule);
        Assertions.assertNotNull(target);
        Assertions.assertEquals(128, target.getBounds().x);
        Assertions.assertEquals(64, target.getBounds().y);

    }

    @Test
    public void testModuleDuplicate2() {
        var source = new ParameterModule(new Point(128, 64));
        var target = source.duplicate();

        Assertions.assertTrue(target instanceof ParameterModule);
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

    @ParameterizedTest
    @MethodSource("getModuleClasses")
    public void testDefaultModuleConstructor(Class clazz) throws ReflectiveOperationException {
        var mod = (Module)clazz.getConstructor().newInstance();
        mod.setPosition(123, 456);
        Assertions.assertEquals(123, mod.bounds.x);
        Assertions.assertEquals(456, mod.bounds.y);
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

    static Stream<Class<? extends ProceduralModule<? extends ProceduralModule<?>>>> getModuleClasses() {
        return List.of(ColorEqualityModule.class, CompareModule.class, PowerModule.class, RGBModule.class).stream();
    }
}
