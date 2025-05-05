package artofillusion.translators

import org.junit.jupiter.api.Test

class TestReadPLY {

    @Test
    fun `First test` () {
        val input = TestReadPLY::class.java.getResource("/artofillusion/translators/cube.ply").openStream()
    }
}
