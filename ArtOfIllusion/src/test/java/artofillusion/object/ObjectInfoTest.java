/* Copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import artofillusion.RenderingMesh;
import artofillusion.animation.PoseTrack;
import artofillusion.animation.PositionTrack;
import artofillusion.animation.RotationTrack;
import artofillusion.animation.Skeleton;
import artofillusion.animation.TextureTrack;
import artofillusion.animation.Track;
import artofillusion.animation.distortion.Distortion;
import artofillusion.material.Material;
import artofillusion.material.MaterialMapping;
import artofillusion.material.UniformMaterial;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import artofillusion.texture.Texture;
import artofillusion.texture.TextureMapping;
import artofillusion.texture.UniformTexture;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author MaksK
 */
@DisplayName("Object Info Test")
class ObjectInfoTest {

    /**
     * Test to create new ObjectInfo object with all defaults
     */
    @Test
    @DisplayName("Test Create New Object Info")
    void testCreateNewObjectInfo() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        Assertions.assertNotNull(test);
        Assertions.assertEquals("Test", test.getName());
        Assertions.assertEquals(-1, test.getId());
        Assertions.assertTrue(test.isVisible());
        Assertions.assertFalse(test.isLocked());
        Assertions.assertNull(test.getPose());
        Assertions.assertNull(test.getParent());
        Assertions.assertNotNull(test.getChildren());
        Assertions.assertEquals(0, test.getChildren().length);
        Assertions.assertEquals(0,(test.getTracks().length));
        Assertions.assertNull(test.getDistortion());
    }

    /**
     * Test to check that parent object is set and get properly
     */
    @Test
    @DisplayName("Test Set Object Info Parent")
    void testSetObjectInfoParent() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null");
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.setParent(parent);
        Assertions.assertNotNull(test.getParent());
        Assertions.assertEquals(parent, test.getParent());
    }

    /**
     * Test to add some tracks
     */
    @Test
    @DisplayName("Test Add Track")
    void testAddTrack() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.addTrack(new PositionTrack(test), 0);
        test.addTrack(new RotationTrack(test), 1);
        Assertions.assertNotNull(test.getTracks());
        Assertions.assertEquals(2, test.getTracks().length);
        Assertions.assertInstanceOf(PositionTrack.class, test.getTracks()[0]);
        Assertions.assertInstanceOf(RotationTrack.class, test.getTracks()[1]);
    }

    /**
     *
     */
    @Test
    @DisplayName("Test Add Track To Given Error Pos")
    void testAddTrackToGivenErrorPos() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> test.addTrack(new PositionTrack(test), 5));
    }

    /**
     *
     */
    @Test
    @DisplayName("Test Add Track To Given Pos In Exist List")
    void testAddTrackToGivenPosInExistList0() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.addTrack(new RotationTrack(test), 0);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> test.addTrack(new PositionTrack(test), 5));
    }

    @Test
    @DisplayName("Test Add Track To Given Pos In Exist List")
    void testAddTrackToGivenPosInExistList1() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.addTrack(new RotationTrack(test));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> test.addTrack(new PositionTrack(test), 5));
    }

    /**
     * Test check added track can be found and removed
     */
    @Test
    @DisplayName("Test Remove Track By Track")
    void testRemoveTrackByTrack() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        Track<?> pTrack = new PositionTrack(test);
        Track<?> rTrack = new RotationTrack(test);
        test.addTrack(pTrack, 0);
        test.addTrack(rTrack, 1);
        test.removeTrack(pTrack);
        Assertions.assertNotNull(test.getTracks());
        Assertions.assertEquals(1, test.getTracks().length);
        Assertions.assertInstanceOf(RotationTrack.class, test.getTracks()[0]);
    }

    /**
     * Test to remove track by position
     */
    @Test
    @DisplayName("Test Remove Track By Position First")
    void testRemoveTrackByPositionFirst() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        Track<?> pTrack = new PositionTrack(test);
        Track<?> rTrack = new RotationTrack(test);
        test.addTrack(pTrack, 0);
        test.addTrack(rTrack, 1);
        test.removeTrack(0);
        Assertions.assertNotNull(test.getTracks());
        Assertions.assertEquals(1, test.getTracks().length);
        Assertions.assertInstanceOf(RotationTrack.class, test.getTracks()[0]);
    }

    /**
     * Test to remove track by position
     */
    @Test
    @DisplayName("Test Remove Track By Position Last")
    void testRemoveTrackByPositionLast() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        Track<?> pTrack = new PositionTrack(test);
        Track<?> rTrack = new RotationTrack(test);
        test.addTrack(pTrack, 0);
        test.addTrack(rTrack, 1);
        test.removeTrack(1);
        Assertions.assertNotNull(test.getTracks());
        Assertions.assertEquals(1, test.getTracks().length);
        Assertions.assertInstanceOf(PositionTrack.class, test.getTracks()[0]);
    }

    /**
     * Test objectInfo duplicate
     */
    @Test
    @DisplayName("Test Duplicate")
    void testDuplicate() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.setVisible(false);
        test.setLocked(true);
        ObjectInfo duplicate = test.duplicate();
        Assertions.assertNotNull(duplicate);
        Assertions.assertFalse(duplicate.isVisible());
        Assertions.assertTrue(duplicate.isLocked());
        Assertions.assertNotEquals(duplicate, test);
        Assertions.assertEquals(duplicate.getObject(), test.getObject());
    }

    /**
     * Test objectInfo replace geometry and duplicate
     */
    @Test
    @DisplayName("Test Duplicate With New Geometry")
    void testDuplicateWithNewGeometry() {
        ObjectInfo source = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        source.setVisible(false);
        source.setLocked(true);
        Object3D newObj = new Sphere(1d, 1d, 1d);
        ObjectInfo duplicate = source.duplicate(newObj);
        Assertions.assertNotNull(duplicate);
        Assertions.assertFalse(duplicate.isVisible());
        Assertions.assertTrue(duplicate.isLocked());
        Assertions.assertNotEquals(duplicate, source);
        Assertions.assertNotEquals(duplicate.getObject(), source.getObject());
        Assertions.assertInstanceOf(Sphere.class, duplicate.getObject());
    }

    /**
     * Test objectInfo replace geometry and duplicate with existed tracks data
     */
    @Test
    @DisplayName("Test Duplicate With New Geometry And Tracks")
    void testDuplicateWithNewGeometryAndTracks() {
        ObjectInfo source = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        source.setVisible(false);
        source.setLocked(true);
        source.addTrack(new PoseTrack(source), 0);
        source.addTrack(new PositionTrack(source), 1);
        source.addTrack(new RotationTrack(source), 2);
        Object3D newObj = new Sphere(1d, 1d, 1d);
        ObjectInfo duplicate = source.duplicate(newObj);
        Assertions.assertNotNull(duplicate);
        Assertions.assertFalse(duplicate.isVisible());
        Assertions.assertTrue(duplicate.isLocked());
        Assertions.assertNotEquals(duplicate, source);
        Assertions.assertNotEquals(duplicate.getObject(), source.getObject());
        Assertions.assertInstanceOf(Sphere.class, duplicate.getObject());
        Assertions.assertEquals(3, duplicate.getTracks().length);
        Assertions.assertEquals(duplicate, duplicate.getTracks()[0].getParent());
    }

    /**
     * Test objectInfo duplicate with existed tracks
     */
    @Test
    @DisplayName("Test Duplicate With Tracks")
    void testDuplicateWithTracks() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.setVisible(false);
        test.setLocked(true);
        test.addTrack(new PositionTrack(test), 0);
        test.addTrack(new RotationTrack(test), 1);
        ObjectInfo duplicate = test.duplicate();
        Assertions.assertNotNull(duplicate);
        Assertions.assertFalse(duplicate.isVisible());
        Assertions.assertTrue(duplicate.isLocked());
        Assertions.assertNotEquals(duplicate, test);
        Assertions.assertEquals(duplicate.getObject(), test.getObject());

        Assertions.assertEquals(2, duplicate.getTracks().length);
        Assertions.assertInstanceOf(PositionTrack.class, duplicate.getTracks()[0]);
        Assertions.assertInstanceOf(RotationTrack.class, duplicate.getTracks()[1]);
        Assertions.assertEquals(duplicate, duplicate.getTracks()[0].getParent());
        Assertions.assertNull(duplicate.getDistortion());
    }

    /**
     * Test objectInfo duplicate with distortion data
     */
    @Test
    @DisplayName("Test Duplicate With Distortion")
    void testDuplicateWithDistortion() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.setDistortion(new DistortionImpl());
        ObjectInfo duplicate = test.duplicate();
        Assertions.assertNotNull(duplicate.getDistortion());
    }

    /**
     * Test objectInfo to add child objects at the begin of list
     */
    @Test
    @DisplayName("Test Add Child First")
    void testAddChildFirst() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Parent");
        ObjectInfo childOne = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        ObjectInfo childTwo = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Sphere");
        parent.addChild(childOne, 0);
        parent.addChild(childTwo, 0);
        Assertions.assertEquals(2, parent.getChildren().length);
        Assertions.assertEquals(childTwo, parent.getChildren()[0]);
        Assertions.assertEquals(childOne, parent.getChildren()[1]);
    }

    /**
     * Test objectInfo to add child objects at the end of list
     */
    @Test
    @DisplayName("Test Add Child Two")
    void testAddChildTwo() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Parent");
        ObjectInfo childOne = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        ObjectInfo childTwo = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Sphere");
        parent.addChild(childOne, 0);
        parent.addChild(childTwo, 1);
        Assertions.assertEquals(2, parent.getChildren().length);
        Assertions.assertEquals(childOne, parent.getChildren()[0]);
        Assertions.assertEquals(childTwo, parent.getChildren()[1]);
    }

    /**
     * Test objectInfo to remove given child from list
     */
    @Test
    @DisplayName("Test Remove Child By Child")
    void testRemoveChildByChild() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Parent");
        ObjectInfo childOne = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        ObjectInfo childTwo = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Sphere");
        parent.addChild(childOne, 0);
        parent.addChild(childTwo, 1);
        parent.removeChild(childOne);
        Assertions.assertEquals(1, parent.getChildren().length);
        Assertions.assertEquals(childTwo, parent.getChildren()[0]);
    }

    /**
     * Test objectInfo to remove non-existed child from an object list
     */
    @Test
    @DisplayName("Test Remove Child By Missed Child")
    void testRemoveChildByMissedChild() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Parent");
        ObjectInfo childOne = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        ObjectInfo childTwo = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Sphere");
        parent.addChild(childOne, 0);
        parent.removeChild(childTwo);
        Assertions.assertEquals(1, parent.getChildren().length);
        Assertions.assertEquals(childOne, parent.getChildren()[0]);
    }

    /**
     * Test objectInfo to remove null child from list
     */
    @Test
    @DisplayName("Test Remove Child By Null Child")
    void testRemoveChildByNullChild() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Parent");
        ObjectInfo childOne = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        parent.addChild(childOne, 0);
        parent.removeChild(null);
        Assertions.assertEquals(1, parent.getChildren().length);
        Assertions.assertEquals(childOne, parent.getChildren()[0]);
    }

    /**
     * Test objectInfo to remove child from list by position
     */
    @Test
    @DisplayName("Test Remove Child By Position One")
    void testRemoveChildByPositionOne() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Parent");
        ObjectInfo childOne = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        ObjectInfo childTwo = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Sphere");
        parent.addChild(childOne, 0);
        parent.addChild(childTwo, 1);
        parent.removeChild(1);
        Assertions.assertEquals(1, parent.getChildren().length);
        Assertions.assertEquals(childOne, parent.getChildren()[0]);
    }

    /**
     * Test objectInfo to remove child from list by position
     */
    @Test
    @DisplayName("Test Remove Child By Position Two")
    void testRemoveChildByPositionTwo() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Parent");
        ObjectInfo childOne = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        ObjectInfo childTwo = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Sphere");
        parent.addChild(childOne, 0);
        parent.addChild(childTwo, 1);
        parent.removeChild(0);
        Assertions.assertEquals(1, parent.getChildren().length);
        Assertions.assertEquals(childTwo, parent.getChildren()[0]);
    }

    /**
     * Test objectInfo copy data from other objectInfo and points to same geometry
     */
    @Test
    @DisplayName("Test Copy Info")
    void testCopyInfo() {
        Object3D sourceGeometry = new Cube(1d, 1d, 1d);
        var coordinates = new CoordinateSystem(Vec3.vx(), Vec3.vy(), Vec3.vz());
        ObjectInfo source = new ObjectInfo(sourceGeometry, coordinates, "Source");
        source.setVisible(false);
        source.setLocked(true);
        source.setId(100);
        ObjectInfo target = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Target");
        target.copyInfo(source);
        Assertions.assertEquals(100, target.getId());
        Assertions.assertEquals("Source", target.getName());
        Assertions.assertEquals(sourceGeometry, target.getObject());
        Assertions.assertEquals(coordinates, target.getCoords());
        Assertions.assertTrue(target.isLocked());
        Assertions.assertFalse(target.isVisible());
    }

    /**
     * Test objectInfo copy data from other objectInfo and points to same geometry
     * Checks that source empty tracks overwrite existed one
     */
    @Test
    @DisplayName("Test Copy Info With Empty Source Tracks Over Existed")
    void testCopyInfoWithEmptyTracksOverExisted() {
        ObjectInfo source = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Source");
        ObjectInfo target = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Target");
        target.addTrack(new PositionTrack(target), 0);
        target.addTrack(new RotationTrack(target), 1);
        target.copyInfo(source);
        Assertions.assertEquals(0, target.getTracks().length);
    }

    /**
     * Test objectInfo copy data from other objectInfo and points to same geometry
     * Checks that source tracks overwrite existed one
     */
    @Test
    @DisplayName("Test Copy Info With Tracks Over Existed")
    void testCopyInfoWithTracksOverExisted() {
        ObjectInfo source = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Source");
        source.addTrack(new TextureTrackImpl((source)), 0);
        ObjectInfo target = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Target");
        target.addTrack(new PositionTrack(target), 0);
        target.addTrack(new RotationTrack(target), 1);
        target.copyInfo(source);
        Assertions.assertNotNull(target.getTracks());
        Assertions.assertEquals(1, target.getTracks().length);
        Track<?> testT = target.getTracks()[0];
        Assertions.assertInstanceOf(TextureTrack.class, testT);
        Assertions.assertEquals(target, target.getTracks()[0].getParent());
    }

    /**
     * Test objectInfo copy data from other objectInfo and points to same geometry
     * Checks that source distortion copied
     */
    @Test
    @DisplayName("Test Copy Info With Distortion Over Empty")
    void testCopyInfoWithDistortionOverEmpty() {
        ObjectInfo source = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Source");
        Distortion dist = new DistortionImpl();
        source.setDistortion(dist);
        ObjectInfo target = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Target");
        target.copyInfo(source);
        Assertions.assertNotNull(target.getDistortion());
        Assertions.assertInstanceOf(DistortionImpl.class, target.getDistortion());
    }

    /**
     * Test objectInfo copy data from other objectInfo and points to same geometry
     * Checks that empty distortion data overwrites existed one
     */
    @Test
    @DisplayName("Test Copy Info With Null Distortion Over Existed")
    void testCopyInfoWithNullDistortionOverExisted() {
        ObjectInfo source = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Source");
        ObjectInfo target = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Target");
        target.setDistortion(new DistortionImpl());
        target.copyInfo(source);
        Assertions.assertNull(target.getDistortion());
    }

    /**
     * Test checks new distortion sets for an object
     */
    @Test
    @DisplayName("Test Set Distortion")
    void testSetDistortion() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.setDistortion(new DistortionImpl());
        Distortion cd = test.getDistortion();
        Assertions.assertNotNull(cd);
        Assertions.assertNull(cd.getPreviousDistortion());
    }

    /**
     * Test checks new distortion adds to non existed one
     */
    @Test
    @DisplayName("Test Add Distortion To Null")
    void testAddDistortionToNull() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.addDistortion(new DistortionImpl());
        Distortion cd = test.getDistortion();
        Assertions.assertNotNull(cd);
        Assertions.assertNull(cd.getPreviousDistortion());
    }

    /**
     * Test checks new distortion adds to already existed one
     */
    @Test
    @DisplayName("Test Add Distortion To Existed")
    void testAddDistortionToExisted() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        Distortion dist1 = new DistortionImpl();
        Distortion dist2 = new DistortionImpl();
        test.addDistortion(dist1);
        test.addDistortion(dist2);
        Distortion cd = test.getDistortion();
        Assertions.assertNotNull(cd);
        Assertions.assertNotNull(cd.getPreviousDistortion());
    }

    /**
     * Test clears non existed distortion
     */
    @Test
    @DisplayName("Test Clear Null Distortion")
    void testClearNullDistortion() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        test.clearDistortion();
        Assertions.assertNull(test.getDistortion());
        Assertions.assertFalse(test.isDistorted());
    }

    /**
     * Test clears existed distortion from object
     */
    @Test
    @DisplayName("Test Clear Not Null Distortion")
    void testClearNotNullDistortion() {
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        Distortion dist = new DistortionImpl();
        test.setDistortion(dist);
        Assertions.assertNull(dist.getPreviousDistortion());
        test.clearDistortion();
        Assertions.assertNull(test.getDistortion());
        Assertions.assertFalse(test.isDistorted());
    }

    /**
     * Test sets and checks material add
     */
    @Test
    @DisplayName("Set Object Info Material")
    void setObjectInfoMaterial() {
        Object3D cube = new Cube(1d, 1d, 1d);
        ObjectInfo test = new ObjectInfo(cube, new CoordinateSystem(), "Test");
        Material mat = new UniformMaterial();
        MaterialMapping map = mat.getDefaultMapping(cube);
        test.setMaterial(mat, map);
        Object3D res = test.getObject();
        Assertions.assertNotNull(res.getMaterial());
        Assertions.assertNotNull(res.getMaterialMapping());
        Assertions.assertEquals(mat, res.getMaterial());
        Assertions.assertEquals(map, res.getMaterialMapping());
    }

    /**
     * Test to get skeleton data
     */
    @Test
    @DisplayName("Test Get Cube Skeleton")
    void testGetCubeSkeleton() {
        Object3D cube = new Cube(1d, 1d, 1d);
        ObjectInfo test = new ObjectInfo(cube, new CoordinateSystem(), "Test");
        Assertions.assertNull(test.getSkeleton());
    }

    /**
     * Test to get skeleton data from mesh object
     */
    @Test
    @DisplayName("Test Get Mesh Skeleton")
    void testGetMeshSkeleton() {
        Object3D meshCube = new Cube(1d, 1d, 1d);
        TriangleMesh mesh = meshCube.convertToTriangleMesh(0.1);
        ObjectInfo test = new ObjectInfo(meshCube, new CoordinateSystem(), "Test");
        test.setObject(mesh);
        mesh = (TriangleMesh) test.getObject();
        Skeleton sc = new Skeleton();
        mesh.setSkeleton(sc);
        Assertions.assertNotNull(test.getSkeleton());
        Assertions.assertEquals(sc, test.getSkeleton());
    }

    /**
     * Test sets texture to object and checks that existed texture tracks changes its parameters
     */
    @Test
    @DisplayName("Test Set Texture With Texture Tracks")
    void testSetTextureWithTextureTracks() {
        Object3D cube = new Cube(1d, 1d, 1d);
        ObjectInfo test = new ObjectInfo(cube, new CoordinateSystem(), "Test");
        TextureTrackImpl tt = new TextureTrackImpl(test);
        test.addTrack(tt, 0);
        test.addTrack(new PositionTrack(test), 1);
        Texture tex = new UniformTexture();
        TextureMapping map = tex.getDefaultMapping(cube);
        test.setTexture(tex, map);
        Assertions.assertEquals(1, tt.getParameterChangedEventFireCount());
        Assertions.assertEquals(tex, test.getObject().getTexture());
        Assertions.assertEquals(map, test.getObject().getTextureMapping());
    }

    /**
     * Test check that getDistortedObject without distortion returns unchanged object
     */
    @Test
    @DisplayName("Test Get Distorted Object No Distortion")
    void testGetDistortedObjectNoDistortion() {
        Object3D cube = new Cube(1d, 1d, 1d);
        ObjectInfo test = new ObjectInfo(cube, new CoordinateSystem(), "Test");
        Object3D distorted = test.getDistortedObject(0.1d);
        Assertions.assertNotNull(distorted);
        Assertions.assertEquals(test.getObject(), distorted);

    }

    /**
     * Test check that getDistortedObject without distortion returns unchanged object
     * Distorted object is wrapped with ObjectWrapper
     */
    @Test
    @DisplayName("Test Get Distorted Object From Wrapper No Distortion")
    void testGetDistortedObjectFromWrapperNoDistortion() {
        Object3D cube = new Cube(1d, 1d, 1d);
        ObjectWrapperImpl wrapper = new ObjectWrapperImpl(cube);
        ObjectInfo test = new ObjectInfo(wrapper, new CoordinateSystem(), "Test");
        Object3D distorted = test.getDistortedObject(0.1d);
        Assertions.assertNotNull(distorted);
        Assertions.assertEquals(test.getObject(), distorted);
    }

    @Test
    @DisplayName("Test Get Rendering Mesh With Unset Texture")
    void testGetRenderingMeshWithUnsetTexture() {
        Object3D cube = new Cube(1d, 1d, 1d);
        ObjectInfo test = new ObjectInfo(cube, new CoordinateSystem(), "Test");
        assertThrows(NullPointerException.class, () -> test.getRenderingMesh(0.1d));
    }

    @Test
    @DisplayName("Test Get Rendering Mesh")
    void testGetRenderingMesh() {
        Object3D cube = new Cube(1d, 1d, 1d);
        ObjectInfo test = new ObjectInfo(cube, new CoordinateSystem(), "Test");
        Texture tex = new UniformTexture();
        TextureMapping map = tex.getDefaultMapping(cube);
        test.setTexture(tex, map);
        Assertions.assertNotNull(test.getRenderingMesh(0.1d));
        Assertions.assertInstanceOf(RenderingMesh.class, test.getRenderingMesh(0.1d));
    }

    @Test
    @DisplayName("Test Duplicate All")
    void testDuplicateAll() {
        ObjectInfo parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Parent");
        ObjectInfo childOne = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube");
        ObjectInfo childTwo = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Sphere");
        parent.addChild(childOne, 0);
        parent.addChild(childTwo, 0);
        ObjectInfo test = new ObjectInfo(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Test");
        ObjectInfo deep = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null");
        ObjectInfo wrapper = new ObjectInfo(new ObjectWrapperImpl(new Cylinder(5, 1, 2, 3)), new CoordinateSystem(), "Wrapper");
        deep.addChild(wrapper, 0);
        test.addTrack(new PositionTrack(test), 0);
        test.addTrack(new RotationTrack(test), 1);
        ObjectInfo[] source = new ObjectInfo[]{parent, test, deep};
        ObjectInfo[] result = ObjectInfo.duplicateAll(source);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.length);
        Assertions.assertEquals(2, result[1].getTracks().length);
    }

    @Getter
    private static class TextureTrackImpl extends TextureTrack {

        private int parameterChangedEventFireCount = 0;

        public TextureTrackImpl(ObjectInfo info) {
            super(info);
        }

        @Override
        public void parametersChanged() {
            parameterChangedEventFireCount++;
        }

    }

    private static class DistortionImpl extends Distortion {



        @Override
        public boolean isIdenticalTo(Distortion d) {
            return false;
        }

        @Override
        public Distortion duplicate() {
            return new DistortionImpl();
        }

        @Override
        public Mesh transform(Mesh obj) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }


    private static class ObjectWrapperImpl extends ObjectWrapper {

        public ObjectWrapperImpl(Object3D target) {
            this.theObject = target;
        }

        @Override
        public ObjectWrapperImpl duplicate() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void copyObject(Object3D obj) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSize(double xSize, double ySize, double zSize) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
