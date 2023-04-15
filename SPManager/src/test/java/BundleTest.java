
import java.util.ResourceBundle;
import org.junit.Test;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author MaksK
 */
public class BundleTest {


    @Test
    public void testGetBundle() {
        ResourceBundle bundle = ResourceBundle.getBundle("version");
        System.out.println(bundle.getString("version"));
    }
}
