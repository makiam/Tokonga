package artofillusion;

import artofillusion.material.Material;
import artofillusion.material.MaterialMapping;
import artofillusion.object.ObjectInfo;

import java.util.HashMap;
import java.util.Map;

public class RemMatAc implements UndoableEdit {
    private int which;
    private Scene scene;

    private Map<ObjectInfo, MaterialMapping> matMap = new HashMap<>();
    private final Material material;

    RemMatAc(Scene scene, int which) {
        this.scene = scene;
        this.which = which;
        this.material = scene.getMaterial(which);
    }

    @Override
    public void undo() {

    }

    @Override
    public void redo() {
        scene.removeMaterial(which);
    }
}
