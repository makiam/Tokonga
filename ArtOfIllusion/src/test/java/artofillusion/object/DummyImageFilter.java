/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.Scene;
import artofillusion.image.ComplexImage;
import artofillusion.image.filter.ImageFilter;
import artofillusion.math.CoordinateSystem;
import org.junit.jupiter.api.DisplayName;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@DisplayName("Dummy Image Filter")
public class DummyImageFilter extends ImageFilter {

    @Override
    public String getName() {
        // To change body of generated methods, choose Tools | Templates.
        return "Dummy Image Filter";
    }

    @Override
    public void filterImage(ComplexImage image, Scene scene, SceneCamera camera, CoordinateSystem cameraPos) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
    }

    @Override
    public void initFromStream(DataInputStream in, Scene theScene) throws IOException {
    }
}
