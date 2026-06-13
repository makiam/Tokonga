package artofillusion.translators;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PLYReaderTest {

    @Test
    void airplane() throws IOException {

        var airplane = PLYReader.read("C:\\Work Files\\GitHub\\Tokonga\\Translators\\src\\test\\resources\\artofillusion\\translators\\airplane.ply");

    }
}
