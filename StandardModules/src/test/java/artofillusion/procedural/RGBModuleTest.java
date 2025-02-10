package artofillusion.procedural;

import artofillusion.math.RGBColor;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RGBModuleTest {

    @Test
    void testNoGivenColorInputs() {
        var mod = new RGBModule(new Point(0,0));
        var color = new RGBColor(1,1,1);
        mod.getColor(0, color, 0.0);

        assertAll(() -> assertEquals(0.0, color.getRed()),
                  () -> assertEquals(0.0, color.getGreen()),
                  () -> assertEquals(0.0, color.getBlue()));
    }


    @Test
    void testGetOneChannelColor() {
        var mod = new RGBModule(new Point(0,0));
        var color = new RGBColor(1,1,1);
        var n1 = new NumberModule(new Point(0,0));
        n1.setValue(0.5);

        mod.linkFrom[0] = n1;
        mod.getColor(0, color, 0.0);
        assertAll(() -> assertEquals(0.5, color.getRed()),
                () -> assertEquals(0.0, color.getGreen()),
                () -> assertEquals(0.0, color.getBlue()));
    }


    @Test
    void testAllChannelsSet() {
        var mod = new RGBModule(new Point(0,0));
        var color = new RGBColor(1,1,1);
        var c1 = new NumberModule(new Point(0,0));
        c1.setValue(0.1);
        var c2 = new NumberModule(new Point(0,0));
        c2.setValue(0.5);
        var c3 = new NumberModule(new Point(0,0));
        c3.setValue(0.75);

        mod.linkFrom[0] = c1;
        mod.linkFrom[1] = c2;
        mod.linkFrom[2] = c3;
        mod.getColor(0, color, 0.0);
        assertAll(() -> assertEquals(0.1, color.getRed(), 0.00000001),
                () -> assertEquals(0.5, color.getGreen(), 0.00000001),
                () -> assertEquals(0.75, color.getBlue(), 0.00000001));
    }

    @Test
    void testColorModule() {
        var mod = new ColorModule(new Point(0,0));
    }
}
