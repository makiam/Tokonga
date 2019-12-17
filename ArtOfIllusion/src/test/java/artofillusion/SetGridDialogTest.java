/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author maksim.khramov
 */
public class SetGridDialogTest {
    
    private static final Logger logger = Logger.getLogger(SetGridDialogTest.class.getName());
    
    @BeforeClass
    public static void setupClass() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            logger.log(Level.INFO, "Exception at test setup", ex);
        }
    }
    
    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void test() {
        SwingUtilities.invokeLater(() -> {
            new SetGridDialog(null, null).setVisible(true);
        });
        
        JDialogOperator op = new JDialogOperator();
        JCheckBoxOperator sg = new JCheckBoxOperator(op, 0);
        JCheckBoxOperator snap = new JCheckBoxOperator(op, 0);
        System.out.println(sg.isSelected());
        System.out.println(snap.isSelected());
        
        sg.setSelected(true);
        System.out.println(sg.isSelected());
        
        op.close();
    }
}
