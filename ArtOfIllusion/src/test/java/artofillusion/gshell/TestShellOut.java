package artofillusion.gshell;

import artofillusion.ArtOfIllusion;
import groovy.lang.Binding;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@Slf4j
public class TestShellOut {


    @Test
    public void testGetShell() {
        var shell = ArtOfIllusion.getShell();
        Binding context = shell.getContext();
        context.setProperty("out", new PrintStream(new ByteArrayOutputStream()) {
            public void println(String message) {
                log.info(message);
            }
        });
        var script = shell.parse("print 'Hello World'");
        script.run();

    }
}
