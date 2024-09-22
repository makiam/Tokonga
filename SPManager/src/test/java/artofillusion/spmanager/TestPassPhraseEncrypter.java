/*
 *  Copyright 2022 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.spmanager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author MaksK
 */
class TestPassPhraseEncrypter {

    private static final String secretString = "Attack at dawn!";
    private static final String secretString2 = "The quick brown fox jumps over the lazy dog.";
    private static final String passPhrase = "My Pass Phrase";

    @Test
    public void testEmpty() {
        StringEncrypter encrypter = new StringEncrypter(passPhrase);
        String desEncrypted = encrypter.encrypt("");
        String desDecrypted = encrypter.decrypt(desEncrypted);

        Assertions.assertEquals("", desDecrypted);
    }

    @Test
    public void testPassPhrase() {
        StringEncrypter encrypter = new StringEncrypter(passPhrase);
        String desEncrypted = encrypter.encrypt(secretString);
        String desDecrypted = encrypter.decrypt(desEncrypted);

        Assertions.assertEquals(secretString, desDecrypted);
    }

    @Test
    public void testPassPhrase2() {
        StringEncrypter encrypter = new StringEncrypter(passPhrase);
        String desEncrypted = encrypter.encrypt(secretString2);
        String desDecrypted = encrypter.decrypt(desEncrypted);

        Assertions.assertEquals(secretString2, desDecrypted);
    }

    @Test
    public void testExtraChars() {
        String test1 = " ~!@#$%^& *()_+=`| }{[]\\;: \"?><,./ ";
        StringEncrypter encrypter = new StringEncrypter(passPhrase);
        String desEncrypted = encrypter.encrypt(test1);
        String desDecrypted = encrypter.decrypt(desEncrypted);

        Assertions.assertEquals(test1, desDecrypted);

    }
}
