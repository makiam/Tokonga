package artofillusion.plugin;

import artofillusion.ApplicationPreferences;
import artofillusion.Plugin;
import artofillusion.keystroke.KeystrokeRecord;
import artofillusion.keystroke.KeystrokesList;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class TestPluginData {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.aliasSystemAttribute("name", "class");
        xstream.allowTypes(new Class[]{Plugin.Category.class});
        xstream.processAnnotations(new Class[]{Plugin.Category.class});


    }
    @Test
    public void testReadCategory() throws IOException {
        Plugin.Category cat = (Plugin.Category)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Category.xml").openStream());
        Assertions.assertEquals("CategoryClass", cat.getClassName());
    }
}
