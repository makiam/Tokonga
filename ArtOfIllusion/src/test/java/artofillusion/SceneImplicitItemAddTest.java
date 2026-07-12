/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.image.DummyImage;
import artofillusion.image.ImageMap;
import artofillusion.image.ImageOrColor;
import artofillusion.material.Material;
import artofillusion.material.UniformMaterial;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.RGBColor;
import artofillusion.object.Cube;
import artofillusion.object.ObjectInfo;
import artofillusion.texture.ImageMapTexture;
import artofillusion.texture.Texture;
import artofillusion.texture.UniformTexture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
/*
 *  NB. Current implementation does not add material/texture/image implicitly
 */
class SceneImplicitItemAddTest {

    private Scene scene;

    @BeforeEach
    void setUp() {
        scene = new Scene();
    }


    @Test
    void testAddSceneItemWithNewAssignedMaterial() {
        ObjectInfo si = new ObjectInfo(new Cube(), new CoordinateSystem(), "This is The Cube");
        Material mat = new UniformMaterial();
        mat.setName("New Material");

        var mm = mat.getDefaultMapping(si.getObject());
        Assertions.assertEquals(0, scene.getNumObjects());
        Assertions.assertEquals(0, scene.getMaterials().size());

        si.getGeometry().setMaterial(mat, mm);
        scene.addObject(si, null);

        Assertions.assertEquals(1, scene.getNumObjects());
        Assertions.assertEquals(0, scene.getMaterials().size());
    }

    @Test
    void testAddSceneItemWithNewAssignedTexture() {
        ObjectInfo si = new ObjectInfo(new Cube(), new CoordinateSystem(), "This is The Cube");
        Texture tex = new UniformTexture();
        tex.setName("New Texture");

        var mm = tex.getDefaultMapping(si.getObject());
        Assertions.assertEquals(0, scene.getNumObjects());
        Assertions.assertEquals(1, scene.getTextures().size());

        si.getGeometry().setTexture(tex, mm);
        scene.addObject(si, null);

        Assertions.assertEquals(1, scene.getNumObjects());
        Assertions.assertEquals(1, scene.getTextures().size());
    }

    @Test
    void testAddSceneItemWithNewAssignedTextureWithImage() {
        ObjectInfo si = new ObjectInfo(new Cube(), new CoordinateSystem(), "This is The Cube");
        var tex = new ImageMapTexture();
        tex.setName("New Texture");

        ImageMap im = new DummyImage();
        tex.specularColor = new ImageOrColor(new RGBColor(0, 0, 0), im);

        var mm = tex.getDefaultMapping(si.getObject());
        Assertions.assertEquals(0, scene.getNumObjects());
        Assertions.assertEquals(1, scene.getTextures().size());
        Assertions.assertEquals(0, scene.getNumImages());

        si.getGeometry().setTexture(tex, mm);
        scene.addObject(si, null);

        Assertions.assertEquals(1, scene.getNumObjects());
        Assertions.assertEquals(1, scene.getTextures().size());
        Assertions.assertEquals(0, scene.getNumImages());
    }
}
