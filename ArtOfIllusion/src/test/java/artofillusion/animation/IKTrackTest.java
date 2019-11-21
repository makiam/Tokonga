/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.animation;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.NullObject;
import artofillusion.object.ObjectInfo;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class IKTrackTest {

    @Test
    public void testEmptyIKTrackisNullTrack() {
        IKTrack track = new IKTrack(null);
        Assert.assertTrue(track.isNullTrack());
                
    }
    
    @Test
    public void testDuplicateIKTrack() {
        IKTrack track = new IKTrack(null);
        IKTrack track2 = (IKTrack)track.duplicate(null);
        
        Assert.assertEquals(track.name, track2.name);
        Assert.assertEquals(track.enabled, track2.enabled);
        Assert.assertEquals(track.quantized, track2.quantized);
        
        
    }
    
    @Test
    public void testGetEmptyTrackEmptyDependencies() {
        IKTrack track = new IKTrack(null);
        ObjectInfo[] dependencies = track.getDependencies();
        
        Assert.assertNotNull(dependencies);
        Assert.assertEquals(0, dependencies.length);
    }
    
    
    @Test
    public void testNonEmptyIKTrackisNullTrack() {
        IKTrack track = new IKTrack(new ObjectInfo(new NullObject(), new CoordinateSystem(), "NullObject IK Track"));
        track.edit(null);
        Assert.assertTrue(track.isNullTrack());
                
    }
}
