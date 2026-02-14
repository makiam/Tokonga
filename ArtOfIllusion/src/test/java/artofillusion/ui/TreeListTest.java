/* Copyright (C) 2024-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import artofillusion.object.NullObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


@DisplayName("Tree List Component Test")
public class TreeListTest {
    private class DummyTreeElement extends TreeElement {

        private final Object item;
        public  DummyTreeElement(Object item) {
            this.item = item;
            this.children = new ArrayList<>();
        }

        @Override
        public String getLabel() {
            return null;
        }

        @Override
        public boolean canAcceptAsParent(TreeElement el) {
            return false;
        }

        @Override
        public void addChild(TreeElement el, int position) {
            this.children.add(position, el);
        }

        @Override
        public void removeChild(Object obj) {

        }

        @Override
        public Object getObject() {
            return item;
        }

        @Override
        public boolean isGray() {
            return false;
        }
    }

    @Test
    public void getElementsFromEmptyList() {

        TreeList treeList = new TreeList(null);
        TreeElement[] elements = treeList.getElements();
        Assertions.assertEquals(0, elements.length);

    }

    @Test
    public void getSingleItem() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);

        TreeElement el = new DummyTreeElement(new NullObject());

        treeList.addElement(el, 0);
        TreeElement[] elements = treeList.getElements();
        Assertions.assertEquals(1, elements.length);
        Assertions.assertEquals(el, elements[0]);
    }

    @Test
    public void getDoubleItemOneLevel() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);

        TreeElement el = new DummyTreeElement(new NullObject());

        treeList.addElement(el);
        treeList.addElement(el);

        TreeElement[] elements = treeList.getElements();
        Assertions.assertEquals(2, elements.length);
        Assertions.assertEquals(el, elements[0]);
        Assertions.assertEquals(el, elements[1]);
    }

    @Test
    public void getDoubleItemTwoLevels() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);

        TreeElement el0 = new DummyTreeElement(new NullObject());
        TreeElement el2 = new DummyTreeElement(new NullObject());

        el0.addChild(el2, 0);

        treeList.addElement(el0);
        TreeElement[] elements = treeList.getElements();
        Assertions.assertEquals(2, elements.length);
        Assertions.assertEquals(el0, elements[0]);
        Assertions.assertEquals(el2, elements[1]);
    }


    @Test
    public void findNonExistedItemInEmptyTree() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);

        Assertions.assertNull(treeList.findElement(new NullObject()));
    }

    @Test
    public void findNonExistedItemInTreeOneLevel() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);

        TreeElement el0 = new DummyTreeElement(new NullObject());
        treeList.addElement(el0);
        Assertions.assertNull(treeList.findElement(new NullObject()));

    }

    @Test
    public void findExistedItemInTreeOneLevel() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);
        NullObject target = new NullObject();
        TreeElement el0 = new DummyTreeElement(target);
        treeList.addElement(el0);

        TreeElement result = treeList.findElement(target);
        Assertions.assertEquals(el0, result);


    }


    @Test
    public void findNonExistedItemInTreeTwoLevels() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);

        TreeElement el0 = new DummyTreeElement(new NullObject());
        TreeElement el2 = new DummyTreeElement(new NullObject());
        el0.addChild(el2, 0);

        treeList.addElement(el0);
        Assertions.assertNull(treeList.findElement(new NullObject()));

    }

    @Test
    public void findExistedItemInTreeTwoLevels() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);

        TreeElement el0 = new DummyTreeElement(new NullObject());

        NullObject target = new NullObject();


        TreeElement el2 = new DummyTreeElement(target);
        el0.addChild(el2, 0);

        treeList.addElement(el0);
        TreeElement result = treeList.findElement(target);
        Assertions.assertEquals(el2, result);

    }

    @Test
    public void findSelectedItemsInEmptyTree() {
        TreeList treeList = new TreeList(null);
        treeList.setUpdateEnabled(false);

        Object[] selected = treeList.getSelectedObjects();
        Assertions.assertNotNull(selected);
        Assertions.assertEquals(0, selected.length);
    }

    @Test
    public void findSelectedItemsInOneLevelTree() {
        TreeList treeList = new TreeList(null);
        //treeList.setUpdateEnabled(false);

        NullObject target = new NullObject();
        TreeElement el0 = new DummyTreeElement(target);
        Assertions.assertFalse(el0.isSelected());
        treeList.addElement(el0);
        treeList.setSelected(el0, true);


        Object[] selected = treeList.getSelectedObjects();
        Assertions.assertTrue(el0.isSelected());
        Assertions.assertNotNull(selected);
        Assertions.assertEquals(1, selected.length);
    }

    @Test
    public void findSelectedItemsInOneLevelTreeByObject() {
        TreeList treeList = new TreeList(null);
        //treeList.setUpdateEnabled(false);

        NullObject target = new NullObject();
        TreeElement el0 = new DummyTreeElement(target);
        Assertions.assertFalse(el0.isSelected());

        treeList.addElement(el0);
        treeList.setSelected(target, true);


        Object[] selected = treeList.getSelectedObjects();
        Assertions.assertTrue(el0.isSelected());
        Assertions.assertNotNull(selected);
        Assertions.assertEquals(1, selected.length);
    }

    @Test
    public void findSelectedItemsInTwoLevelsTree() {
        TreeList treeList = new TreeList(null);
        //treeList.setUpdateEnabled(false);


        TreeElement el0 = new DummyTreeElement( new NullObject());
        TreeElement el1 = new DummyTreeElement( new NullObject());

        el0.addChild(el1, 0);

        treeList.addElement(el0);
        treeList.setSelected(el0, true);
        treeList.setSelected(el1, true);

        Object[] selected = treeList.getSelectedObjects();
        Assertions.assertNotNull(selected);
        Assertions.assertEquals(2, selected.length);
    }
}