/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.object;

import artofillusion.math.Vec3;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class CSGModellerTest {
    
    public CSGModellerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testCSGModellerVertexInfoConstructors3() {
        CSGModeller.VertexInfo vi = new CSGModeller.VertexInfo(Vec3.vy(), 1, new double[10]);
        Assert.assertEquals(Vec3.vy(), vi.r);
        Assert.assertEquals(1, vi.smoothness, 0);
        Assert.assertEquals(CSGModeller.UNKNOWN, vi.type);
        Assert.assertNotNull(vi.param);
        Assert.assertEquals(10, vi.param.length);
    }
    
    @Test
    public void testCSGModellerVertexInfoConstructors3WithLastNull() {
        CSGModeller.VertexInfo vi = new CSGModeller.VertexInfo(Vec3.vy(), 1, null);
        Assert.assertEquals(Vec3.vy(), vi.r);
        Assert.assertEquals(1, vi.smoothness, 0);
        Assert.assertEquals(CSGModeller.UNKNOWN, vi.type);
        Assert.assertNull(vi.param);
    }
   
    @Test
    public void testCSGModellerVertexInfoConstructors4() {
        CSGModeller.VertexInfo vi = new CSGModeller.VertexInfo(Vec3.vy(), 1, new double[10], CSGModeller.BOUNDARY);
        Assert.assertEquals(Vec3.vy(), vi.r);
        Assert.assertEquals(1, vi.smoothness, 0);
        Assert.assertEquals(CSGModeller.BOUNDARY, vi.type);
        Assert.assertNotNull(vi.param);
        Assert.assertEquals(10, vi.param.length);
    }
    
    @Test
    public void testCSGModellerVertexInfoConstructors4WithNullParams() {
        CSGModeller.VertexInfo vi = new CSGModeller.VertexInfo(Vec3.vy(), 1, null, CSGModeller.BOUNDARY);
        Assert.assertEquals(Vec3.vy(), vi.r);
        Assert.assertEquals(1, vi.smoothness, 0);
        Assert.assertEquals(CSGModeller.BOUNDARY, vi.type);
        Assert.assertNull(vi.param);
    }
}
