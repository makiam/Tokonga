/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.polymesh.dialogs;

import javax.swing.SwingUtilities;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JSpinnerOperator;

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
        JSpinnerOperator jsp = new JSpinnerOperator(op, 0);
        jsp.setValue(7);
        JButtonOperator ok = new JButtonOperator(op, 0);
        ok.clickMouse();
        
    }

    public void createDialog() {
        DivideDialog dialog = new DivideDialog(null);
        dialog.setVisible(true);
        System.out.println(dialog.getValue());
    }
    
}
