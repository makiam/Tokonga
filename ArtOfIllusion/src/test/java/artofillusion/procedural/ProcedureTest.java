/* Copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.procedural;

import artofillusion.math.RGBColor;
import artofillusion.math.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author MaksK
 */
@DisplayName("Procedure Test")
class ProcedureTest {

    @Test
    @DisplayName("Test Create New Procedure")
    void testCreateNewProcedure() {
        Procedure procedure = new Procedure();
        Assertions.assertNotNull(procedure);
        Assertions.assertNotNull(procedure.getOutputModules());
        Assertions.assertEquals(0, procedure.getOutputModules().length);
        Assertions.assertNotNull(procedure.getModules());
        Assertions.assertEquals(0, procedure.getModules().size());
        Assertions.assertNotNull(procedure.getLinks());
        Assertions.assertEquals(0, procedure.getLinks().length);
    }

    @Test
    @DisplayName("Test Get Null Output Module Index From Procedure")
    void testGetNullOutputModuleIndexFromProcedure() {
        Procedure procedure = new Procedure();
        Assertions.assertEquals(-1, procedure.getOutputIndex(null));
    }

    @Test
    @DisplayName("Test Get Missed Output Module Index From Procedure")
    void testGetMissedOutputModuleIndexFromProcedure() {
        Procedure procedure = new Procedure();
        OutputModule missed = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Assertions.assertEquals(-1, procedure.getOutputIndex(missed));
    }

    @Test
    @DisplayName("Test Get Single Output Module Index From Procedure")
    void testGetSingleOutputModuleIndexFromProcedure() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure procedure = new Procedure(new OutputModule[]{exist});
        Assertions.assertEquals(0, procedure.getOutputIndex(exist));
    }

    @Test
    @DisplayName("Test Get Second Output Module Index From Procedure")
    void testGetSecondOutputModuleIndexFromProcedure() {
        OutputModule first = new OutputModule("TestOut1", "Label0", 0, new RGBColor(1, 1, 1), 0);
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure procedure = new Procedure(first, exist);
        Assertions.assertEquals(1, procedure.getOutputIndex(exist));
    }

    @Test
    @DisplayName("Test Get Null Module From Procedure")
    void testGetNullModuleFromProcedure() {
        Procedure procedure = new Procedure();
        Assertions.assertEquals(-1, procedure.getModuleIndex(null));
    }

    @Test
    @DisplayName("Test Get Missed Module From Procedure")
    void testGetMissedModuleFromProcedure() {
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure();
        Assertions.assertEquals(-1, procedure.getModuleIndex(mod));
    }

    @Test
    @DisplayName("Test Get Single Module Index")
    void testGetSingleModuleIndex() {
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure();
        procedure.addModule(mod);
        Assertions.assertEquals(0, procedure.getModuleIndex(mod));
    }

    @Test
    @DisplayName("Test Get Double Module Index")
    void testGetDoubleModuleIndex() {
        var mod1 = new ProceduralModule("Test1", new IOPort[]{}, new IOPort[]{}, new Point());
        var mod2 = new ProceduralModule("Test2", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure();
        procedure.addModule(mod1);
        procedure.addModule(mod2);
        Assertions.assertEquals(0, procedure.getModuleIndex(mod1));
        Assertions.assertEquals(1, procedure.getModuleIndex(mod2));
    }

    @Test
    @DisplayName("Test Delete First Of Two Modules")
    void testDeleteFirstOfTwoModules() {
        var mod1 = new ProceduralModule("Test1", new IOPort[]{}, new IOPort[]{}, new Point());
        var mod2 = new ProceduralModule("Test2", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure();
        procedure.addModule(mod1);
        procedure.addModule(mod2);
        procedure.deleteModule(0);
        Assertions.assertEquals(0, procedure.getModuleIndex(mod2));
    }

    @Test
    @DisplayName("Test Delete Last Single Module")
    void testDeleteLastSingleModule() {
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure();
        procedure.addModule(mod);
        procedure.deleteModule(0);
    }

    @Test
    @DisplayName("Test Delete Missed Module")
    void testDeleteMissedModule() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
            Procedure procedure = new Procedure();
            procedure.addModule(mod);
            procedure.deleteModule(10);
        });
    }

    @Test
    @DisplayName("Test Init Module From Point")
    void testInitModuleFromPoint() {
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point()) {

            private PointInfo point;

            @Override
            public void init(PointInfo p) {
                super.init(p);
                bounds.x = (int) p.x;
                bounds.y = (int) p.y;
            }
        };
        Procedure procedure = new Procedure();
        procedure.addModule(mod);
        PointInfo pi = new PointInfo();
        pi.x = 100;
        pi.y = 100;
        procedure.initForPoint(pi);
        Assertions.assertEquals(100, mod.getBounds().getX(), 0.000001);
        Assertions.assertEquals(100, mod.getBounds().getY(), 0.000001);
    }

    @Test
    @DisplayName("Test Add Link")
    void testAddLink() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure procedure = new Procedure(exist);
        int linksCount = procedure.getLinks().length;
        IOPort from = new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.BOTTOM, new String[]{"Link From"});
        IOPort to = new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.TOP, new String[]{"Link To"});
        to.setModule(exist);
        Link link = new Link(from, to);
        procedure.addLink(link);
        Assertions.assertEquals(++linksCount, procedure.getLinks().length);
    }

    @Test
    @DisplayName("Test Add Two Links")
    void testAddTwoLinks() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure procedure = new Procedure(exist);
        IOPort from = new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.BOTTOM, new String[]{"Link From"});
        IOPort to = new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.TOP, new String[]{"Link To"});
        to.setModule(exist);
        Link link = new Link(from, to);
        procedure.addLink(link);
        from = new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.BOTTOM, new String[]{"Link From"});
        to = new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.TOP, new String[]{"Link To"});
        to.setModule(exist);
        link = new Link(from, to);
        procedure.addLink(link);
        Assertions.assertEquals(2, procedure.getLinks().length);
    }

    @Test
    @DisplayName("Test Procedure Copy With No Module")
    void testProcedureCopyWithNoModule() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        Procedure target = new Procedure(exist);
        target.copy(origin);
        Assertions.assertNotNull(target.getModules());
        Assertions.assertEquals(0, target.getModules().size());
        Assertions.assertNotNull(target.getLinks());
        Assertions.assertEquals(0, target.getLinks().length);
    }

    @Test
    @DisplayName("Test Procedure Copy With Single Module No Links")
    void testProcedureCopyWithSingleModuleNoLinks() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        origin.addModule(mod);
        Procedure target = new Procedure(exist);
        target.copy(origin);
        Assertions.assertNotNull(target.getModules());
        Assertions.assertEquals(1, target.getModules().size());
        Assertions.assertNotNull(target.getLinks());
        Assertions.assertEquals(0, target.getLinks().length);
    }

    @Test
    @DisplayName("Test Procedure Copy Two Modules With Single Link")
    void testProcedureCopyTwoModulesWithSingleLink() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        // Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        // Time module
        var coor = new CoordinateModule(new java.awt.Point(), 3);
        origin.addModule(coor);
        origin.addModule(sine);
        // Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];
        Link link = new Link(coorout, sinein);
        origin.addLink(link);
        // Create target procedure
        OutputModule cout = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure target = new Procedure(cout);
        target.copy(origin);
        Assertions.assertNotNull(target.getModules());
        Assertions.assertEquals(2, target.getModules().size());
        Assertions.assertNotNull(target.getLinks());
        Assertions.assertEquals(1, target.getLinks().length);
    }

    @Test
    @DisplayName("Test Procedure Copy Three Modules With Two Links")
    void testProcedureCopyThreeModulesWithTwoLinks() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        // Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        // Time module
        var coor = new CoordinateModule(new java.awt.Point(), 3);
        origin.addModule(coor);
        origin.addModule(sine);
        // Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];
        Link link = new Link(coorout, sinein);
        origin.addLink(link);
        // Made one more connection
        IOPort sineout = sine.getOutputPorts()[0];
        var procTm = origin.getOutputModules()[0];
        IOPort procTmIn = procTm.getInputPorts()[0];
        Link link2 = new Link(sineout, procTmIn);
        origin.addLink(link2);
        // Create target procedure
        OutputModule cout = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        var target = new Procedure(cout);
        target.copy(origin);
        Assertions.assertNotNull(target.getModules());
        Assertions.assertEquals(2, target.getModules().size());
        Assertions.assertNotNull(target.getLinks());
        Assertions.assertEquals(2, target.getLinks().length);
    }

    @Test
    @DisplayName("Test Proc Get Default Output Value")
    void testProcGetDefaultOutputValue() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 42, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        Assertions.assertEquals(42, origin.getOutputValue(0), 0);
    }

    @Test
    @DisplayName("Test Proc Get Default Output Gradient")
    void testProcGetDefaultOutputGradient() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 42, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        Vec3 grad = new Vec3(1, 2, 3);
        origin.getOutputGradient(0, grad);
        Assertions.assertEquals(0, grad.x, 0);
        Assertions.assertEquals(0, grad.y, 0);
        Assertions.assertEquals(0, grad.z, 0);
    }

    @Test
    @DisplayName("Test Proc Get Default Output Color")
    void testProcGetDefaultOutputColor() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 42, new RGBColor(0, 0.5, 1), 0);
        Procedure origin = new Procedure(exist);
        RGBColor color = new RGBColor();
        origin.getOutputColor(0, color);
        Assertions.assertEquals(0, color.red, 0);
        Assertions.assertEquals(0.5, color.green, 0);
        Assertions.assertEquals(1, color.blue, 0);
    }

    @Test
    @DisplayName("Test Delete Link From No Links")
    void testDeleteLinkFromNoLinks() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            // Create source procedure
            OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
            Procedure origin = new Procedure(exist);
            origin.deleteLink(0);
        });
    }

    @Test
    @DisplayName("Test Delete Illegal Index Link From No Links")
    void testDeleteIllegalIndexLinkFromNoLinks() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            // Create source procedure
            OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
            Procedure origin = new Procedure(exist);
            origin.deleteLink(10);
        });
    }

    @Test
    @DisplayName("Test Delete Single Link")
    void testDeleteSingleLink() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        // Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        // Time module
        var coor = new CoordinateModule(new java.awt.Point(), 3);
        origin.addModule(coor);
        origin.addModule(sine);
        // Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];
        Link link = new Link(coorout, sinein);
        origin.addLink(link);
        origin.deleteLink(0);
        Assertions.assertNotNull(origin.getLinks());
        Assertions. assertEquals(0, origin.getLinks().length);
        Assertions.assertNull(sine.linkFrom[0]);
    }

    @Test
    @DisplayName("Test Delete First Link Of Two")
    void testDeleteFirstLinkOfTwo() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        // Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        // Time module
        var coor = new CoordinateModule(new java.awt.Point(), 3);
        origin.addModule(coor);
        origin.addModule(sine);
        // Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];
        Link link = new Link(coorout, sinein);
        origin.addLink(link);
        // Made one more connection
        IOPort sineout = sine.getOutputPorts()[0];
        var procTm = origin.getOutputModules()[0];
        IOPort procTmIn = procTm.getInputPorts()[0];
        Link link2 = new Link(sineout, procTmIn);
        origin.addLink(link2);
        origin.deleteLink(0);
        Assertions.assertNotNull(origin.getLinks());
        Assertions.assertEquals(1, origin.getLinks().length);
        Assertions.assertNull(sine.linkFrom[0]);
    }

    @Test
    @DisplayName("Test Delete Second Link Of Two")
    void testDeleteSecondLinkOfTwo() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        // Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        // Time module
        var coor = new CoordinateModule(new java.awt.Point(), 3);
        origin.addModule(coor);
        origin.addModule(sine);
        // Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];
        Link link = new Link(coorout, sinein);
        origin.addLink(link);
        // Made one more connection
        IOPort sineout = sine.getOutputPorts()[0];
        var procTm = origin.getOutputModules()[0];
        IOPort procTmIn = procTm.getInputPorts()[0];
        Link link2 = new Link(sineout, procTmIn);
        origin.addLink(link2);
        origin.deleteLink(1);
        Assertions.assertNotNull(origin.getLinks());
        Assertions.assertEquals(1, origin.getLinks().length);
        Assertions.assertNull(procTm.linkFrom[0]);
    }

    @Test
    @DisplayName("Test Delete Single Link Back Direction")
    void testDeleteSingleLinkBackDirection() {
        // Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(exist);
        // Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        // Time module
        var coor = new CoordinateModule(new java.awt.Point(), 3);
        origin.addModule(coor);
        origin.addModule(sine);
        // Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];
        Link link = new Link(sinein, coorout);
        origin.addLink(link);
        origin.deleteLink(0);
        Assertions.assertNotNull(origin.getLinks());
        Assertions.assertEquals(0, origin.getLinks().length);
        Assertions.assertNull(sine.linkFrom[0]);
    }
}
