/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.polymesh.dialogs;

import artofillusion.ViewerCanvas;
import artofillusion.math.RGBColor;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author maksim.khramov
 */
public class TestPolyMeshToolDialog {
    
    @BeforeClass
    public static void setUpClass() {
        Locale.setDefault(Locale.US);
    }
    
    @Test
    public void testDialog() throws InterruptedException {
        SwingUtilities.invokeLater(() -> {
            PolymeshToolDialog dialog = new PolymeshToolDialog(null);
            
            dialog.setVisible(true);
        });
        
        JDialogOperator op = new JDialogOperator();
        TimeUnit.SECONDS.sleep(10);
        op.close();
    }
}
