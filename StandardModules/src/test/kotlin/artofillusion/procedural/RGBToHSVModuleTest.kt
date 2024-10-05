
import artofillusion.procedural.RGBToHSVModule
import org.junit.jupiter.api.Assertions

import org.junit.jupiter.api.Test
import java.awt.Point

class RGBToHSVModuleTest {

    @Test
    fun testRGBToHSVNoGivenInputs() {
        val module = RGBToHSVModule(Point(0, 0))
        Assertions.assertEquals(0.0, module.getAverageValue(0, 0.0), 0.0)
        Assertions.assertEquals(0.0, module.getAverageValue(1, 0.0), 0.0)
        Assertions.assertEquals(0.0, module.getAverageValue(2, 0.0), 0.0)
    }

    @Test
    fun testRGBWhite() {
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 1.0, 0))
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 1.0, 1))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 1.0, 2))
    }

    @Test
    fun testRGBRed() {
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 0.0, 0))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 0.0, 1))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 0.0, 2))
    }

    @Test
    fun testRGBGreen() {
        Assertions.assertEquals(0.3333333333333333, RGBToHSVModule.rgbToHsv(0.0, 1.0, 0.0, 0))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 1.0, 0.0, 1))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 1.0, 0.0, 2))
    }

    @Test
    fun testRGBBlue() {
        Assertions.assertEquals(0.6666666666666666, RGBToHSVModule.rgbToHsv(0.0, 0.0, 1.0, 0))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 0.0, 1.0, 1))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 0.0, 1.0, 2))
    }

    @Test
    fun testRGBYellow() {
        Assertions.assertEquals(0.16666666666666666, RGBToHSVModule.rgbToHsv(1.0, 1.0, 0.0, 0))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 0.0, 1))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 1.0, 0.0, 2))
    }

    @Test
    fun testRGBMagenta() {
        Assertions.assertEquals(0.8333333333333334, RGBToHSVModule.rgbToHsv(1.0, 0.0, 1.0, 0))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 1.0, 1))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(1.0, 0.0, 1.0, 2))
    }

    @Test
    fun testRGBPurple() {
        Assertions.assertEquals(0.8333333333333334, RGBToHSVModule.rgbToHsv(0.5, 0.0, 0.5, 0))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.5, 0.0, 0.5, 1))
        Assertions.assertEquals(0.5, RGBToHSVModule.rgbToHsv(0.5, 0.0, 0.5, 2))
    }


    @Test
    fun testRGBCyan() {
        Assertions.assertEquals(0.5, RGBToHSVModule.rgbToHsv(0.0, 1.0, 1.0, 0))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 1.0, 1.0, 1))
        Assertions.assertEquals(1.0, RGBToHSVModule.rgbToHsv(0.0, 1.0, 1.0, 2))
    }

    @Test
    fun testRGBGray() {
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(0.5, 0.5, 0.5, 0))
        Assertions.assertEquals(0.0, RGBToHSVModule.rgbToHsv(0.5, 0.5, 0.5, 1))
        Assertions.assertEquals(0.5, RGBToHSVModule.rgbToHsv(0.5, 0.5, 0.5, 2))
    }
}