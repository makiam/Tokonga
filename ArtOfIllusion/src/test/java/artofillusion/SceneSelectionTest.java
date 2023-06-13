/*
 *  Copyright 2022 by Maksim Khramov
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
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author MaksK
 */
public class SceneSelectionTest {

    @Test
    public void testSceneGetEmptySelection() {
        Scene scene = new Scene();
        int[] selection = scene.getSelection();
        Assert.assertNotNull(selection);
        Assert.assertEquals(0, selection.length);

    }

    @Test
    public void testSceneGetSingeSelectionOne() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);
        scene.setSelection(0);

        int[] selection = scene.getSelection();
        Assert.assertNotNull(selection);
        Assert.assertEquals(1, selection.length);

    }

    @Test
    public void testSceneGetSingeSelectionOneWithArray() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);
        scene.setSelection(new int[]{0});

        int[] selection = scene.getSelection();
        Assert.assertNotNull(selection);
        Assert.assertEquals(1, selection.length);

    }

    @Test
    public void testSceneGetSingeSelectionTwo() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);
        scene.setSelection(1);

        int[] selection = scene.getSelection();
        Assert.assertNotNull(selection);
        Assert.assertEquals(1, selection.length);

    }

    @Test
    public void testSceneGetMultipleSelection() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);
        scene.setSelection(1);
        scene.addToSelection(0);

        int[] selection = scene.getSelection();
        Assert.assertNotNull(selection);
        Assert.assertEquals(2, selection.length);

    }

    @Test
    public void testSceneGetMultipleSelectionWithSet() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);
        scene.setSelection(new int[]{0, 1});

        int[] selection = scene.getSelection();
        Assert.assertNotNull(selection);
        Assert.assertEquals(2, selection.length);

    }

    @Test
    public void testSceneAddToSelectionAndCheckFlag() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);
        scene.setSelection(new int[]{0, 1});

        scene.getObjects().forEach(item -> {
            Assert.assertTrue(item.isSelected());
        });

    }

    @Test
    public void testSceneAddToSelectionAndClearAndCheckFlag() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);
        scene.setSelection(new int[]{0, 1});

        scene.getObjects().forEach(item -> {
            Assert.assertTrue(item.isSelected());
        });
        scene.clearSelection();
        scene.getObjects().forEach(item -> {
            Assert.assertFalse(item.isSelected());
        });
    }

    @Test
    public void testSceneGetSelectionWithChild() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");

        one.addChild(two, 0);
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);

        scene.setSelection(0);

        int[] selection = scene.getSelectionWithChildren();
        Assert.assertNotNull(selection);
        Assert.assertEquals(2, selection.length);
    }

    @Test
    public void testSceneGetSelectionWithChildOther() {
        Scene scene = new Scene();
        ObjectInfo one = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null1");
        ObjectInfo two = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null2");

        one.addChild(two, 0);
        scene.addObject(one, (UndoRecord) null);
        scene.addObject(two, (UndoRecord) null);

        scene.setSelection(new int[]{0});

        int[] selection = scene.getSelectionWithChildren();
        Assert.assertNotNull(selection);
        Assert.assertEquals(2, selection.length);
    }
}
