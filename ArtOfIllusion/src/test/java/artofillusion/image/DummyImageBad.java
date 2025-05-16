package artofillusion.image;

import artofillusion.Scene;
import artofillusion.math.RGBColor;
import artofillusion.math.Vec2;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class DummyImageBad extends ImageMap {

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
