package artofillusion;

import artofillusion.material.Material;

import java.util.List;

interface MaterialsContainer {
    /*
    Get all materials from scene as unmodifiable List
     */
    List<Material> getMaterials();
    void addMaterial(Material material);
}
