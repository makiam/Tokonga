package artofillusion.theme;

import artofillusion.plugin.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestThemeData {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.ignoreUnknownElements();
        xstream.allowTypes(new Class[]{ThemeDef.class});
        xstream.processAnnotations(new Class[]{ThemeDef.class});
    }

    @Test
    void testThemeData() {

    }
}
