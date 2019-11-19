/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.polymesh;

import artofillusion.ViewerCanvas;
import artofillusion.math.RGBColor;
import artofillusion.test.util.StreamUtil;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class PolymeshTest {
    private static enum MeshType {
        
        CUBE(0),
        OCTAHEDRON(2),
        CYLINDER(3),
        PLANE(4);
        
        private int type;
        MeshType(int type) {
            this.type =  type;
        }
        
        
    }
    
    @BeforeClass
    public static void setupClass() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, URISyntaxException, IOException {
        ViewerCanvas.lineColor = java.awt.Color.PINK;
        ViewerCanvas.highlightColor = java.awt.Color.GREEN;
        ViewerCanvas.transparentColor = new RGBColor();
    }
    
    
    @Test
    public void testCreatePolyMeshCube() {
        PolyMesh pm = new PolyMesh(MeshType.CUBE.type, 0, 0, 0, 1, 1);
        
        Assert.assertEquals(8, pm.getVertices().length);
    }
    
    @Test(expected = InvalidObjectException.class)
    public void testLoadMesh() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.put((byte) 11);
        new PolyMesh(StreamUtil.stream(wrap));
    }
}
