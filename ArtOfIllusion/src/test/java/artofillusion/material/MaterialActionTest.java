package artofillusion.material;


import artofillusion.Scene;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.ObjectInfo;
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

        scene.removeMaterial(0);

        Assertions.assertEquals(0, scene.getNumMaterials());
    }

    @Test
    @DisplayName("Test Remove Unused Material 2")
    void testRemoveUnusedMaterialTwo() {
        Material mat = new UniformMaterial();
        scene.addMaterial(mat);

        scene.removeMaterial(0);

        Assertions.assertEquals(0, scene.getNumMaterials());
    }

    @Test
    @DisplayName("Test Remove Material used in scene")
    void testRemoveUsedMaterial() {
        Material mat = new UniformMaterial();

        scene.addMaterial(mat);
        ObjectInfo so = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");

        scene.addObject(so, null);
        so.setMaterial(mat, mat.getDefaultMapping(so.getObject()));

        scene.removeMaterial(0);

        Assertions.assertEquals(0, scene.getNumMaterials());
        Assertions.assertNull(so.getGeometry().getMaterial());
        Assertions.assertNull(so.getGeometry().getMaterialMapping());
    }

    @Test
    @DisplayName("Test Remove Material not added to scene")
    void testRemoveUnexistedMaterial() {
        Material mat = new UniformMaterial();
        scene.removeMaterial(0);
        Assertions.assertEquals(0, scene.getNumMaterials());
    }

    @Test
    @DisplayName("Test Remove Material not added to scene")
    void testRemoveUnexistedMaterialByIndex() {
        Material mat = new UniformMaterial();
        scene.removeMaterial(0);
        Assertions.assertEquals(0, scene.getNumMaterials());
    }

}
