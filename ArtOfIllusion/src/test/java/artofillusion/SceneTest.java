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

import org.junit.jupiter.api.*;

import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author MaksK
 */
@DisplayName("Scene Test")
class SceneTest {

    private static final ApplicationPreferences preferences = Mockito.mock(ApplicationPreferences.class);

    private int listenerFireCount = 0;

    private int firedPositionIndex = 0;

    private Scene scene;

    @BeforeAll
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

    @BeforeEach
    public void setUp() {
        scene = new Scene();
    }

    /**
     * Create scene and smoke check some scene defaults created
     */
    @Test
    @DisplayName("Test Create Scene")
    void testCreateScene() {
        Assertions.assertNotNull(scene);
        Assertions.assertNotNull(scene.getAllMetadataNames());
        Assertions.assertEquals(1, scene.getNumTextures());
        Assertions.assertNotNull(scene.getDefaultTexture());
        Assertions.assertNotNull(scene.getEnvironmentTexture());
        Assertions.assertEquals(0, scene.getNumMaterials());
        Assertions.assertEquals(0, scene.getNumImages());
    }

    /**
     * Test to add to scene new object
     * Check that object created, added to scene and contains default tracks...
     */
    @Test
    @DisplayName("Test Add Object As New Object 3 D And Coordinates")
    void testAddObjectAsNewObject3DAndCoordinates() {
        int sceneObjects = scene.getNumObjects();
        scene.addObject(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube", (UndoRecord) null);
        ObjectInfo so = scene.getObject("Cube");
        Assertions.assertEquals(++sceneObjects, scene.getNumObjects());
        Assertions.assertEquals(2, so.getTracks().length);
    }

    /**
     * Test to add to scene new object other way
     * Check that object created, added to scene and contains default tracks...
     */
    @Test
    @DisplayName("Test Add Object As New Object Info")
    void testAddObjectAsNewObjectInfo() {
        int sceneObjects = scene.getNumObjects();
        scene.addObject(new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube"), (UndoRecord) null);
        ObjectInfo so = scene.getObject("Cube");
        Assertions.assertEquals(++sceneObjects, scene.getNumObjects());
        Assertions.assertEquals(2, so.getTracks().length);
    }

    /**
     * Test to add to scene new object other way
     * Check that object created, added to scene and contains default tracks...
     */
    @Test
    @DisplayName("Test Add Object As New Object Info To Given Pos")
    void testAddObjectAsNewObjectInfoToGivenPos() {
        int sceneObjects = scene.getNumObjects();
        scene.addObject(new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube"), scene.getNumObjects(), (UndoRecord) null);
        ObjectInfo so = scene.getObject("Cube");
        Assertions.assertEquals(++sceneObjects, scene.getNumObjects());
        Assertions.assertEquals(2, so.getTracks().length);
    }

    /**
     * Test to add to scene new object with Undo info
     * Check that Undo record contains proper data to revert operation
     */
    @Test
    @DisplayName("Test Add Object With Undo")
    void testAddObjectWithUndo() {
        UndoRecord ur = new UndoRecord(null, false);
        int sceneObjects = scene.getNumObjects();
        scene.addObject(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube", ur);
        Assertions.assertEquals(++sceneObjects, scene.getNumObjects());
        Assertions.assertNotNull(scene);
        Assertions.assertNotNull(ur.getCommands());
        Assertions.assertEquals(1, ur.getCommands().size());
        Assertions.assertTrue(ur.getCommands().get(0) == UndoRecord.DELETE_OBJECT);
    }

    /**
     * Add material to the scene.
     * Check that material listener event is triggered and provided correct index
     */
    @Test
    @DisplayName("Test Add Material")
    void testAddMaterial() {

        listenerFireCount = 0;
        firedPositionIndex = -1;

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
        scene.addMaterial(new UniformMaterial());
        Assertions.assertEquals(1, listenerFireCount);
        Assertions.assertEquals(0, firedPositionIndex);

    }

    /**
     * Add material to the scene.
     * Check that material listener event is triggered and provided correct index
     */
    @Test
    public void testAddMoreMaterial() {

        listenerFireCount = 0;
        firedPositionIndex = -1;

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
        scene.addMaterial(new UniformMaterial());
        scene.addMaterial(new UniformMaterial());
        scene.addMaterial(new UniformMaterial());

        Assert.assertEquals(3, listenerFireCount);
        Assert.assertEquals(2, firedPositionIndex);

    }

    @Test
    public void testAddMaterialWithPolymorphicMethod() {
        Material mat = new UniformMaterial();
        listenerFireCount = 0;
        firedPositionIndex = -1;

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
        scene.add(mat);
        Assertions.assertEquals(1, listenerFireCount);
        Assertions.assertEquals(0, firedPositionIndex);
    }

    @Test
    public void testAddTextureWithPolymorphicMethod() {
        Texture tex = new UniformTexture();
        listenerFireCount = 0;
        firedPositionIndex = -1;

        scene.addTextureListener(new ListChangeListener() {
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
        scene.add(tex);
        Assertions.assertEquals(1, listenerFireCount);
        Assertions.assertEquals(0, firedPositionIndex);
    }

    /**
     * Add material to Scene at given position.
     * Check that material listener event is triggered
     * Check that material is inserted at expected position
     */
    @Test
    @DisplayName("Test Add Material At Given Pos")
    void testAddMaterialAtGivenPos() {
        Material mat = new UniformMaterial();
        listenerFireCount = 0;
        firedPositionIndex = -1;

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
        Assertions.assertEquals(2, listenerFireCount);
        Assertions.assertEquals(0, firedPositionIndex);
    }

    /**
     * Test to remove unused material from scene
     * Check proper materials count
     * Check that listener event is fired
     */
    @Test
    @DisplayName("Test Remove Unassigned Matrial")
    void testRemoveUnassignedMatrial() {
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
        Assertions.assertEquals(0, scene.getNumMaterials());
        Assertions.assertEquals(1, listenerFireCount);
    }

    /**
     * Test to remove assigned to object material from scene
     * Check proper materials count
     * Check that listener event is fired
     * Check that material and mapping is unassigned from target object
     */
    @Test
    @DisplayName("Test Remove Assigned Material")
    void testRemoveAssignedMaterial() {
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
        Assertions.assertEquals(0, scene.getNumMaterials());
        Assertions.assertEquals(1, listenerFireCount);
        Assertions.assertNull(target.getObject().getMaterial());
        Assertions.assertNull(target.getObject().getMaterialMapping());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMaterialsListIsImmutable() {
        Material mat = new UniformMaterial();
        scene.getMaterials().add(mat);
    }

    /**
     * Test to check itemChanged event is fired on material change
     */
    @Test
    @DisplayName("Test Change Material Event Fired")
    void testChangeMaterialEventFired() {
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
        Assertions.assertEquals(1, listenerFireCount);
        Assertions.assertEquals(1, listenerFireCount);

    }

    /**
     * Test to get added material by name
     */
    @Test
    @DisplayName("Test Get Material By Name")
    void testGetMaterialByName() {
        Material mat = new UniformMaterial();
        mat.setName("Test");
        scene.addMaterial(mat);
        mat = null;
        mat = scene.getMaterial("Test");
        Assertions.assertNotNull(mat);
    }

    /**
     * Test not to get material by wrong name
     */
    @Test
    @DisplayName("Test Get Unknown Material By Name")
    void testGetUnknownMaterialByName() {
        Material mat = scene.getMaterial("Missing");
        Assertions.assertNull(mat);
    }

    /**
     * Test not to get material by null name
     */
    @Test
    @DisplayName("Test Get Material By Null Name")
    void testGetMaterialByNullName() {
        Material mat = scene.getMaterial(null);
        Assertions.assertNull(mat);
    }

    /**
     * Add texture to scene
     * Check that texture listener event is triggered
     */
    @Test
    @DisplayName("Test Add Texture Event Fired")
    void testAddTextureEventFired() {
        Texture tex = new UniformTexture();
        listenerFireCount = 0;
        firedPositionIndex = -1;
        scene.addTextureListener(new ListChangeListener() {

            @Override
            public void itemAdded(int index, Object tex) {
                listenerFireCount++;
                firedPositionIndex = index;
            }

            @Override
            public void itemRemoved(int index, Object tex) {
            }

            @Override
            public void itemChanged(int index, Object tex) {
            }
        });
        scene.addTexture(tex);
        Assertions.assertEquals(1, listenerFireCount);
        Assertions.assertEquals(2, scene.getNumTextures());
        Assertions.assertEquals(0, firedPositionIndex);
    }

    /**
     * Add texture to Scene at given position.
     * Check that texture listener event is triggered
     * Check that texture is inserted at expected position
     */
    @Test
    @DisplayName("Test Add Texture At Given Pos")
    void testAddTextureAtGivenPos() {
        Texture tex = new UniformTexture();
        listenerFireCount = 0;
        firedPositionIndex = -1;
        scene.addTextureListener(new ListChangeListener() {

            @Override
            public void itemAdded(int index, Object obj) {
                listenerFireCount = 0;
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
        Assertions.assertEquals(0, firedPositionIndex);
    }

    /**
     * Test checks that itemChanged event fired on default texture change
     */
    @Test
    @DisplayName("Test Change Default Texture")
    void testChangeDefaultTexture() {
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
        Assertions.assertEquals(0, firedPositionIndex);
        Assertions.assertEquals(1, listenerFireCount);
    }

    /**
     * Test to get added texture by name
     */
    @Test
    @DisplayName("Test Get Texture By Name")
    void testGetTextureByName() {
        Texture tex = new UniformTexture();
        tex.setName("Test");
        scene.addTexture(tex);
        tex = null;
        tex = scene.getTexture("Test");
        Assertions.assertNotNull(tex);
    }

    /**
     * Test to not return texture by wrong name
     */
    @Test
    @DisplayName("Test Get Unknown Texture By Name")
    void testGetUnknownTextureByName() {
        Texture tex = scene.getTexture("Missing");
        Assertions.assertNull(tex);
    }

    /**
     * Test to not return texture by null name
     */
    @Test
    @DisplayName("Test Get Texture By Null Name")
    void testGetTextureByNullName() {
        Texture tex = scene.getTexture(null);
        Assertions.assertNull(tex);
    }

    /**
     * Test to remove single and one default texture from scene
     * Check that itemRemoved event is fired
     * Check that default texture is reconstructed for scene
     */
    @Test
    @DisplayName("Test Remove Single Default Texture")
    void testRemoveSingleDefaultTexture() {
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
        Assertions.assertEquals(2, listenerFireCount);
        Assertions.assertEquals(1, scene.getNumTextures());
        Assertions.assertNotNull(scene.getDefaultTexture());
    }

    /**
     * Test to remove unused texture from scene
     * Check that itemRemoved event is fired
     */
    @Test
    @DisplayName("Test Remove Unassigned Texture")
    void testRemoveUnassignedTexture() {
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
        Assertions.assertEquals(1, listenerFireCount);
        Assertions.assertEquals(1, scene.getNumTextures());
        Assertions.assertNotNull(scene.getDefaultTexture());
    }

    /**
     *
     */
    @Test
    @DisplayName("Test Remove Assigned Texture")
    void testRemoveAssignedTexture() {
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
        Assertions.assertEquals(1, listenerFireCount);
        Assertions.assertEquals(1, scene.getNumTextures());
        Texture def = scene.getDefaultTexture();
        Assertions.assertNotNull(def);
        Assertions.assertEquals(def, target.getObject().getTexture());
    }

    /**
     * Test adds new Image to scene
     *
     * @throws InterruptedException
     */
    @Test
    @DisplayName("Test Add Image")
    void testAddImage() throws InterruptedException {
        int SIZE = 50;
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);
        scene.addImage(map);
        Assertions.assertEquals(1, scene.getNumImages());
        Assertions.assertEquals(0, scene.indexOf(map));
        Assertions.assertEquals(map, scene.getImage(0));
    }

    @Test
    public void testAddImageWithPolymorphicMethod() throws InterruptedException {
        int SIZE = 50;
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

        ImageMap map = new MIPMappedImage(im);
        scene.add(map);

        Assert.assertEquals(1, scene.getImages().size());
        Assert.assertEquals(0, scene.indexOf(map));

        Assert.assertEquals(map, scene.getImage(0));
    }

    @Test
    public void testAddImageWithPolymorphicMethod() throws InterruptedException {
        int SIZE = 50;
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

        ImageMap map = new MIPMappedImage(im);
        scene.add(map);

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
    @Test
    @DisplayName("Test Add Image From Texture")
    void testAddImageFromTexture() {
        assertThrows(AssertionError.class, () -> {
            int SIZE = 50;
            ImageMapTexture tex = new ImageMapTexture();
            BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            ImageMap map = new MIPMappedImage(im);
            ImageOrColor ioc = new ImageOrColor(new RGBColor(), map);
            tex.diffuseColor = ioc;
            scene.addTexture(tex);
            Assertions.assertEquals(1, scene.getNumImages());
            Assertions.assertEquals(0, scene.indexOf(map));
            Assertions.assertEquals(map, scene.getImage(0));
        });
    }

    /**
     * Test adds new Texture to scene. Texture uses some image already added to scene.
     * Test fails as image used by texture not added to scene implicitly
     *
     * @throws InterruptedException
     */
    @Test
    @DisplayName("Test Add Image Before Texture")
    void testAddImageBeforeTexture() throws InterruptedException {
        int SIZE = 50;
        ImageMapTexture tex = new ImageMapTexture();
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);
        ImageOrColor ioc = new ImageOrColor(new RGBColor(), map);
        tex.diffuseColor = ioc;
        scene.addImage(map);
        scene.addTexture(tex);
        Assertions.assertEquals(1, scene.getNumImages());
        Assertions.assertEquals(0, scene.indexOf(map));
        Assertions.assertEquals(map, scene.getImage(0));
    }

    /**
     * Test to remove image from scene
     *
     * @throws InterruptedException
     */
    @Test
    @DisplayName("Test Remove Image")
    void testRemoveImage() throws InterruptedException {
        int SIZE = 50;
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);
        scene.addImage(map);
        boolean removed = scene.removeImage(0);
        Assertions.assertTrue(removed);
        Assertions.assertEquals(0, scene.getNumImages());
    }

    /**
     * Test to remove image used through texture from scene
     * Checks that image deletion is rejected
     *
     * @throws InterruptedException
     */
    @Test
    @DisplayName("Test Attempt Remove Image Used In Texture")
    void testAttemptRemoveImageUsedInTexture() throws InterruptedException {
        int SIZE = 50;
        ImageMapTexture tex = new ImageMapTexture();
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);
        ImageOrColor ioc = new ImageOrColor(new RGBColor(), map);
        tex.diffuseColor = ioc;
        scene.addImage(map);
        scene.addTexture(tex);
        boolean result = scene.removeImage(0);
        Assertions.assertFalse(result);
    }

    /**
     * Test to remove image used through material from scene
     * Checks that image deletion is rejected
     * As no way to add image to Procedural Material as Image module programmatically test fails
     *
     * @throws InterruptedException
     */
    @Disabled
    @Test
    @DisplayName("Test Attempt Remove Image Used In Material")
    void testAttemptRemoveImageUsedInMaterial() throws InterruptedException {
        int SIZE = 50;
        BufferedImage im = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        ImageMap map = new MIPMappedImage(im);
        ProceduralMaterial3D pm = new ProceduralMaterial3D();
        scene.addMaterial(pm);
        Assertions.fail("No way to add image to material programmatically");
    }

    /**
     * Test to check scene objectModified(...) code.
     */
    @Test
    @DisplayName("Test Scene Object Modified")
    void testSceneObjectModified() {
        ObjectInfo target = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        scene.addObject(target, (UndoRecord) null);
        RenderingMesh rm = target.getPreviewMesh();
        Assertions.assertNotNull(rm);
        scene.objectModified(target.getObject());
        Assertions.assertNull(target.getPose());
        Assertions.assertNotNull(target.getPreviewMesh());
    }

    @Test
    @DisplayName("Test Get Scene Has No Cameras List")
    void testGetSceneHasNoCamerasList() {
        List<ObjectInfo> cameras = scene.getCameras();
        Assertions.assertNotNull(cameras);
        Assertions.assertTrue(cameras.isEmpty());
    }

    @Test
    @DisplayName("Test Get Scene Has Single Camera Only")
    void testGetSceneHasSingleCameraOnly() {
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new SceneCamera(), coords, "Camera 1");
        scene.addObject(info, null);
        List<ObjectInfo> cameras = scene.getCameras();
        Assertions.assertNotNull(cameras);
        Assertions.assertEquals(1, cameras.size());
        Assertions.assertTrue(cameras.get(0).getObject() instanceof SceneCamera);
    }

    @Test
    @DisplayName("Test Get Scene Has Cameras Only")
    void testGetSceneHasCamerasOnly() {
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new SceneCamera(), coords, "Camera 1");
        scene.addObject(info, null);
        info = new ObjectInfo(new SceneCamera(), coords, "Camera 2");
        scene.addObject(info, null);
        List<ObjectInfo> cameras = scene.getCameras();
        Assertions.assertNotNull(cameras);
        Assertions.assertEquals(2, cameras.size());
        for (ObjectInfo cameraObj : cameras) {
            Assertions.assertTrue(cameraObj.getObject() instanceof SceneCamera);
        }
    }

    @Test
    @DisplayName("Test Scene Has Camera And Other")
    void testSceneHasCameraAndOther() {
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new NullObject(), coords, "Null Object");
        scene.addObject(info, null);
        info = new ObjectInfo(new Sphere(1.0, 1.0, 1.0), coords, "Sphere 1");
        scene.addObject(info, null);
        info = new ObjectInfo(new SpotLight(new RGBColor(), 1.9f, 90.0, 5.0, 5.0), coords, "SpotLight 1");
        scene.addObject(info, null);
        info = new ObjectInfo(new SceneCamera(), coords, "Camera 1");
        scene.addObject(info, null);
        Assertions.assertTrue(scene.getNumObjects() == 4);
        List<ObjectInfo> cameras = scene.getCameras();
        Assertions.assertNotNull(cameras);
        Assertions.assertEquals(1, cameras.size());
        Assertions.assertTrue(cameras.get(0).getObject() instanceof SceneCamera);
    }

    @Test
    @DisplayName("Scene Get Single Added Object Index")
    void sceneGetSingleAddedObjectIndex() {
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new NullObject(), coords, "Null Object");
        scene.addObject(info, null);
        Assertions.assertEquals(0, scene.indexOf(info));
    }

    @Test
    @DisplayName("Scene Get Missed Object Index")
    void sceneGetMissedObjectIndex() {
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new NullObject(), coords, "Null Object");
        scene.addObject(info, null);
        ObjectInfo missed = new ObjectInfo(new Sphere(1.0, 1.0, 1.0), coords, "Not added to Scene object");
        Assertions.assertEquals(-1, scene.indexOf(missed));
    }

    @Test
    public void testAddTextureBefore() {
        listenerFireCount = 0;
        firedPositionIndex = -1;
        scene.addTextureListener(new ListChangeListener() {
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

        scene.add(new UniformTexture(), 0);
        Assert.assertEquals(1, listenerFireCount);
        Assert.assertEquals(0, firedPositionIndex);
    }

    @Test
    public void testAddMaterialBefore() {
        listenerFireCount = 0;
        firedPositionIndex = -1;

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
        scene.add(new UniformMaterial(), 0);
        scene.add(new UniformMaterial(), 0);
        Assert.assertEquals(2, listenerFireCount);
        Assert.assertEquals(0, firedPositionIndex);
    }

}
