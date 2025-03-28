package artofillusion.animation;

import artofillusion.LayoutWindow;
import artofillusion.Scene;
import org.junit.jupiter.api.DisplayName;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@DisplayName("Dummy Track")
class DummyTrack extends Track<DummyTrack> {

    @Override
    public void edit(LayoutWindow win) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void apply(double time) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DummyTrack duplicate(Object parent) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copy(DummyTrack tr) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[] getKeyTimes() {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int moveKeyframe(int which, double time) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteKeyframe(int which) {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isNullTrack() {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeToStream(DataOutputStream out, Scene scene) throws IOException {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initFromStream(DataInputStream in, Scene scene) throws IOException {
        // To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
