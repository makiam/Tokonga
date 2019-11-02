/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion;

import artofillusion.keystroke.KeystrokeManager;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author MaksK
 */
public class KeystrokeTest {
    
    private static Unmarshaller umt = null;

    @BeforeClass
    public static void setUpClass() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        Field umf = KeystrokeManager.class.getDeclaredField("um");
        umf.setAccessible(true);
        umt = (Unmarshaller) umf.get(null);

    }
    
    
    @Test
    public void testKeyStroke() throws IOException, JAXBException {
        KeystrokeManager.KeystrokeRecords kr = (KeystrokeManager.KeystrokeRecords)umt.unmarshal(KeystrokeTest.class.getResource("keystrokes.xml").openStream());
        
        kr.getItems().forEach(System.out::println);
        
    }
}
