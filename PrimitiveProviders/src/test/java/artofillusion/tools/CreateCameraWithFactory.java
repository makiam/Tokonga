package artofillusion.tools;

import artofillusion.object.Object3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class CreateCameraWithFactory {

    @Test
    void createCamera() {
        PrimitiveFactory factory = new CameraFactory();
        Optional<Object3D> camera = factory.create();
        Assertions.assertTrue(factory.create().isPresent());
    }
}
