package nik777.aoi;

import artofillusion.Scene;
import artofillusion.WireframeMesh;
import artofillusion.animation.Keyframe;
import artofillusion.math.BoundingBox;
import artofillusion.object.Object3D;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MetaObject extends Object3D {

    private static BoundingBox bounds = new BoundingBox(-0.25, 0.25, -0.25, 0.25, 0.0, 0.0);

    protected static Map<Scene, MetaObject> extent = new HashMap<>();

    public static MetaObject of(Scene scene) {
        return null;
    }

    public static void release(Scene scene) {
        extent.remove(scene);
    }

    @Override
    public Object3D duplicate() {
        return null;
    }


    @Override
    public void copyObject(Object3D obj) {

    }


    @Override
    public BoundingBox getBounds() {
        return null;
    }


    @Override
    public void setSize(double xsize, double ysize, double zsize) {

    }


    @Override
    public WireframeMesh getWireframeMesh() {
        return null;
    }


    @Override
    public Keyframe getPoseKeyframe() {
        return null;
    }


    @Override
    public void applyPoseKeyframe(Keyframe k) {

    }

    @Override
    public void writeToFile(DataOutputStream out, Scene scene) throws IOException {
        super.writeToFile(out, scene);
    }
}
