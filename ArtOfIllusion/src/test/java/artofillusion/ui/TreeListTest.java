/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.ui;

import artofillusion.LayoutWindow;
import artofillusion.Scene;
import artofillusion.test.util.RegisterTestResources;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class TreeListTest {
    
    private EditingWindow layout = new LayoutWindow(new Scene());

    @ClassRule
    public static final RegisterTestResources res = new RegisterTestResources();
    
    @BeforeClass
    public static void setUpClass() {
        Translate.setLocale(Locale.ENGLISH);
    }
    public TreeListTest() {
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testGetSelectedObjectsFromEmptyTree() {
        TreeList list = new TreeList(layout);
        Object[] empty = list.getSelectedObjects();
        Assert.assertEquals(0, empty.length);
    }
 
    @Test
    public void testGetSelectedObjectsFromNonEmptyTree() {
        TreeList list = new TreeList(layout);
        TreeElement item = new GenericTreeElement("Label", "String Here", null, list, null);
        
        list.addElement(item);
        list.setSelected(item, true);
        
        Object[] empty = list.getSelectedObjects();
        Assert.assertEquals(1, empty.length);
    }
}
