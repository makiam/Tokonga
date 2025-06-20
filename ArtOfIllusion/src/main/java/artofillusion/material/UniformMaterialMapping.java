/* Copyright (C) 2000-2007 by Peter Eastman
   Changes copyright (C) 2024-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.material;

import artofillusion.*;
import artofillusion.api.ImplementationVersion;
import artofillusion.math.*;
import artofillusion.object.*;
import buoy.widget.*;
import java.io.*;

/**
 * UniformMaterialMapping is the MaterialMapping for UniformMaterials.
 */
@ImplementationVersion
public class UniformMaterialMapping extends MaterialMapping {

    public UniformMaterialMapping(Object3D theObject, Material theMaterial) {
        super(theObject, theMaterial);
    }

    @Override
    public double getStepSize() {
        return material.getStepSize();
    }

    @Override
    public void getMaterialSpec(Vec3 pos, MaterialSpec spec, double size, double t) {
        ((UniformMaterial) material).getMaterialSpec(spec);
    }

    @Override
    public boolean legalMapping(Object3D obj, Material mat) {
        return mat instanceof UniformMaterial;
    }

    @Override
    public MaterialMapping duplicate() {
        return new UniformMaterialMapping(object, material);
    }

    @Override
    public MaterialMapping duplicate(Object3D obj, Material mat) {
        return new UniformMaterialMapping(obj, mat);
    }

    @Override
    public void copy(MaterialMapping map) {
        material = map.material;
    }

    @Override
    public Widget getEditingPanel(Object3D obj, MaterialPreviewer preview) {
        return new CustomWidget();
    }

    public UniformMaterialMapping(DataInputStream in, Object3D theObject, Material theMaterial) throws IOException {
        super(theObject, theMaterial);

        short version = in.readShort();

        if (version != 0) {
            throw new InvalidObjectException("");
        }
        material = theMaterial;
    }

    @Override
    public void writeToFile(DataOutputStream out) throws IOException {
        out.writeShort(0);
    }
}
