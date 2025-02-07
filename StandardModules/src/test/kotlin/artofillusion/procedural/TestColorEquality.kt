package artofillusion.procedural

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Point

class TestColorEquality {
    @Test
    fun testNoGivenInputs() {
        val module = ColorEqualityModule(Point(0,0))

        Assertions.assertEquals(0.0, module.getAverageValue(0, 0.0), 0.0);
    }

    @Test
    fun testAttachOnlyOne() {
        val module = ColorEqualityModule(Point(0,0))

        Assertions.assertEquals(0.0, module.getAverageValue(0, 0.0), 0.0);
    }
}
