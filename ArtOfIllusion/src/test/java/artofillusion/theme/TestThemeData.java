package artofillusion.theme;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.IIOException;
import java.awt.*;
import java.io.IOException;

public class TestThemeData {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.ignoreUnknownElements();

        xstream.useAttributeFor(Button.class, "buttonClass");
        xstream.aliasAttribute("class", "buttonClass");
        xstream.aliasSystemAttribute("buttonClass", "class");

        xstream.allowTypes(new Class[]{UITheme.class, UIThemeColorSet.class, Button.class, StyleAttribute.class});
        xstream.processAnnotations(new Class[]{UITheme.class, UIThemeColorSet.class, Button.class, StyleAttribute.class});

    }

    @Test
    void testThemeNameOmitted() throws IOException {
        UITheme theme = getTheme("Theme3");
        Assertions.assertNotNull(theme.getName());
        Assertions.assertEquals("", theme.getName());
    }

    @Test
    void testThemeButtonOmitted() throws IOException {
        UITheme theme = getTheme("Theme3");
        Assertions.assertNull(theme.getButton());
    }

    @Test
    void testThemeData() throws IOException {
        UITheme theme = getTheme("Theme1");

        Assertions.assertNotNull(theme.getName());
        Assertions.assertEquals("ElectricWax Grey", theme.getName());
        Assertions.assertNotNull(theme.getColorSets());
        Assertions.assertEquals(6, theme.getColorSets().size());

        Assertions.assertEquals(13, theme.getButtonMargin());
        Assertions.assertEquals(9, theme.getPaletteMargin());

        Assertions.assertNotNull(theme.getButton());
        UIThemeColorSet cs = theme.getColorSets().get(0);

        Assertions.assertEquals(new Color(43, 144, 255), cs.getDockableBarColor2());



    }

    @Test
    void testThemeIsSelectableNoAttr() throws IOException {
        UITheme theme = getTheme("Theme1");
        Assertions.assertTrue(theme.isSelectable());
    }

    @Test
    void testNoButton() throws IOException {
        UITheme theme = getTheme("Theme2");
        Assertions.assertNull(theme.getButton());
    }

    @Test
    void testButton() throws IOException {
        UITheme theme = getTheme("Theme1");
        Assertions.assertNotNull(theme.getButton());
        Assertions.assertEquals(2, theme.getButton().getStyles().size());
        Assertions.assertEquals("artofillusion.ui.DefaultToolButton", theme.getButton().getButtonClass());
    }

    @Test
    void testNoColorSet() throws IOException {
        UITheme theme = getTheme("Polymesh");
        Assertions.assertNotNull(theme.getColorSets());
    }

    @Test
    void testThemeNoButtonMargin() throws IOException {
        UITheme theme = getTheme("Polymesh");
        Assertions.assertEquals(0, theme.getButtonMargin());
    }

    @Test
    void testThemeNoPaletteMargin() throws IOException {
        UITheme theme = getTheme("Polymesh");
        Assertions.assertEquals(0, theme.getPaletteMargin());
    }

    @Test
    void testThemeButtonClass() throws IOException {
        UITheme theme = getTheme("Theme1");
        theme.getButton().getButtonClass();
        Assertions.assertEquals("artofillusion.ui.DefaultToolButton", theme.getButton().getButtonClass());
    }

    @Test
    void testThemeButtonGetStyles() throws IOException {
        UITheme theme = getTheme("Theme1");
        Assertions.assertEquals(2, theme.getButton().getStyles().size());
        Assertions.assertEquals("artofillusion.view.ViewerControl", theme.getButton().getStyles().get(1).getOwner());
    }

    @Test
    void testThemeButtonGetSizeAttribute() throws IOException {
        UITheme theme = getTheme("Theme1");
        Assertions.assertEquals("22,22", theme.getButton().getStyles().get(1).getAttributes().get("size"));
    }

    private UITheme getTheme(String path) throws IOException {
        try {
            return (UITheme) xstream.fromXML(UITheme.class.getResource("/artofillusion/theme/" + path + "/theme.xml").openStream());
        } catch (IIOException e) {
            throw new IOException("Could not load theme " + path, e);
        }
    }

    @Test
    void testSplit() {
        String[] s1 = "22,22".split(",");
        String[] s2 = "22 , 22".split(",");
        String[] s3 = "22".split(",");
    }
}
