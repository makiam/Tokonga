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
                  () -> assertEquals(0,color.getBlue()));
    }
}
