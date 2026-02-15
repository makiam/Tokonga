/* Copyright (C) 2018-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.material;

import artofillusion.MaterialPreviewer;
import artofillusion.math.Vec3;
import artofillusion.object.Object3D;
import buoy.widget.Widget;

import java.io.DataInputStream;
import java.io.DataOutputStream;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;


/**
 * @author maksim.khramov
 */
@DisplayName("Material Mapping Test")
class MaterialMappingTest {

    @Test
    @DisplayName("Create Material Mapping")
    void createMaterialMapping() {
    }

    @DisplayName("Dummy Mapping")
    static class DummyMapping extends MaterialMapping {

        public DummyMapping(Object3D obj, Material mat) {
            super(obj, mat);
        }

        public DummyMapping(DataInputStream in, Object3D obj, Material mat) {
            this(obj, mat);
        }

        @Override
        public void writeToFile(DataOutputStream out) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getStepSize() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void getMaterialSpec(Vec3 pos, MaterialSpec spec, double size, double t) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MaterialMapping duplicate() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MaterialMapping duplicate(Object3D obj, Material mat) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void copy(MaterialMapping map) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Widget getEditingPanel(Object3D obj, MaterialPreviewer preview) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
