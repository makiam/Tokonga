/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.polymesh.dialogs;

import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author MaksK
 */
public class DivideDialogTest {
    
    
    @Test
    public void testDialog() throws InterruptedException {
        SwingUtilities.invokeLater(this::createDialog);
        
        String caption = java.util.ResourceBundle.getBundle("polymesh").getString("subdivideEdgesTitle");

        
        JDialogOperator op = new JDialogOperator(caption);
        TimeUnit.MINUTES.sleep(1);
        op.close();
    }

    public void createDialog() {
        DivideDialog dialog = new DivideDialog(null, true);
        dialog.setVisible(true);
    }
    
}
