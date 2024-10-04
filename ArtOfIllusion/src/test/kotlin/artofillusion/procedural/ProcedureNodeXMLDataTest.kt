package artofillusion.procedural

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.StaxDriver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.IOException


class ProcedureNodeXMLDataTest {

    @Test
    @Throws(IOException::class)
    fun resultFromNestedNode() {
        val result = xstream.fromXML(
            ProcedureNodeXMLDataTest::class.java.getResource("/artofillusion/procedural/Node1.xml").openStream()
        ) as NodeDef
        Assertions.assertEquals("Node As Nested Node", result.getName())
    }

    @Test
    @Throws(IOException::class)
    fun resultFromAttribute() {
        val result = xstream.fromXML(
            ProcedureNodeXMLDataTest::class.java.getResource("/artofillusion/procedural/Node2.xml").openStream()
        ) as NodeDef
        Assertions.assertEquals("Node As Attribute", result.getName())
    }

    companion object {
        var xstream: XStream = XStream(StaxDriver())

        @BeforeAll
        @JvmStatic
        fun setupAll() {
            xstream.ignoreUnknownElements()
            xstream.useAttributeFor(NodeDef::class.java, "name")
            xstream.allowTypes(arrayOf<Class<*>>(NodeDef::class.java))
            xstream.processAnnotations(arrayOf<Class<*>>(NodeDef::class.java))
        }

    }

}
