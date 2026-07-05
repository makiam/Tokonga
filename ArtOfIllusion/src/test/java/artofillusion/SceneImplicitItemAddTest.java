package artofillusion;

import artofillusion.material.Material;
import artofillusion.material.MaterialMapping;
import artofillusion.material.UniformMaterial;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.ObjectInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SceneImplicitItemAddTest {

    private Scene scene;

    @BeforeEach
    public void setUp() {
        scene = new Scene();
    }


    @Test
    void testAddSceneItemWithNewAssignedMaterial() {
        ObjectInfo si = new ObjectInfo(new Cube(), new CoordinateSystem(), "This is The Cube");
        Material mat = new UniformMaterial();
        mat.setName("New Material");
        MaterialMapping mm = mat.getDefaultMapping(si.getObject());
        Assertions.assertEquals(0, scene.getNumObjects());
        Assertions.assertEquals(0, scene.getMaterials().size());

        si.getGeometry().setMaterial(mat, mm);
        scene.addObject(si, null);

        Assertions.assertEquals(1, scene.getNumObjects());
        Assertions.assertEquals(1, scene.getMaterials().size());
    }
}
