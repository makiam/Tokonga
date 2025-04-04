package artofillusion.procedural;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleSupplerTest {

    @Test
    public void testModuleSupplier() {
        CoordinateModule cm = new CoordinateModule();
        ModuleSuppler<CoordinateModule> cms = new ModuleSuppler<>(cm, it -> {
            it.setCoordinate(CoordinateModule.X);
        });
    }

}
