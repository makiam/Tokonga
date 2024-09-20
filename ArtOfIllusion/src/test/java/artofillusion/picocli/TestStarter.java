package artofillusion.picocli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class TestStarter {

    @Test
    void testStarter() {
        String[] args = new String[] {"--nosplash", "--old", "File1", "File2"};
        Assertions.assertEquals(0, new CommandLine(new Starter()).execute(args));
    }
}
