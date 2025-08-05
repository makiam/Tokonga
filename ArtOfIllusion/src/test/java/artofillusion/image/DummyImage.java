/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.image;

import artofillusion.Scene;
import artofillusion.math.RGBColor;
import artofillusion.math.Vec2;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class DummyImage extends ImageMap {

    @Override
    public int getWidth() {
        return 100;
    }

    @Override
    public int getHeight() {
        return 100;
    }

    @Override
    public float getAspectRatio() {
        return 1.0f;
    }

    @Override
    public int getComponentCount() {
        return 3;
    }

    @Override
    public float getComponent(int component, boolean wrapx, boolean wrapy, double x, double y, double xsize, double ysize) {
        return 0;
    }

    @Override
    public float getAverageComponent(int component) {
        return 0;
    }

    @Override
    public void getColor(RGBColor theColor, boolean wrapx, boolean wrapy, double x, double y, double xsize, double ysize) {

    }

    @Override
    public void getGradient(Vec2 grad, int component, boolean wrapx, boolean wrapy, double x, double y, double xsize, double ysize) {

    }

    @Override
    public Image getPreview() {
        return null;
    }

    @Override
    public Image getPreview(int size) {
        return null;
    }

    @Override
    public void writeToStream(DataOutputStream out, Scene scene) throws IOException {
        out.writeDouble(Math.PI);
    }
}
