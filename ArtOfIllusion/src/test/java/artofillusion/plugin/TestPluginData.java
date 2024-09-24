package artofillusion.plugin;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class TestPluginData {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.allowTypes(new Class[]{Extension.class, Category.class, PluginDef.class});
        xstream.processAnnotations(new Class[]{Extension.class, Category.class, PluginDef.class});

    }
    @Test
    public void testReadEmptyExtension() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/EmptyExtension.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
    }

    @Test
    public void testReadCombinedExtension() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined0.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(2, ext.categoryList.size());

        ext.categoryList.forEach(cc -> System.out.println(cc.category));
    }

    @Test
    public void testReadCombinedExtension2() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined1.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(2, ext.pluginsList.size());

    }

    @Test
    void testBadXml() throws IOException {
        Assertions.assertThrows(com.thoughtworks.xstream.mapper.CannotResolveClassException.class, () -> {
            xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/noextension.xml").openStream());
        });
    }
}
