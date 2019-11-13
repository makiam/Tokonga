/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package polymesh;

import artofillusion.polymesh.dialogs.PolymeshDisplayProperties;
import java.util.ResourceBundle;
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
    
    public TestPolymeshPropertiesDialog() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @Before
    public void setUp() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        SwingUtilities.invokeLater(this::createDialog);
        
        
        String cap = java.util.ResourceBundle.getBundle("polymesh").getString("setMeshProperties");
        
        JDialogOperator op = new JDialogOperator(cap);
        op.close();
    }
    
    public void createDialog() {
        PolymeshDisplayProperties pd = new PolymeshDisplayProperties(null);
        pd.setVisible(true);
    }
}
