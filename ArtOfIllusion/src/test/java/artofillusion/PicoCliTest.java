package artofillusion;

import groovyjarjarpicocli.CommandLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PicoCliTest {

    @Test
    void testGetPicoCliVersion() {
        Assertions.assertNotNull(CommandLine.VERSION);
    }
}
