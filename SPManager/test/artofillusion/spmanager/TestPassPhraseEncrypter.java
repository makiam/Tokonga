package artofillusion.spmanager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author MaksK
 */
public class TestPassPhraseEncrypter {

    private static final String secretString = "Attack at dawn!";
    private static final String passPhrase = "My Pass Phrase";

    @Test
    public void testPassPhrase()
    {
        StringEncrypter desEncrypter = new StringEncrypter( passPhrase );
        String desEncrypted = desEncrypter.encrypt( secretString );
        String desDecrypted = desEncrypter.decrypt( desEncrypted );

        Assert.assertEquals(secretString, desDecrypted);
    }

}
