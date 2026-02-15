/* Copyright (C) 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.Scene;
import artofillusion.texture.Texture;
import artofillusion.texture.TextureMapping;
import artofillusion.texture.TextureSpec;
import buoy.widget.WindowWidget;
import org.junit.jupiter.api.DisplayName;

import java.io.DataOutputStream;


@DisplayName("Mock Texture")
class DummyTexture extends Texture {

    @Override
    public boolean hasComponent(int component) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getAverageSpec(TextureSpec spec, double time, double[] param) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TextureMapping getDefaultMapping(Object3D object) {
        return new DummyTextureMapping(null, object, this);
    }

    @Override
    public Texture duplicate() {
        return new DummyTexture();
    }

    @Override
    public void edit(WindowWidget<?> fr, Scene sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeToFile(DataOutputStream out, Scene theScene) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
