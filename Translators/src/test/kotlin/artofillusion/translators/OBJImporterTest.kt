package artofillusion.translators

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class OBJImporterTest {
    @Test
    fun `break Vertex Line` () {
        val line = "v 123.0 12.12 -0.5 44 # Rest of line"
        val res = OBJImporter.breakLine(line)
        Assertions.assertEquals(5, res.size)
    }
}