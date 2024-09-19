package artofillusion.plugin;

import artofillusion.ApplicationPreferences;
import artofillusion.Plugin;
import artofillusion.keystroke.KeystrokeRecord;
import artofillusion.keystroke.KeystrokesList;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class TestPluginData {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.allowTypes(new Class[]{Plugin.Extension.class});
        xstream.processAnnotations(new Class[]{Plugin.Extension.class});

    }
    @Test
    public void testReadExtension() {

    }
}
