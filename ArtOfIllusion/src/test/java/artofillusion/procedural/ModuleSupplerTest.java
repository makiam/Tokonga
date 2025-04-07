package artofillusion.procedural;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ModuleSupplerTest {

    @Test
    public void testModuleSupplier() {
        CoordinateModule cm = new CoordinateModule();

        ModuleSuppler<CoordinateModule> cms = new ModuleSuppler<>(CoordinateModule.class, it ->
            it.setCoordinate(CoordinateModule.X));
        var neww = cms.get();
        Assertions.assertNotEquals(cm, neww);
        Assertions.assertEquals(neww.coordinate, CoordinateModule.X);
    }

}
