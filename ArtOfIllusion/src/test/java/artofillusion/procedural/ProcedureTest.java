/* Copyright (C) 2017-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.math.RGBColor;
import artofillusion.math.Vec3;
import java.awt.Point;
import org.junit.Assert;

import org.junit.Test;

/**
 *
 * @author MaksK
 */
public class ProcedureTest {

    @Test
    public void testCreateNewProcedure() {
        Procedure procedure = new Procedure(new OutputModule[]{});
        Assert.assertNotNull(procedure);
        Assert.assertNotNull(procedure.getOutputModules());
        Assert.assertEquals(0, procedure.getOutputModules().length);
        Assert.assertNotNull(procedure.getModules());
        Assert.assertEquals(0, procedure.getModules().length);

        Assert.assertNotNull(procedure.getLinks());
        Assert.assertEquals(0, procedure.getLinks().length);
    }

    @Test
    public void testGetNullOutputModuleIndexFromProcedure() {
        Procedure procedure = new Procedure(new OutputModule[]{});
        Assert.assertEquals(-1, procedure.getOutputIndex(null));
    }

    @Test
    public void testGetMissedOutputModuleIndexFromProcedure() {

        Procedure procedure = new Procedure(new OutputModule[]{});

        OutputModule missed = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Assert.assertEquals(-1, procedure.getOutputIndex(missed));

    }

    @Test
    public void testGetSingleOutputModuleIndexFromProcedure() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure procedure = new Procedure(new OutputModule[]{exist});

        Assert.assertEquals(0, procedure.getOutputIndex(exist));

    }

    @Test
    public void testGetSecondOutputModuleIndexFromProcedure() {
        OutputModule first = new OutputModule("TestOut1", "Label0", 0, new RGBColor(1, 1, 1), 0);
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure procedure = new Procedure(new OutputModule[]{first, exist});

        Assert.assertEquals(1, procedure.getOutputIndex(exist));

    }

    @Test
    public void testGetNullModuleFromProcedure() {
        Procedure procedure = new Procedure(new OutputModule[]{});
        Assert.assertEquals(-1, procedure.getModuleIndex(null));
    }

    @Test
    public void testGetMissedModuleFromProcedure() {
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure(new OutputModule[]{});
        Assert.assertEquals(-1, procedure.getModuleIndex(mod));
    }

    @Test
    public void testGetSingleModuleIndex() {
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure(new OutputModule[]{});
        procedure.addModule(mod);
        Assert.assertEquals(0, procedure.getModuleIndex(mod));
    }

    @Test
    public void testGetDoubleModuleIndex() {
        var mod1 = new ProceduralModule("Test1", new IOPort[]{}, new IOPort[]{}, new Point());
        var mod2 = new ProceduralModule("Test2", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure(new OutputModule[]{});
        procedure.addModule(mod1);
        procedure.addModule(mod2);
        Assert.assertEquals(0, procedure.getModuleIndex(mod1));
        Assert.assertEquals(1, procedure.getModuleIndex(mod2));
    }

    @Test
    public void testDeleteFirstOfTwoModules() {
        var mod1 = new ProceduralModule("Test1", new IOPort[]{}, new IOPort[]{}, new Point());
        var mod2 = new ProceduralModule("Test2", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure(new OutputModule[]{});
        procedure.addModule(mod1);
        procedure.addModule(mod2);

        procedure.deleteModule(0);
        Assert.assertEquals(0, procedure.getModuleIndex(mod2));
    }

    @Test
    public void testDeleteLastSingleModule() {
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure(new OutputModule[]{});
        procedure.addModule(mod);

        procedure.deleteModule(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteMissedModule() {
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        Procedure procedure = new Procedure(new OutputModule[]{});
        procedure.addModule(mod);

        procedure.deleteModule(10);
    }

    @Test
    public void testInitModuleFromPoint() {

        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point()) {
            private PointInfo point;

            @Override
            public void init(PointInfo p) {
                super.init(p);
                bounds.x = (int) p.x;
                bounds.y = (int) p.y;
            }

        };
        Procedure procedure = new Procedure(new OutputModule[]{});
        procedure.addModule(mod);
        PointInfo pi = new PointInfo();
        pi.x = 100;
        pi.y = 100;
        procedure.initForPoint(pi);

        Assert.assertEquals(100, mod.getBounds().getX(), 0.000001);
        Assert.assertEquals(100, mod.getBounds().getY(), 0.000001);

    }

    @Test
    public void testAddLink() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure procedure = new Procedure(new OutputModule[]{exist});
        int linksCount = procedure.getLinks().length;
        IOPort from = new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.BOTTOM, new String[]{"Link From"});
        IOPort to = new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.TOP, new String[]{"Link To"});
        to.setModule(exist);

        Link link = new Link(from, to);
        procedure.addLink(link);

        Assert.assertEquals(++linksCount, procedure.getLinks().length);
    }

    @Test
    public void testAddTwoLinks() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure procedure = new Procedure(new OutputModule[]{exist});

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

        Assert.assertEquals(2, procedure.getLinks().length);
    }

    @Test
    public void testProcedureCopyWithNoModule() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        Procedure target = new Procedure(new OutputModule[]{exist});

        target.copy(origin);

        Assert.assertNotNull(target.getModules());
        Assert.assertEquals(0, target.getModules().length);

        Assert.assertNotNull(target.getLinks());
        Assert.assertEquals(0, target.getLinks().length);
    }

    @Test
    public void testProcedureCopyWithSingleModuleNoLinks() {
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});
        var mod = new ProceduralModule("Test", new IOPort[]{}, new IOPort[]{}, new Point());
        origin.addModule(mod);

        Procedure target = new Procedure(new OutputModule[]{exist});

        target.copy(origin);

        Assert.assertNotNull(target.getModules());
        Assert.assertEquals(1, target.getModules().length);

        Assert.assertNotNull(target.getLinks());
        Assert.assertEquals(0, target.getLinks().length);
    }

    @Test
    public void testProcedureCopyTwoModulesWithSingleLink() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        //Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        var coor = new CoordinateModule(new java.awt.Point(), 3); //Time module
        origin.addModule(coor);
        origin.addModule(sine);

        //Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];

        Link link = new Link(coorout, sinein);
        origin.addLink(link);

        //Create target procedure
        OutputModule cout = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure target = new Procedure(new OutputModule[]{cout});

        target.copy(origin);

        Assert.assertNotNull(target.getModules());
        Assert.assertEquals(2, target.getModules().length);

        Assert.assertNotNull(target.getLinks());
        Assert.assertEquals(1, target.getLinks().length);

    }

    @Test
    public void testProcedureCopyThreeModulesWithTwoLinks() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        //Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        var coor = new CoordinateModule(new java.awt.Point(), 3); //Time module
        origin.addModule(coor);
        origin.addModule(sine);

        //Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];

        Link link = new Link(coorout, sinein);
        origin.addLink(link);

        //Made one more connection
        IOPort sineout = sine.getOutputPorts()[0];
        var procTm = origin.getOutputModules()[0];
        IOPort procTmIn = procTm.getInputPorts()[0];

        Link link2 = new Link(sineout, procTmIn);
        origin.addLink(link2);

        //Create target procedure
        OutputModule cout = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure target = new Procedure(new OutputModule[]{cout});

        target.copy(origin);

        Assert.assertNotNull(target.getModules());
        Assert.assertEquals(2, target.getModules().length);

        Assert.assertNotNull(target.getLinks());
        Assert.assertEquals(2, target.getLinks().length);
    }

    @Test
    public void testProcGetDefaultOutputValue() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 42, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        Assert.assertEquals(42, origin.getOutputValue(0), 0);
    }

    @Test
    public void testProcGetDefaultOutputGradient() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 42, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        Vec3 grad = new Vec3(1, 2, 3);
        origin.getOutputGradient(0, grad);
        Assert.assertEquals(0, grad.x, 0);
        Assert.assertEquals(0, grad.y, 0);
        Assert.assertEquals(0, grad.z, 0);
    }

    @Test
    public void testProcGetDefaultOutputColor() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 42, new RGBColor(0, 0.5, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        RGBColor color = new RGBColor();
        origin.getOutputColor(0, color);
        Assert.assertEquals(0, color.red, 0);
        Assert.assertEquals(0.5, color.green, 0);
        Assert.assertEquals(1, color.blue, 0);
    }

    @Test(expected = NegativeArraySizeException.class)
    public void testDeleteLinkFromNoLinks() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        origin.deleteLink(0);
    }

    @Test(expected = NegativeArraySizeException.class)
    public void testDeleteIllegalIndexLinkFromNoLinks() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        origin.deleteLink(10);
    }

    @Test
    public void testDeleteSingleLink() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        //Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        var coor = new CoordinateModule(new java.awt.Point(), 3); //Time module
        origin.addModule(coor);
        origin.addModule(sine);

        //Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];

        Link link = new Link(coorout, sinein);
        origin.addLink(link);

        origin.deleteLink(0);

        Assert.assertNotNull(origin.getLinks());
        Assert.assertEquals(0, origin.getLinks().length);

        Assert.assertNull(sine.linkFrom[0]);

    }

    @Test
    public void testDeleteFirstLinkOfTwo() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        //Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        var coor = new CoordinateModule(new java.awt.Point(), 3); //Time module
        origin.addModule(coor);
        origin.addModule(sine);

        //Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];

        Link link = new Link(coorout, sinein);
        origin.addLink(link);

        //Made one more connection
        IOPort sineout = sine.getOutputPorts()[0];
        var procTm = origin.getOutputModules()[0];
        IOPort procTmIn = procTm.getInputPorts()[0];

        Link link2 = new Link(sineout, procTmIn);
        origin.addLink(link2);

        origin.deleteLink(0);

        Assert.assertNotNull(origin.getLinks());
        Assert.assertEquals(1, origin.getLinks().length);

        Assert.assertNull(sine.linkFrom[0]);

    }

    @Test
    public void testDeleteSecondLinkOfTwo() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        //Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        var coor = new CoordinateModule(new java.awt.Point(), 3); //Time module
        origin.addModule(coor);
        origin.addModule(sine);

        //Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];

        Link link = new Link(coorout, sinein);
        origin.addLink(link);

        //Made one more connection
        IOPort sineout = sine.getOutputPorts()[0];
        var procTm = origin.getOutputModules()[0];
        IOPort procTmIn = procTm.getInputPorts()[0];

        Link link2 = new Link(sineout, procTmIn);
        origin.addLink(link2);

        origin.deleteLink(1);

        Assert.assertNotNull(origin.getLinks());
        Assert.assertEquals(1, origin.getLinks().length);

        Assert.assertNull(procTm.linkFrom[0]);

    }

    @Test
    public void testDeleteSingleLinkBackDirection() {
        //Create source procedure
        OutputModule exist = new OutputModule("TestOut", "Label", 0, new RGBColor(1, 1, 1), 0);
        Procedure origin = new Procedure(new OutputModule[]{exist});

        //Create module with singe output Port
        var sine = new SineModule(new java.awt.Point());
        var coor = new CoordinateModule(new java.awt.Point(), 3); //Time module
        origin.addModule(coor);
        origin.addModule(sine);

        //Made connection
        IOPort sinein = sine.getInputPorts()[0];
        IOPort coorout = coor.getOutputPorts()[0];

        Link link = new Link(sinein, coorout);
        origin.addLink(link);

        origin.deleteLink(0);

        Assert.assertNotNull(origin.getLinks());
        Assert.assertEquals(0, origin.getLinks().length);

        Assert.assertNull(sine.linkFrom[0]);

    }
}
