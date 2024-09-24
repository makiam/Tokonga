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
        xstream.allowTypes(new Class[]{Extension.class, Category.class, PluginDef.class, ImportDef.class, Export.class});
        xstream.processAnnotations(new Class[]{Extension.class, Category.class, PluginDef.class, ImportDef.class, Export.class});


    }
    @Test
    void testReadEmptyExtension() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/EmptyExtension.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
    }

    @Test
    void testReadCombinedExtension0() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined0.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(2, ext.getCategoryList().size());

        ext.getCategoryList().forEach(cc -> System.out.println(cc.getCategory()));
    }

    @Test
    void testReadCombinedExtension1() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined1.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(0, ext.getCategoryList().size());
        Assertions.assertEquals(2, ext.getPluginsList().size());

    }

    @Test
    void testReadCombinedExtension2() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined2.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(0, ext.getCategoryList().size());
        Assertions.assertEquals(1, ext.getImports().size());

    }

    @Test
    void testGetPluginExports() throws IOException {
        Extension ext = (Extension)xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/Combined3.xml").openStream());
        Assertions.assertEquals("Extension", ext.getName());
        Assertions.assertEquals(1, ext.getPluginsList().size());

        Assertions.assertEquals(2, ext.getPluginsList().get(0).getExports().size());
        String def = ext.getPluginsList().get(0).getExports().get(1).getDescription();
        Assertions.assertEquals("Some description", def);
    }

    @Test
    void testBadXml() {
        Assertions.assertThrows(com.thoughtworks.xstream.mapper.CannotResolveClassException.class, () -> {
            xstream.fromXML(TestPluginData.class.getResource("/artofillusion/plugin/noextension.xml").openStream());
        });
    }
}
