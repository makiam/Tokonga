package artofillusion.theme;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import theme.UIThemeColorSet;

import java.io.IOException;

public class TestThemeData {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.ignoreUnknownElements();
        xstream.allowTypes(new Class[]{UITheme.class, UIThemeColorSet.class});
        xstream.processAnnotations(new Class[]{UITheme.class, UIThemeColorSet.class});
    }

    @Test
    void testThemeData() throws IOException {
        UITheme theme = (UITheme)xstream.fromXML(UITheme.class.getResource("/artofillusion/theme/Theme1/theme.xml").openStream());

        Assertions.assertEquals("ElectricWax Grey", theme.getName());
        Assertions.assertNotNull(theme.getColorSets());
        Assertions.assertEquals(6, theme.getColorSets().size());
    }
}
