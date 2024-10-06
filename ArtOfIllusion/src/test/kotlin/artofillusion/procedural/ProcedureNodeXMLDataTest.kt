/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

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
