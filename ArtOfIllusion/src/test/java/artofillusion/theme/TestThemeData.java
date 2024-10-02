package artofillusion.theme;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;

public class TestThemeData {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.ignoreUnknownElements();
        xstream.allowTypes(new Class[]{UITheme.class, UIThemeColorSet.class, Button.class, ButtonStyle.class});
        xstream.processAnnotations(new Class[]{UITheme.class, UIThemeColorSet.class, Button.class, ButtonStyle.class});
    }

    @Test
    void testThemeData() throws IOException {
        UITheme theme = (UITheme)xstream.fromXML(UITheme.class.getResource("/artofillusion/theme/Theme1/theme.xml").openStream());

        Assertions.assertEquals("ElectricWax Grey", theme.getName());
        Assertions.assertNotNull(theme.getColorSets());
        Assertions.assertEquals(6, theme.getColorSets().size());

        Assertions.assertEquals(13, theme.getButtonMargin());
        Assertions.assertEquals(9, theme.getPaletteMargin());

        Assertions.assertNotNull(theme.getButtons());
        UIThemeColorSet cs = theme.getColorSets().get(0);

        Assertions.assertEquals(new Color(43, 144, 255), cs.getDockableBarColor2().getColor());


    }
}
