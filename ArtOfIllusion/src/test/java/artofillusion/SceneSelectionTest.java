/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author MaksK
 */
public class SceneSelectionTest {
    
    @Test
    public void testGetEmptySceneEmptySelection() {
        Scene scene = new Scene();
        int[] selection = scene.getSelection();
        
        Assert.assertNotNull(selection);
        Assert.assertTrue(selection.length == 0);
    }
    
    @Test
    public void testGetSceneEmptySelection() {
        Scene scene = new Scene();
        scene.addObject(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube", (UndoRecord) null);
        
        int[] selection = scene.getSelection();
        
        Assert.assertNotNull(selection);
        Assert.assertTrue(selection.length == 0);
    }
    
    @Test
    public void testSetSceneSelection() {
        Scene scene = new Scene();
        scene.addObject(new Cube(1d, 1d, 1d), new CoordinateSystem(), "Cube", (UndoRecord) null);

        scene.setSelection(0);
        
        int[] selection = scene.getSelection();
        
        Assert.assertNotNull(selection);
        Assert.assertTrue(selection.length == 1);
    }
}
