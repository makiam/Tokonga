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