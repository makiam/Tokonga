/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.animation.distortion;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.ObjectInfo;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class ScaleTrackTest {
   
    @Test
    public void testGetTrackValueRange() {
        ScaleTrack track = new ScaleTrack(new ObjectInfo(new artofillusion.object.Cube(1, 1, 1), new CoordinateSystem(), "Cube"));
        double[][] range = track.getValueRange();
    }


}
