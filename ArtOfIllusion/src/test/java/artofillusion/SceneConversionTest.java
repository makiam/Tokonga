package artofillusion;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class SceneConversionTest {

    @Test
    public void testConvertScene() throws IOException {

        var scene = new Scene(Path.of("/home/huawei/Downloads/30Glasflaschen.aoi").toFile());
        scene.writeToFile(Path.of("/home/huawei/Downloads/30GlasflaschenV6.aoi").toFile());
    }


}
