/* Copyright (C) 2023-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.script.ScriptedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.junit.jupiter.api.DisplayName;


@DisplayName("Object Properties Test")
class ObjectPropertiesTest {

    @Test
    @DisplayName("Test Get Object 3 D Properties")
    void testGetObject3DProperties() {
        var ph = Mockito.mock(Object3D.class, Answers.CALLS_REAL_METHODS);
        var props = ph.getProperties();
        Assertions.assertNotNull(props);
        Assertions.assertEquals(0, props.length);
    }

    @Test
    @DisplayName("Test Get Cube Properties Test")
    void testGetCubePropertiesTest() {
        var ph = Mockito.mock(Cube.class, Answers.CALLS_REAL_METHODS);
        var props = ph.getProperties();
        Assertions.assertNotNull(props);
        Assertions.assertEquals(3, props.length);
    }

    @Test
    @DisplayName("Test Get Curve Properties")
    void testGetCurveProperties() {
        var ph = Mockito.mock(Curve.class, Answers.CALLS_REAL_METHODS);
        var props = ph.getProperties();
        Assertions.assertNotNull(props);
        Assertions.assertEquals(2, props.length);
    }

    @Test
    @DisplayName("Test Get Scripted Object Object Properties")
    void testGetScriptedObjectObjectProperties() {
        ScriptedObject ph = new ScriptedObject("");
        var props = ph.getProperties();
        Assertions.assertNotNull(props);
        Assertions.assertEquals(ph.getNumParameters(), props.length);
    }
}
