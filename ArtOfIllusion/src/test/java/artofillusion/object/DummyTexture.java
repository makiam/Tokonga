package artofillusion.object;

import artofillusion.Scene;
import artofillusion.texture.Texture;
import artofillusion.texture.TextureMapping;
import artofillusion.texture.TextureSpec;
import buoy.widget.WindowWidget;
import org.junit.jupiter.api.DisplayName;

import java.io.DataOutputStream;
import java.io.IOException;

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
    public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
