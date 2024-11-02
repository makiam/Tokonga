package artofillusion.material;

import artofillusion.RemMatAc;
import artofillusion.Scene;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MaterialActionTest {
    private Scene scene;
    @BeforeEach
    public void setUp() {
        scene = new Scene();
    }

    @Test
    @DisplayName("Test Remove Unused Material 1")
    void testRemoveUnusedMaterialOne() {
        Material mat = new UniformMaterial();
        scene.addMaterial(mat);

        RemMatAc action = new RemMatAc(scene, mat);
        action.execute();

        Assertions.assertEquals(0, scene.getNumMaterials());
    }

    @Test
    @DisplayName("Test Remove Unused Material 2")
    void testRemoveUnusedMaterialTwo() {
        Material mat = new UniformMaterial();
        scene.addMaterial(mat);

        RemMatAc action = new RemMatAc(scene, 0);
        action.execute();

        Assertions.assertEquals(0, scene.getNumMaterials());
    }
}
