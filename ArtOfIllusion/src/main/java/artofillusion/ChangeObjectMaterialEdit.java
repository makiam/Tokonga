package artofillusion;

import artofillusion.material.Material;
import artofillusion.material.MaterialMapping;
import artofillusion.object.ObjectInfo;

public final class ChangeObjectMaterialEdit implements UndoableEdit {

    private final ObjectInfo target;
    private final Material oldMaterial;
    private final Material material;
    private final MaterialMapping oldMapping;

    public ChangeObjectMaterialEdit(ObjectInfo target, Material material) {
        this.target =  target;
        this.oldMaterial = target.getObject().getMaterial();
        this.oldMapping = target.getObject().getMaterialMapping();
        this.material = material;

    }

    @Override
    public void undo() {
        target.setMaterial(oldMaterial, oldMapping);
    }


    @Override
    public void redo() {
        var mm = material == null ? null : material.getDefaultMapping(target.getGeometry());
        target.setMaterial(material, mm);
    }


    @Override
    public String getName() {
        return "Change Object(s) material";
    }
}
