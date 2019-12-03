/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion;

import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author maksim.khramov
 */
public class EnvironmentDialogTest {
    
    @Test
    public void testDialog() throws InterruptedException {
        SwingUtilities.invokeLater(() -> {
            new SceneEnvironmentDialog(null, null).setVisible(true);
        });
        
        JDialogOperator op = new JDialogOperator();
        TimeUnit.SECONDS.sleep(10);
        op.close();
    }
}
