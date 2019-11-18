/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package polymesh;

import artofillusion.ViewerCanvas;
import artofillusion.math.RGBColor;
import artofillusion.polymesh.PolyMesh;
import artofillusion.polymesh.PolymeshTest;
import artofillusion.polymesh.dialogs.PolymeshDisplayProperties;
import javax.swing.SwingUtilities;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author maksim.khramov
 */
public class TestPolymeshPropertiesDialog {
    
    @BeforeClass
    public static void setUpClass() {
        ViewerCanvas.lineColor = java.awt.Color.PINK;
        ViewerCanvas.highlightColor = java.awt.Color.GREEN;
        ViewerCanvas.transparentColor = new RGBColor();
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void hello() {
        SwingUtilities.invokeLater(this::createDialog);
        
        
        String cap = java.util.ResourceBundle.getBundle("polymesh").getString("setMeshProperties");
        
        JDialogOperator op = new JDialogOperator(cap);
        op.close();
    }
    
    public void createDialog() {
        PolyMesh pm = new PolyMesh(0, 0, 0, 0, 1, 1);
        PolymeshDisplayProperties pd = new PolymeshDisplayProperties(null, pm);
        pd.setVisible(true);
    }
}
