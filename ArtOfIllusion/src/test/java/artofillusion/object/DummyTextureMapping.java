/* Copyright 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.MaterialPreviewer;
import artofillusion.TextureParameter;
import artofillusion.math.RGBColor;
import artofillusion.math.Vec3;
import artofillusion.texture.Texture;
import artofillusion.texture.TextureMapping;
import artofillusion.texture.TextureSpec;
import buoy.widget.Widget;
import org.junit.jupiter.api.DisplayName;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@DisplayName("Mock Texture Mapping")
class DummyTextureMapping extends TextureMapping {

    private final Object3D object;

    private final Texture texture;

    public DummyTextureMapping(DataInputStream in, Object3D obj, Texture texture) {
        this.texture = texture;
        this.object = obj;
    }

    @Override
    public void writeToFile(DataOutputStream out) throws IOException {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public Object3D getObject() {
        return object;
    }

    @Override
    public void getTextureSpec(Vec3 pos, TextureSpec spec, double angle, double size, double t, double[] param) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getTransparency(Vec3 pos, RGBColor trans, double angle, double size, double t, double[] param) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getDisplacement(Vec3 pos, double size, double t, double[] param) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TextureMapping duplicate() {
        return new DummyTextureMapping(null, object, texture);
    }

    @Override
    public TextureMapping duplicate(Object3D obj, Texture tex) {
        return new DummyTextureMapping(null, obj, tex);
    }

    /**
     * Make this mapping identical to another one.
     */
    @Override
    public void copy(TextureMapping map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Widget getEditingPanel(Object3D obj, MaterialPreviewer preview) {
        // To change body of generated methods, choose Tools | Templates.
        return null;
    }

    @Override
    public TextureParameter[] getParameters() {
        return super.getParameters();
    }
}
