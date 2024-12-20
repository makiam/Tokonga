package artofillusion.object;

import artofillusion.WireframeMesh;
import artofillusion.animation.Keyframe;
import artofillusion.math.BoundingBox;

class Dummy3DObject extends Object3D {

    public Dummy3DObject() {
        super();
    }

    @Override
    public Object3D duplicate() {
        return new Dummy3DObject();
    }

    @Override
    public void copyObject(Object3D obj) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BoundingBox getBounds() {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSize(double xsize, double ysize, double zsize) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WireframeMesh getWireframeMesh() {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Keyframe getPoseKeyframe() {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void applyPoseKeyframe(Keyframe k) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
