// -----------------------------------------------------------------------------
// StringEncrypter.java
// -----------------------------------------------------------------------------

package artofillusion.spmanager;

// CIPHER / GENERATORS
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

// KEY SPECIFICATIONS
import java.security.spec.KeySpec;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEParameterSpec;

// EXCEPTIONS
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.NoSuchPaddingException;


/**
 *  -----------------------------------------------------------------------------
 *  The following example implements a class for encrypting and decrypting
 *  strings using several Cipher algorithms. The class is created with a key and
 *  can be used repeatedly to encrypt and decrypt strings using that key. Some
 *  of the more popular algorithms are: Blowfish DES DESede PBEWithMD5AndDES
 *  PBEWithMD5AndTripleDES TripleDES -----------------------------------------------------------------------------
 *
 *@author     not me ! See http://www.idevelopment.info/, which hase several
 *      java code examples
 *@created    23 mars 2004
 */

public class StringEncrypter
{

    private Cipher ecipher;
    private Cipher dcipher;

    // 8-bytes Salt
    private final byte[] salt = {
            (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
            (byte) 0x56, (byte) 0x34, (byte) 0xE3, (byte) 0x03
            };

    // Iteration count
    private final int iterationCount = 19;


    /**
     *  Constructor used to create this object. Responsible for setting and
     *  initializing this object's encrypter and decrypter Chipher instances
     *  given a Pass Phrase and algorithm.
     *
     *@param  passPhrase  Pass Phrase used to initialize both the encrypter and
     *      decrypter instances.
     */
    StringEncrypter( String passPhrase )
    {

        try
        {

            KeySpec keySpec = new PBEKeySpec( passPhrase.toCharArray(), salt, iterationCount );
            SecretKey key = SecretKeyFactory.getInstance( "PBEWithMD5AndDES" ).generateSecret( keySpec );

            ecipher = Cipher.getInstance( "PBEWithMD5AndDES" );
            // changed only this line of code, which went :
            // ecipher = Cipher.getInstance( key.getAlgorithm() ); and raised an exception
            dcipher = Cipher.getInstance( "PBEWithMD5AndDES" );

            // Prepare the parameters to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec( salt, iterationCount );

            ecipher.init( Cipher.ENCRYPT_MODE, key, paramSpec );
            dcipher.init( Cipher.DECRYPT_MODE, key, paramSpec );

        }
        catch ( InvalidAlgorithmParameterException e )
        {
            System.out.println( "EXCEPTION: InvalidAlgorithmParameterException" );
            e.printStackTrace();
        }
        catch ( InvalidKeySpecException e )
        {
            System.out.println( "EXCEPTION: InvalidKeySpecException" );
            e.printStackTrace();
        }
        catch ( NoSuchPaddingException e )
        {
            System.out.println( "EXCEPTION: NoSuchPaddingException" );
            e.printStackTrace();
        }
        catch ( NoSuchAlgorithmException e )
        {
            System.out.println( "EXCEPTION: NoSuchAlgorithmException" );
            e.printStackTrace();
        }
        catch ( InvalidKeyException e )
        {
            System.out.println( "EXCEPTION: InvalidKeyException" );
            e.printStackTrace();
        }
    }


    /**
     *  Takes a single String as an argument and returns an Encrypted version of
     *  that String.
     *
     *@param  str  String to be encrypted
     *@return      <code>String</code> Encrypted version of the provided String
     */
    public String encrypt( String str )
    {
        try
        {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes( "UTF8" );

            // Encrypt
            byte[] enc = ecipher.doFinal( utf8 );

            // Encode bytes to base64 to get a string
            return new sun.misc.BASE64Encoder().encode( enc );
        }
        catch (Exception e) {}
        return null;
    }


    /**
     *  Takes a encrypted String as an argument, decrypts and returns the
     *  decrypted String.
     *
     *@param  str  Encrypted String to be decrypted
     *@return      <code>String</code> Decrypted version of the provided String
     */
    public String decrypt( String str )
    {

        try
        {

            // Decode base64 to get bytes
            byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer( str );

            // Decrypt
            byte[] utf8 = dcipher.doFinal( dec );

            // Decode using utf-8
            return new String( utf8, "UTF8" );
        }
        catch (Exception e) {}
        return null;
    }

}