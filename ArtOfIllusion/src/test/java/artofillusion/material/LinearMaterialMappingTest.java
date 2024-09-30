/* Copyright (C) 2018 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.material;

import artofillusion.Scene;
import artofillusion.object.Cube;
import artofillusion.test.util.StreamUtil;
import buoy.widget.BFrame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import buoy.widget.WindowWidget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author maksim.khramov
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")
@DisplayName("Linear Material Mapping Test")
class LinearMaterialMappingTest {

    @Test
    @DisplayName("Test Create LMM")
    void testCreateLMM() {
        Cube cube = new Cube(1, 1, 1);
        Material3D mat = new DummyMaterial();
        LinearMaterialMapping lmm = new LinearMaterialMapping(cube, mat);
        Assertions.assertEquals(mat, lmm.getMaterial());
        Assertions.assertEquals(lmm.getName(), "Linear");
    }

    @Test
    @DisplayName("Test Create LMM From Stream Bad Version 1")
    void testCreateLMMFromStreamBadVersion1() {
        assertThrows(InvalidObjectException.class, () -> {
            Cube cube = new Cube(1, 1, 1);
            Material3D mat = new DummyMaterial();
            ByteBuffer wrap = ByteBuffer.allocate(2);
            wrap.putShort((short) -1);
            new LinearMaterialMapping(StreamUtil.stream(wrap), cube, mat);
        });
    }

    @Test
    @DisplayName("Test Create LMM From Stream Bad Version 2")
    void testCreateLMMFromStreamBadVersion2() {
        assertThrows(InvalidObjectException.class, () -> {
            Cube cube = new Cube(1, 1, 1);
            Material3D mat = new DummyMaterial();
            ByteBuffer wrap = ByteBuffer.allocate(2);
            wrap.putShort((short) 2);
            new LinearMaterialMapping(StreamUtil.stream(wrap), cube, mat);
            // 
            wrap.putDouble(1.0);
            wrap.putDouble(2.0);
            wrap.putDouble(3.0);
            wrap.putDouble(0.0);
            wrap.putDouble(45.0);
            wrap.putDouble(90.0);
        });
    }

    @Test
    @DisplayName("Test Create LMM From Stream Version 0")
    void testCreateLMMFromStreamVersion0() throws IOException {
        Cube cube = new Cube(1, 1, 1);
        Material3D mat = new DummyMaterial();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Version
        wrap.putShort((short) 0);
        // Coordinate system data
        wrap.putDouble(1.0);
        wrap.putDouble(2.0);
        wrap.putDouble(3.0);
        wrap.putDouble(0.0);
        wrap.putDouble(45.0);
        wrap.putDouble(90.0);
        // dx,dy,dz
        wrap.putDouble(4.0);
        wrap.putDouble(5.0);
        wrap.putDouble(6.0);
        // x, y, z scales
        wrap.putDouble(0.1);
        wrap.putDouble(0.5);
        wrap.putDouble(3.5);
        LinearMaterialMapping lmm = new LinearMaterialMapping(StreamUtil.stream(wrap), cube, mat);
        Assertions.assertEquals(mat, lmm.getMaterial());
        Assertions.assertEquals(false, lmm.isScaledToObject());
        Assertions.assertEquals(1.0, lmm.coords.getOrigin().x, 0);
        Assertions.assertEquals(2.0, lmm.coords.getOrigin().y, 0);
        Assertions.assertEquals(3.0, lmm.coords.getOrigin().z, 0);
        Assertions.assertEquals(0.1, lmm.xscale, 0);
        Assertions.assertEquals(0.5, lmm.yscale, 0);
        Assertions.assertEquals(3.5, lmm.zscale, 0);
    }

    @Test
    @DisplayName("Test Create LMM From Stream Version 1")
    void testCreateLMMFromStreamVersion1() throws IOException {
        Cube cube = new Cube(1, 1, 1);
        Material3D mat = new DummyMaterial();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Version
        wrap.putShort((short) 1);
        // Coordinate system data
        wrap.putDouble(1.0);
        wrap.putDouble(2.0);
        wrap.putDouble(3.0);
        wrap.putDouble(0.0);
        wrap.putDouble(45.0);
        wrap.putDouble(90.0);
        // dx,dy,dz
        wrap.putDouble(4.0);
        wrap.putDouble(5.0);
        wrap.putDouble(6.0);
        // x, y, z scales
        wrap.putDouble(0.1);
        wrap.putDouble(0.5);
        wrap.putDouble(3.5);
        // scale to object
        wrap.put((byte) 1);
        LinearMaterialMapping lmm = new LinearMaterialMapping(StreamUtil.stream(wrap), cube, mat);
        Assertions.assertEquals(mat, lmm.getMaterial());
        Assertions.assertEquals(true, lmm.isScaledToObject());
        Assertions.assertEquals(1.0, lmm.coords.getOrigin().x, 0);
        Assertions.assertEquals(2.0, lmm.coords.getOrigin().y, 0);
        Assertions.assertEquals(3.0, lmm.coords.getOrigin().z, 0);
        Assertions.assertEquals(0.1, lmm.xscale, 0);
        Assertions.assertEquals(0.5, lmm.yscale, 0);
        Assertions.assertEquals(3.5, lmm.zscale, 0);
    }

    @Test
    @DisplayName("Test Create LMM From Stream Version 1 Unscaled")
    void testCreateLMMFromStreamVersion1Unscaled() throws IOException {
        Cube cube = new Cube(1, 1, 1);
        Material3D mat = new DummyMaterial();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Version
        wrap.putShort((short) 1);
        // Coordinate system data
        wrap.putDouble(1.0);
        wrap.putDouble(2.0);
        wrap.putDouble(3.0);
        wrap.putDouble(0.0);
        wrap.putDouble(45.0);
        wrap.putDouble(90.0);
        // dx,dy,dz
        wrap.putDouble(4.0);
        wrap.putDouble(5.0);
        wrap.putDouble(6.0);
        // x, y, z scales
        wrap.putDouble(0.1);
        wrap.putDouble(0.5);
        wrap.putDouble(3.5);
        // scale to object
        // Boolean treats as byte
        wrap.put((byte) 0);
        LinearMaterialMapping lmm = new LinearMaterialMapping(StreamUtil.stream(wrap), cube, mat);
        Assertions.assertEquals(mat, lmm.getMaterial());
        Assertions.assertEquals(false, lmm.isScaledToObject());
        Assertions.assertEquals(1.0, lmm.coords.getOrigin().x, 0);
        Assertions.assertEquals(2.0, lmm.coords.getOrigin().y, 0);
        Assertions.assertEquals(3.0, lmm.coords.getOrigin().z, 0);
        Assertions.assertEquals(0.1, lmm.xscale, 0);
        Assertions.assertEquals(0.5, lmm.yscale, 0);
        Assertions.assertEquals(3.5, lmm.zscale, 0);
    }

    @DisplayName("Dummy Material")
    private class DummyMaterial extends Material3D {

        @Override
        public void getMaterialSpec(MaterialSpec spec, double x, double y, double z, double xsize, double ysize, double zsize, double t) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isScattering() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean castsShadows() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Material duplicate() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void edit(WindowWidget fr, Scene sc) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
