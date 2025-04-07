package artofillusion.procedural;

import artofillusion.test.util.RegisterTestResources;
import artofillusion.test.util.SetupLocale;
import artofillusion.test.util.SetupLookAndFeel;
import artofillusion.test.util.SetupTheme;
import artofillusion.ui.Translate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({SetupLocale.class})
class ModuleSupplerTest {

    @Test
    public void testModuleSupplier() {
        CoordinateModule cm = new CoordinateModule();

        ModuleSuppler<CoordinateModule> cms = new ModuleSuppler<>(CoordinateModule.class, it ->
            it.setCoordinate(CoordinateModule.Y));
        var neww = cms.get();
        Assertions.assertNotEquals(cm, neww);
        Assertions.assertEquals(CoordinateModule.Y, neww.coordinate);
        Assertions.assertEquals("Y", neww.getName());
    }

    @Test
    public void testDefaultModuleSupplier() {
        CoordinateModule cm = new CoordinateModule();

        ModuleSuppler<CoordinateModule> cms = new ModuleSuppler<>(CoordinateModule.class);
        var neww = cms.get();
        Assertions.assertNotEquals(cm, neww);
        Assertions.assertEquals(CoordinateModule.X, neww.coordinate);
        Assertions.assertEquals("X", neww.getName());
    }

    @Test
    public void testTimeCoordinateModuleSupplier() {
        CoordinateModule cm = new CoordinateModule();

        ModuleSuppler<CoordinateModule> cms = new ModuleSuppler<>(CoordinateModule.class, it ->
                it.setCoordinate(CoordinateModule.T));
        var neww = cms.get();
        Assertions.assertNotEquals(cm, neww);
        Assertions.assertEquals(CoordinateModule.T, neww.coordinate);
        Assertions.assertEquals(Translate.text("Time"), neww.getName());
    }
}
