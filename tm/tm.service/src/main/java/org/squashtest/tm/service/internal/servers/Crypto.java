/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.servers;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.squashtest.tm.service.servers.EncryptionKeyChangedException;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * <p>
 * That class implements basic AES 128 encryption because java doesn't support AES 256 out of the box, and we don't
 * want to have our users install JCE or enable crypto.policy=unlimited if they don't want to (simplicity of installation
 * is a strong requirement for this application). The result is returned as a Base 64 encoded String.
 * </p>
 *
 * <p>
 * The method chose here is based on https://medium.com/@danojadias/aes-256bit-encryption-decryption-and-storing-in-the-database-using-java-2ada3f2a0b14. Here we store the salt and the initialization vector with the content itself, which elimitates the need
 * for storing them in different columns in the database.
 * </p>
 *
 * <p>
 *     Since we wrap here every parameters needed for encryption (safe from the password used to generate the private key),
 *     this crypto also publishes a version number that should be stored along. It will help detect obsolete encryption
 *     methods if one day we move on to another encryption policy.
 * </p>
 *
 * <p>
 *     As of version 1, the specs are the following :
 *     <ul>
 *         <li>secret key generated with PBKDF2WithHmacSHA1</li>
 *         <li>encryption with AES/CBC/PKCS5Padding</li>
 *         <li>key length is 128 bits</li>
 *         <li>salt size is 8 bytes</li>
 *     </ul>
 * </p>
 *
 */
class Crypto {

    public static final int VERSION = 1;
    private static final String KEY_GEN = "PBKDF2WithHmacSHA1";
    private static final String KEY_ALG = "AES";
    private static final String CRYPT_ALG = "AES/CBC/PKCS5Padding";
    private static final int SALT_SIZE = 8;

    private static final int ITER_CNT = 65556;
    private static final int KEY_LEN = 128;

    private static final String CHARSET = "UTF-8";

    private char[] keyPassword;


    public Crypto(char[] keyPassword){
        this.keyPassword = keyPassword;
    }

    public int getVersion(){
        return VERSION;
    }


    EncryptionOutcome encrypt(String toEncrypt){

        byte[] salt = generateSalt();

        SecretKeySpec keySpec = generateKeySpec(keyPassword, salt);
        Cipher cipher = createEncryptionCipher(keySpec);

        byte[] iv = getIV(cipher);

        byte[] encrypted = doEncrypt(cipher, toEncrypt);

        byte[] finalBytes = appendEverything(salt, iv, encrypted);

        String encoded = Base64.getEncoder().encodeToString(finalBytes);

        return new EncryptionOutcome(encoded, VERSION);

    }

    String decrypt(String encrypted){

        Cipher cipher = createPlainCipher();

        EncryptionBytes encBytes = chunkEncryption(cipher, encrypted);

        SecretKeySpec keySpec = generateKeySpec(keyPassword, encBytes.getSalt());

        initCipher(cipher, keySpec, encBytes.getIv());

        byte[] decrypted = doDecrypt(cipher, encBytes.getEncryptedBytes());

        return decryptedToString(decrypted);

    }

	/**
	 * Should be invoked after use. Voids the password from memory.
	 */
	void dispose(){
		Arrays.fill(keyPassword, '\0');
	}


    // *************** all purpose private methods **************************************

    private SecretKeySpec generateKeySpec(char[] pwd, byte[] salt){
        try {
            PBEKeySpec keySpec = new PBEKeySpec(pwd, salt, ITER_CNT, KEY_LEN);

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_GEN);
            SecretKey secKey = keyFactory.generateSecret(keySpec);

            return new SecretKeySpec(secKey.getEncoded(), KEY_ALG);
        }
        catch(NoSuchAlgorithmException ex){
            // ouch, I so believed this algorithm was a standard honored by any JRE implementor
            throw new RuntimeException(ex);
        }
        catch(InvalidKeySpecException ex){
            // ouch, I so believed my key spec was a standard honored by any JRE implementor
            throw new RuntimeException(ex);
        }
    }


    private Cipher createPlainCipher(){
        try {
            return Cipher.getInstance(CRYPT_ALG);
        } catch (NoSuchAlgorithmException ex) {
            // ouch, I so believed this algorithm was a standard honored by any JRE implementor
            throw new RuntimeException(ex);
        } catch (NoSuchPaddingException ex) {
            // ouch, I so believed this padding was a standard honored by any JRE implementor
            throw new RuntimeException(ex);
        }
    }



    // ***************** private encryption code ***************************



    private byte[] generateSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return salt;
    }

    private Cipher createEncryptionCipher(SecretKeySpec keySpec){
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException  ex) {
            // ouch, I so believed I was using standard algorithms, padding and key spec honored by any JRE implementor
            throw new RuntimeException(ex);
        }
    }


    private final byte[] getIV(Cipher cipher){
        try {
            AlgorithmParameters params = cipher.getParameters();
            return params.getParameterSpec(IvParameterSpec.class).getIV();
        }
        catch(InvalidParameterSpecException ex){
            // ouch, I so  believed this parameter spec was a standard honored by any JRE implementor
            throw new RuntimeException(ex);
        }
    }

    private byte[] doEncrypt(Cipher cipher, String text){
        try {
            return cipher.doFinal(text.getBytes(CHARSET));
        }
        catch(UnsupportedEncodingException ex){
            // ouch, I so  believed UTF-8 was a standard honored by any JRE implementor
            throw new RuntimeException(ex);
        }
        catch(IllegalBlockSizeException | BadPaddingException ex){
            // ouch, I so  believed that my block size and padding were honored by any JRE implementor
            throw new RuntimeException(ex);
        }
    }

    private byte[] appendEverything(byte[] salt, byte[] iv, byte[] encrypted){
        byte[] finalBytes = new byte[salt.length + iv.length + encrypted.length];
        ByteBuffer buffer = ByteBuffer.wrap(finalBytes);

        buffer.put(salt);
        buffer.put(iv);
        buffer.put(encrypted);

        return finalBytes;

    }


    // ***************** private decryption code ***************************


    private EncryptionBytes chunkEncryption(Cipher cipher, String base64Encrypted){
        byte[] allBytes = Base64.getDecoder().decode(base64Encrypted);

        ByteBuffer buffer = ByteBuffer.wrap(allBytes);

        byte[] salt = new byte[SALT_SIZE];
        byte[] iv = new byte[cipher.getBlockSize()];
        byte[] encrypted = new byte[buffer.capacity() - salt.length - iv.length];

        buffer.get(salt, 0, salt.length);
        buffer.get(iv, 0, iv.length);
        buffer.get(encrypted);

        return new EncryptionBytes(salt, iv, encrypted);

    }

    private void initCipher(Cipher cipher, SecretKeySpec key, byte[] iv){
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        } catch (InvalidKeyException ex) {
            // ouch, I so believed my key spec was a standard honored by any JRE implementor
            throw new RuntimeException(ex);
        } catch (InvalidAlgorithmParameterException ex) {
            // ouch, I so believed that initialization vector was a standard parameter honored by any JRE implementor
            throw new RuntimeException(ex);
        }
    }

    private byte[] doDecrypt(Cipher cipher, byte[] encrypted){
        try {
            return cipher.doFinal(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            throw new EncryptionKeyChangedException();
        }
    }

    private String decryptedToString(byte[] decrypted){
        try {
            return new String(decrypted, CHARSET);
        } catch (UnsupportedEncodingException ex) {
            // ouch, I so  believed UTF-8 was a standard honored by any JRE implementor
            throw new RuntimeException(ex);
        }
    }

    // *********************** inner classes *******************************


    static final class EncryptionOutcome{
        private String encryptedText;
        private int version;

        EncryptionOutcome(String encryptedText, int version) {
            this.encryptedText = encryptedText;
            this.version = version;
        }

        String getEncryptedText() {
            return encryptedText;
        }

        int getVersion() {
            return version;
        }
    }

    static final class EncryptionBytes{
        private byte[] salt;
        private byte[] iv;
        private byte[] encryptedBytes;

        EncryptionBytes(byte[] salt, byte[] iv, byte[] encryptedBytes) {
            this.salt = salt;
            this.iv = iv;
            this.encryptedBytes = encryptedBytes;
        }

        byte[] getSalt() {
            return salt;
        }

        byte[] getIv() {
            return iv;
        }


        byte[] getEncryptedBytes() {
            return encryptedBytes;
        }
    }

}
