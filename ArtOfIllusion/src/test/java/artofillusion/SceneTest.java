/* Copyright (C) 2016-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.image.ImageMap;
import artofillusion.image.ImageOrColor;
import artofillusion.image.MIPMappedImage;
import artofillusion.material.Material;
import artofillusion.material.ProceduralMaterial3D;
import artofillusion.material.UniformMaterial;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.RGBColor;
import artofillusion.math.Vec3;
import artofillusion.object.Cube;
import artofillusion.object.NullObject;
import artofillusion.object.ObjectInfo;
import artofillusion.object.SceneCamera;
import artofillusion.object.Sphere;
import artofillusion.object.SpotLight;
import artofillusion.texture.ImageMapTexture;
import artofillusion.texture.Texture;
import artofillusion.texture.UniformTexture;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.Before;
import org.mockito.Mockito;

/**
 *
 * @author MaksK
 */
public class SceneTest {
    
    private static final ApplicationPreferences preferences = Mockito.mock(ApplicationPreferences.class);
    
    private int listenerFireCount = 0;
    private int firedPositionIndex = 0;

    private Scene scene;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        Mockito.when(preferences.getLocale()).thenReturn(Locale.ENGLISH);
        Mockito.when(preferences.getUseOpenGL()).thenReturn(false);
        Mockito.when(preferences.getInteractiveSurfaceError()).thenReturn(0.01);
        Mockito.when(preferences.getShowTravelCuesOnIdle()).thenReturn(false);

        Field pf = ArtOfIllusion.class.getDeclaredField("preferences");
        pf.setAccessible(true);
        pf.set(null, preferences);
        pf.setAccessible(false);        
    }
    
    @Before
    public void setUp() {
        scene = new Scene();
    }

    /**
     * Create scene and smoke check some scene defaults created
     */
    @Test
    public void testCreateScene() {

        Assert.assertNotNull(scene);
        Assert.assertNotNull(scene.getAllMetadataNames());

        Assert.assertEquals(1, scene.getNumTextures());
        Assert.assertNotNull(scene.getDefaultTexture());
        Assert.assertNotNull(scene.getEnvironmentTexture());

        Assert.assertEquals(0, scene.getNumMaterials());
        Assert.assertEquals(0, scene.getNumImages());

    }

    /**
     * Test to add to scene new object
     * Check that object created, added to scene and contains default tracks...
     */
    @Test
    public void testAddObjectAsNewObject3DAndCoordinates() {

        int sceneObjects = scene.getNumObjects();

        scene.addObject(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube", (UndoRecord) null);
        ObjectInfo so = scene.getObject("Cube");

        Assert.assertEquals(++sceneObjects, scene.getNumObjects());

        Assert.assertEquals(2, so.getTracks().length);

    }

    /**
     * Test to add to scene new object other way
     * Check that object created, added to scene and contains default tracks...
     */
    @Test
    public void testAddObjectAsNewObjectInfo() {

        int sceneObjects = scene.getNumObjects();
        scene.addObject(new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube"), (UndoRecord) null);
        ObjectInfo so = scene.getObject("Cube");

        Assert.assertEquals(++sceneObjects, scene.getNumObjects());

        Assert.assertEquals(2, so.getTracks().length);

    }

    /**
     * Test to add to scene new object other way
     * Check that object created, added to scene and contains default tracks...
     */
    @Test
    public void testAddObjectAsNewObjectInfoToGovenPos() {

        int sceneObjects = scene.getNumObjects();
        scene.addObject(new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube"), scene.getNumObjects(), (UndoRecord) null);
        ObjectInfo so = scene.getObject("Cube");

        Assert.assertEquals(++sceneObjects, scene.getNumObjects());

        Assert.assertEquals(2, so.getTracks().length);

    }

    /**
     * Test to add to scene new object with Undo info
     * Check that Undo record contains proper data to revert operation
     */
    @Test
    public void testAddObjectWithUndo() {

        UndoRecord ur = new UndoRecord(null, false);

        int sceneObjects = scene.getNumObjects();

        scene.addObject(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube", ur);
        Assert.assertEquals(++sceneObjects, scene.getNumObjects());
        Assert.assertNotNull(scene);
        Assert.assertNotNull(ur.getCommands());
        Assert.assertEquals(1, ur.getCommands().size());
        Assert.assertTrue(ur.getCommands().get(0) == UndoRecord.DELETE_OBJECT);

    }

    /**
     * Add material to scene.
     * Check that material listener event is triggered
     */
    @Test
    public void testAddMaterial() {

        Material mat = new UniformMaterial();
        listenerFireCount = 0;

        scene.addMaterialListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
                listenerFireCount++;
            }

            @Override
            public void itemRemoved(int index, Object obj) {
            }

            @Override
            public void itemChanged(int index, Object obj) {
            }
        });
        scene.addMaterial(mat);
        Assert.assertEquals(1, listenerFireCount);
    }

    /**
     * Add material to Scene at given position.
     * Check that material listener event is triggered
     * Check that material is inserted at expected position
     */
    @Ignore
    @Test
    public void testAddMaterialAtGivenPos() {

        Material mat = new UniformMaterial();
        listenerFireCount = 0;
        firedPositionIndex = 0;

        scene.addMaterialListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
                listenerFireCount++;
                firedPositionIndex = index;
            }

            @Override
            public void itemRemoved(int index, Object obj) {
            }

            @Override
            public void itemChanged(int index, Object obj) {
            }
        });

        scene.addMaterial(mat, 0);
        scene.addMaterial(mat, 0);
        Assert.assertEquals(2, listenerFireCount);
        Assert.assertEquals(0, firedPositionIndex);
    }

    /**
     * Test to remove unused material from scene
     * Check proper materials count
     * Check that listener event is fired
     */
    @Test
    public void testRemoveUnassignedMatrial() {

        Material mat = new UniformMaterial();
        scene.addMaterial(mat);

        scene.addMaterialListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
            }

            @Override
            public void itemRemoved(int index, Object obj) {
                listenerFireCount++;
            }

            @Override
            public void itemChanged(int index, Object obj) {
            }
        });

        scene.removeMaterial(0);
        Assert.assertEquals(0, scene.getNumMaterials());
        Assert.assertEquals(1, listenerFireCount);
    }

    /**
     * Test to remove assigned to object material from scene
     * Check proper materials count
     * Check that listener event is fired
     * Check that material and mapping is unassigned from target object
     */
    @Test
    public void testRemoveAssignedMaterial() {

        Material mat = new UniformMaterial();
        scene.addMaterial(mat);
        ObjectInfo target = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        target.setMaterial(mat, mat.getDefaultMapping(target.getObject()));
        scene.addObject(target, (UndoRecord) null);

        scene.addMaterialListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
            }

            @Override
            public void itemRemoved(int index, Object obj) {
                listenerFireCount++;
            }

            @Override
            public void itemChanged(int index, Object obj) {
            }
        });

        scene.removeMaterial(0);

        Assert.assertEquals(0, scene.getMaterials().size());
        Assert.assertEquals(1, listenerFireCount);

        Assert.assertNull(target.getObject().getMaterial());
        Assert.assertNull(target.getObject().getMaterialMapping());

    }

    /**
     * Test to check itemChanged event is fired on material change
     */
    @Test
    public void testChangeMaterialEventFired() {

        Material mat = new UniformMaterial();
        scene.addMaterial(mat);
        listenerFireCount = 0;

        scene.addMaterialListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
            }

            @Override
            public void itemRemoved(int index, Object obj) {
            }

            @Override
            public void itemChanged(int index, Object obj) {
                listenerFireCount++;
            }
        });

        scene.changeMaterial(0);
        Assert.assertEquals(1, listenerFireCount);

    }

    /**
     * Test to get added material by name
     */
    @Test
    public void testGetMaterialByName() {

        Material mat = new UniformMaterial();
        mat.setName("Test");
        scene.addMaterial(mat);
        mat = null;
        mat = scene.getMaterial("Test");
        Assert.assertNotNull(mat);
    }

    /**
     * Test not to get material by wrong name
     */
    @Test
    public void testGetUnknownMaterialByName() {
        Material mat = scene.getMaterial("Missing");
        Assert.assertNull(mat);
    }

    /**
     * Test not to get material by null name
     */
    @Test
    public void testGetMaterialByNullName() {
        Material mat = scene.getMaterial(null);
        Assert.assertNull(mat);
    }

    /**
     * Add texture to scene
     * Check that texture listener event is triggered
     */
    @Test
    public void testAddTextureEventFired() {

        Texture tex = new UniformTexture();
        listenerFireCount = 0;

        scene.addTextureListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object tex) {
                listenerFireCount++;
            }

            @Override
            public void itemRemoved(int index, Object tex) {
            }

            @Override
            public void itemChanged(int index, Object tex) {
            }
        });

        scene.addTexture(tex);
        Assert.assertEquals(1, listenerFireCount);
        Assert.assertEquals(2, scene.getTextures().size());
    }

    /**
     * Add texture to Scene at given position.
     * Check that texture listener event is triggered
     * Check that texture is inserted at expected position
     */
    @Ignore
    @Test
    public void testAddTextureAtGivenPos() {

        Texture tex = new UniformTexture();
        listenerFireCount = 0;
        firedPositionIndex = -1;

        scene.addTextureListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
                firedPositionIndex = index;
            }

            @Override
            public void itemRemoved(int index, Object obj) {
            }

            @Override
            public void itemChanged(int index, Object obj) {
            }
        });

        scene.addTexture(tex, 0);
        Assert.assertEquals(0, firedPositionIndex);
    }

    /**
     * Test checks that itemChanged event fired on default texture change
     */
    @Test
    public void testChangeDefaultTexture() {
        listenerFireCount = 0;
        firedPositionIndex = -1;

        scene.addTextureListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
            }

            @Override
            public void itemRemoved(int index, Object obj) {
            }

            @Override
            public void itemChanged(int index, Object obj) {
                listenerFireCount++;
                firedPositionIndex = index;
            }
        });
        scene.changeTexture(0);

        Assert.assertEquals(0, firedPositionIndex);
        Assert.assertEquals(1, listenerFireCount);
    }

    /**
     * Test to get added texture by name
     */
    @Test
    public void testGetTextureByName() {
        Texture tex = new UniformTexture();
        tex.setName("Test");
        scene.addTexture(tex);
        tex = null;
        tex = scene.getTexture("Test");
        Assert.assertNotNull(tex);

    }

    /**
     * Test to not return texture by wrong name
     */
    @Test
    public void testGetUnknownTextureByName() {
        Texture tex = scene.getTexture("Missing");
        Assert.assertNull(tex);

    }

    /**
     * Test to not return texture by null name
     */
    @Test
    public void testGetTextureByNullName() {
        Texture tex = scene.getTexture(null);
        Assert.assertNull(tex);

    }

    /**
     * Test to remove single and one default texture from scene
     * Check that itemRemoved event is fired
     * Check that default texture is reconstructed for scene
     */
    @Test
    public void testRemoveSingleDefaultTexture() {
        scene.addTextureListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
                listenerFireCount++;
            }

            @Override
            public void itemRemoved(int index, Object obj) {
                listenerFireCount++;
            }

            @Override
            public void itemChanged(int index, Object obj) {
            }
        });
        scene.removeTexture(0);
        Assert.assertEquals(2, listenerFireCount);
        Assert.assertEquals(1, scene.getNumTextures());

        Assert.assertNotNull(scene.getDefaultTexture());

    }

    /**
     * Test to remove unused texture from scene
     * Check that itemRemoved event is fired
     */
    @Test
    public void testRemoveUnassignedTexture() {
        Texture tex = new UniformTexture();
        scene.addTexture(tex);

        scene.addTextureListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
            }

            @Override
            public void itemRemoved(int index, Object obj) {
                listenerFireCount++;
            }

            @Override
            public void itemChanged(int index, Object obj) {
            }
        });
        scene.removeTexture(1);
        Assert.assertEquals(1, listenerFireCount);
        Assert.assertEquals(1, scene.getNumTextures());

        Assert.assertNotNull(scene.getDefaultTexture());
    }

    /**
     *
     */
    @Test
    public void testRemoveAssignedTexture() {
        Texture tex = new UniformTexture();
        scene.addTexture(tex);

        ObjectInfo target = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        target.setTexture(tex, tex.getDefaultMapping(target.getObject()));
        scene.addObject(target, (UndoRecord) null);

        scene.addTextureListener(new ListChangeListener() {
            @Override
            public void itemAdded(int index, Object obj) {
            }

            @Override
            public void itemRemoved(int index, Object obj) {
                listenerFireCount++;
            }

            @Override
            public void itemChanged(int index, Object obj) {
            }
        });
        scene.removeTexture(1);

        Assert.assertEquals(1, listenerFireCount);
        Assert.assertEquals(1, scene.getNumTextures());

        Texture def = scene.getDefaultTexture();
        Assert.assertNotNull(def);
        Assert.assertEquals(def, target.getObject().getTexture());

    }

    /**
     *
     * Test adds new Image to scene
     *
     * @throws InterruptedException
     */
    @Test
    public void testAddImage() throws InterruptedException {
        int SIZE = 50;
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

        ImageMap map = new MIPMappedImage(im);
        scene.addImage(map);

        Assert.assertEquals(1, scene.getImages().size());
        Assert.assertEquals(0, scene.indexOf(map));

        Assert.assertEquals(map, scene.getImage(0));

    }

    /**
     * Test adds new Texture to scene. Texture uses some image which is not added to scene.
     * Test fails as image used by texture not added to scene implicitly
     *
     * @throws InterruptedException
     */
    @Test(expected = AssertionError.class)
    public void testAddImageFromTexture() throws InterruptedException {
        int SIZE = 50;

        ImageMapTexture tex = new ImageMapTexture();
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);
        ImageOrColor ioc = new ImageOrColor(new RGBColor(), map);
        tex.diffuseColor = ioc;
        scene.addTexture(tex);

        Assert.assertEquals(1, scene.getImages().size());
        Assert.assertEquals(0, scene.indexOf(map));

        Assert.assertEquals(map, scene.getImage(0));

    }

    /**
     * Test adds new Texture to scene. Texture uses some image already added to scene.
     * Test fails as image used by texture not added to scene implicitly
     *
     * @throws InterruptedException
     */
    @Test
    public void testAddImageBeforeTexture() throws InterruptedException {
        int SIZE = 50;

        ImageMapTexture tex = new ImageMapTexture();
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);
        ImageOrColor ioc = new ImageOrColor(new RGBColor(), map);
        tex.diffuseColor = ioc;
        scene.addImage(map);
        scene.addTexture(tex);

        Assert.assertEquals(1, scene.getImages().size());
        Assert.assertEquals(0, scene.indexOf(map));

        Assert.assertEquals(map, scene.getImage(0));

    }

    /**
     * Test to remove image from scene
     *
     * @throws InterruptedException
     */
    @Test
    public void testRemoveImage() throws InterruptedException {
        int SIZE = 50;

        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

        ImageMap map = new MIPMappedImage(im);
        scene.addImage(map);
        boolean removed = scene.removeImage(0);

        Assert.assertTrue(removed);
        Assert.assertEquals(0, scene.getNumImages());

    }

    /**
     * Test to remove image used through texture from scene
     * Checks that image deletion is rejected
     *
     * @throws InterruptedException
     */
    @Test
    public void testAttemptRemoveImageUsedInTexture() throws InterruptedException {
        int SIZE = 50;

        ImageMapTexture tex = new ImageMapTexture();
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);
        ImageOrColor ioc = new ImageOrColor(new RGBColor(), map);
        tex.diffuseColor = ioc;
        scene.addImage(map);
        scene.addTexture(tex);

        boolean result = scene.removeImage(0);
        Assert.assertFalse(result);

    }

    /**
     * Test to remove image used through material from scene
     * Checks that image deletion is rejected
     * As no way to add image to Procedural Material as Image module programmatically test fails
     *
     * @throws InterruptedException
     */
    @Ignore
    @Test
    public void testAttemptRemoveImageUsedInMaterial() throws InterruptedException {
        int SIZE = 50;

        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);

        ProceduralMaterial3D pm = new ProceduralMaterial3D();
        scene.addMaterial(pm);
        Assert.fail("No way to add image to material programmatically");
    }

    /**
     * Test to check scene objectModified(...) code.
     * Failed as not initialized AOI preferences system @ test time. Code is depends of interactive surface error value
     */
    @Test
    public void testSceneObjectModified() {
        ObjectInfo target = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        scene.addObject(target, (UndoRecord) null);
        RenderingMesh rm = target.getPreviewMesh();
        Assert.assertNotNull(rm);
        scene.objectModified(target.getObject());

        Assert.assertNull(target.getPose());
        rm = target.getPreviewMesh();
        Assert.assertNotNull(rm);
        Assert.assertTrue(rm instanceof RenderingMesh);

    }

    @Test
    public void testGetSceneHasNoCamerasList() {

        List<ObjectInfo> cameras = scene.getCameras();
        Assert.assertNotNull(cameras);
        Assert.assertTrue(cameras.isEmpty());
    }

    @Test
    public void testGetSceneHasSingleCameraOnly() {

        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new SceneCamera(), coords, "Camera 1");

        scene.addObject(info, null);

        List<ObjectInfo> cameras = scene.getCameras();
        Assert.assertNotNull(cameras);
        Assert.assertEquals(1, cameras.size());
        Assert.assertTrue(cameras.get(0).getObject() instanceof SceneCamera);
    }

    @Test
    public void testGetSceneHasCamerasOnly() {

        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new SceneCamera(), coords, "Camera 1");

        scene.addObject(info, null);

        info = new ObjectInfo(new SceneCamera(), coords, "Camera 2");

        scene.addObject(info, null);

        List<ObjectInfo> cameras = scene.getCameras();
        Assert.assertNotNull(cameras);
        Assert.assertEquals(2, cameras.size());
        for (ObjectInfo cameraObj : cameras) {
            Assert.assertTrue(cameraObj.getObject() instanceof SceneCamera);
        }

    }

    @Test
    public void testSceneHasCameraAndOther() {

        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new NullObject(), coords, "Null Object");

        scene.addObject(info, null);
        info = new ObjectInfo(new Sphere(1.0, 1.0, 1.0), coords, "Sphere 1");
        scene.addObject(info, null);

        info = new ObjectInfo(new SpotLight(new RGBColor(), 1.9f, 90.0, 5.0, 5.0), coords, "SpotLight 1");
        scene.addObject(info, null);

        info = new ObjectInfo(new SceneCamera(), coords, "Camera 1");
        scene.addObject(info, null);

        Assert.assertTrue(scene.getNumObjects() == 4);
        List<ObjectInfo> cameras = scene.getCameras();
        Assert.assertNotNull(cameras);
        Assert.assertEquals(1, cameras.size());
        Assert.assertTrue(cameras.get(0).getObject() instanceof SceneCamera);

    }

    @Test
    public void sceneGetSingleAddedObjectIndex() {
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new NullObject(), coords, "Null Object");

        scene.addObject(info, null);

        Assert.assertEquals(0, scene.indexOf(info));

    }

    @Test
    public void sceneGetMissedObjectIndex() {
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new NullObject(), coords, "Null Object");

        scene.addObject(info, null);

        ObjectInfo missed = new ObjectInfo(new Sphere(1.0, 1.0, 1.0), coords, "Not added to Scene object");
        Assert.assertEquals(-1, scene.indexOf(missed));

    }

}
