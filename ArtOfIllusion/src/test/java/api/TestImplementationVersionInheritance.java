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
}

@ImplementationVersion
class BaseClass {

}

class DerivedClass extends BaseClass {

}