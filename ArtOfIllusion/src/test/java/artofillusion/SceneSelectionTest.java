/*
 *  Copyright 2022-2024 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.NullObject;
import artofillusion.object.ObjectInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

/**
 * @author MaksK
 */
@DisplayName("Scene Selection Test")
class SceneSelectionTest {

    @Test
    @DisplayName("Test Scene Get Empty Selection")
    void testSceneGetEmptySelection() {
        Scene scene = new Scene();
        int[] selection = scene.getSelection();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(0, selection.length);
    }

    @Test
    @DisplayName("Test Scene Get Singe Selection One")
    void testSceneGetSingeSelectionOne() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one,  null);
        scene.addObject(two,  null);
        scene.setSelection(0);
        int[] selection = scene.getSelection();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(1, selection.length);
    }

    @Test
    @DisplayName("Test Scene Get Singe Selection One With Array")
    void testSceneGetSingeSelectionOneWithArray() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one,  null);
        scene.addObject(two,  null);
        scene.setSelection(new int[]{0});

        int[] selection = scene.getSelection();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(1, selection.length);

    }

    @Test
    @DisplayName("Test Scene Get Singe Selection One With Array 2")
    public void testSceneGetSingeSelectionOneWithArray2() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one,  null);
        scene.addObject(two,  null);
        scene.setSelection(0);

        int[] selection = scene.getSelection();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(1, selection.length);
    }

    @Test
    @DisplayName("Test Scene Get Singe Selection Two")
    void testSceneGetSingeSelectionTwo() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(1);
        int[] selection = scene.getSelection();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(1, selection.length);
    }

    @Test
    @DisplayName("Test Scene Get Multiple Selection")
    void testSceneGetMultipleSelection() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(1);
        scene.addToSelection(0);
        int[] selection = scene.getSelection();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(2, selection.length);
    }

    @Test
    @DisplayName("Test Scene Get Multiple Selection With Set")
    void testSceneGetMultipleSelectionWithSet() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(new int[]{0, 1});

        int[] selection = scene.getSelection();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(2, selection.length);

    }

    @Test
    @DisplayName("Test Scene Get Multiple Selection With Set 2")
    public void testSceneGetMultipleSelectionWithSet2() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(0, 1);

        int[] selection = scene.getSelection();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(2, selection.length);
    }

    @Test
    @DisplayName("Test Scene Add To Selection And Check Flag")
    void testSceneAddToSelectionAndCheckFlag() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(new int[]{0, 1});

        scene.getObjects().forEach(item -> {
            Assertions.assertTrue(item.isSelected());
        });

    }

    @Test
    @DisplayName("Test Scene Add To Selection And Check Flag 2")
    public void testSceneAddToSelectionAndCheckFlag2() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(0, 1);

        scene.getObjects().forEach(item -> {
            Assertions.assertTrue(item.isSelected());
        });
    }

    @Test
    @DisplayName("Test Scene Add To Selection And Clear And Check Flag")
    void testSceneAddToSelectionAndClearAndCheckFlag() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(new int[]{0, 1});

        scene.getObjects().forEach(item -> {
            Assertions.assertTrue(item.isSelected());
        });
        scene.clearSelection();
        scene.getObjects().forEach(item -> {
            Assertions.assertFalse(item.isSelected());
        });
    }

    @Test
    @DisplayName("Test Scene Add To Selection And Clear And Check Flag 2")
    public void testSceneAddToSelectionAndClearAndCheckFlag2() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(0, 1);

        scene.getObjects().forEach(item -> {
            Assertions.assertTrue(item.isSelected());
        });
        scene.clearSelection();
        scene.getObjects().forEach(item -> {
            Assertions.assertFalse(item.isSelected());
        });
    }

    @Test
    @DisplayName("Test Scene Get Selection With Child")
    void testSceneGetSelectionWithChild() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        one.addChild(two, 0);
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(0);
        int[] selection = scene.getSelectionWithChildren();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(2, selection.length);
    }

    @Test
    @DisplayName("Test Scene Get Selection With Child Other")
    void testSceneGetSelectionWithChildOther() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        one.addChild(two, 0);
        scene.addObject(one, null);
        scene.addObject(two, null);
        scene.setSelection(0);
        int[] selection = scene.getSelectionWithChildren();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(2, selection.length);
    }

    @Test
    @DisplayName("Test Scene Get Selection With More Child")
    void testSceneGetSelectionWithChildMore() {
        Scene scene = new Scene();
        for(var loop = 0; loop < 10; loop++) {
            var so = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null" + loop);

            scene.addObject(so, null);
        }
        int[] selection = scene.getSelectionWithChildren();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(0, selection.length);

        scene.setSelection(0, 3, 6, 9);

        selection = scene.getSelectionWithChildren();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(4, selection.length);


    }

    @Test
    @DisplayName("Test Scene Get Selection With Child Deep hierarchy")
    void testSceneGetSelectionWithChildDeep() {
        Scene scene = new Scene();
        var parent = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null" + 0);
        scene.addObject(parent, null);

        for(var loop = 1; loop < 10; loop++) {

            var so = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null" + loop);
            scene.addObject(so, null);
            parent.addChild(so, 0);

            parent = so;
        }

        int[] selection = scene.getSelectionWithChildren();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(0, selection.length);

        scene.setSelection(0);

        selection = scene.getSelectionWithChildren();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(10, selection.length);

        scene.setSelection(4);

        selection = scene.getSelectionWithChildren();
        Assertions.assertNotNull(selection);
        Assertions.assertEquals(6, selection.length);


    }
}
