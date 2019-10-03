/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.spmanager;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class StringEncrypterTest {

    private final String secretString = "Attack at dawn!";
    private final String passPhrase = "My Pass Phrase";
        
    /**
     * Test of encrypt method, of class StringEncrypter.
     */
    @Test
    public void testEncrypt() {
        StringEncrypter se = new StringEncrypter(passPhrase);
        String encoded = se.encrypt(secretString);
        
        String decoded = se.decrypt(encoded);
        
        Assert.assertEquals(secretString, decoded);
        
    }


    
}
