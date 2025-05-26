/* Copyright 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package api;

import artofillusion.api.ImplementationVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestImplementationVersionInheritance {

    @Test
    public void testDerivedAnnotation() {
        var anno = DerivedClass.class.getAnnotation(ImplementationVersion.class);
        Assertions.assertEquals(0, anno.current());
    }

    @Test
    public void testDerivedFarAnnotation() {
        var anno = DerivedFar.class.getAnnotation(ImplementationVersion.class);
        Assertions.assertEquals(0, anno.current());
    }

    @Test
    public void testDerivedAndAnnotatedAnnotation() {
        var anno = DerivedAndAnnotated.class.getAnnotation(ImplementationVersion.class);
        Assertions.assertEquals(5, anno.current());
    }

    @Test
    public void testDerivedAnnotationFromInstance() {
        var anno = new DerivedClass().getClass().getAnnotation(ImplementationVersion.class);
        Assertions.assertEquals(0, anno.current());
    }
}

@ImplementationVersion
class BaseClass {

}

class DerivedClass extends BaseClass {

}

class DerivedFar extends DerivedClass {

}

@ImplementationVersion(current = 5)
class DerivedAndAnnotated extends DerivedClass {}