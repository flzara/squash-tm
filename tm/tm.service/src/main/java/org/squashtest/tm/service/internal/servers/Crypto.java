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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.*;
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
 *     Since we wrap here every parameters needed for encryption (except the passphrase used to generate the private key),
 *     this crypto also publishes a version number that should be stored along. It will help detect obsolete encryption
 *     methods if one day we move on to another encryption policy.
 * </p>
 *
 * <p>
 *     As of version 1, the specs are the following :
 *     <ul>
 *         <li>secret key generated by stretching the passphrase with PBKDF2WithHmacSHA1</li>
 *         <li>encryption with AES/CBC/PKCS5Padding</li>
 *         <li>iteration count is 65556</li>
 *         <li>key length is 128 bytes</li>
 *         <li>salt size is 8 bytes</li>
 *     </ul>
 * </p>
 * 
 * <p>
 * 	<h4>Q/A</h4>
 *  <b>Q: My goodness, SHA-1 ?</b>
 *  <p>R: according to OWASP, "[if error]..., replace PBKDF2WithHmacSHA512 with PBKDF2WithHmacSHA1. Both are adequate to the task 
 *  but you may be criticized when people see "SHA1" in the specification (SHA1 can be unsafe outside of the context of PBKDF2)."
 *  Source: https://www.owasp.org/index.php/Hashing_Java
 *  </p>
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

    // TODO : not fixing what is not broken yet, but I think Latin-1 is what we want since the server is using it. 
    private static final String CHARSET = "UTF-8";

    private char[] keyPassword;


    public Crypto(char[] keyPassword){
    	if(keyPassword == null) {
    		this.keyPassword = null;
		} else {
			this.keyPassword = Arrays.copyOf(keyPassword, keyPassword.length);
		}
    }

    public int getVersion(){
        return VERSION;
    }


    EncryptionOutcome encrypt(String toEncrypt) throws GeneralSecurityException, UnsupportedEncodingException{

        byte[] salt = generateSalt();

        SecretKeySpec keySpec = generateKeySpec(keyPassword, salt);
        Cipher cipher = createEncryptionCipher(keySpec);

        byte[] iv = getIV(cipher);

        byte[] encrypted = doEncrypt(cipher, toEncrypt);

        byte[] finalBytes = appendEverything(salt, iv, encrypted);

        String encoded = Base64.getEncoder().encodeToString(finalBytes);

        return new EncryptionOutcome(encoded, VERSION);

    }

    String decrypt(String encrypted) throws GeneralSecurityException, UnsupportedEncodingException{

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

    private SecretKeySpec generateKeySpec(char[] pwd, byte[] salt) throws GeneralSecurityException{
        
        PBEKeySpec keySpec = new PBEKeySpec(pwd, salt, ITER_CNT, KEY_LEN);

        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_GEN);
        SecretKey secKey = keyFactory.generateSecret(keySpec);

        return new SecretKeySpec(secKey.getEncoded(), KEY_ALG);
        
    }


    private Cipher createPlainCipher() throws GeneralSecurityException{        
        return Cipher.getInstance(CRYPT_ALG);      
    }



    // ***************** private encryption code ***************************



    private byte[] generateSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return salt;
    }

    private Cipher createEncryptionCipher(SecretKeySpec keySpec) throws GeneralSecurityException{
        Cipher cipher = null;
        cipher = Cipher.getInstance(CRYPT_ALG);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher;
    }


    private final byte[] getIV(Cipher cipher) throws GeneralSecurityException{
       
        AlgorithmParameters params = cipher.getParameters();
        return params.getParameterSpec(IvParameterSpec.class).getIV();
        
    }

    private byte[] doEncrypt(Cipher cipher, String text) throws UnsupportedEncodingException, GeneralSecurityException{       
        return cipher.doFinal(text.getBytes(CHARSET));        
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

    private void initCipher(Cipher cipher, SecretKeySpec key, byte[] iv) throws GeneralSecurityException{        
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
    }

    private byte[] doDecrypt(Cipher cipher, byte[] encrypted) throws GeneralSecurityException{
        return cipher.doFinal(encrypted);
    }

    private String decryptedToString(byte[] decrypted) throws UnsupportedEncodingException{
        return new String(decrypted, CHARSET);
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
        	this.salt = copyArrayOrReturnNull(salt);
        	this.iv = copyArrayOrReturnNull(iv);
        	this.encryptedBytes = copyArrayOrReturnNull(encryptedBytes);
        }

		/**
		 * Same method as java.util.Arrays#copyof,
		 * but returns null if array parameter is null. */
		private byte[] copyArrayOrReturnNull(byte[] arrayToCopy) {
			if(arrayToCopy == null) {
				return null;
			} else {
				return Arrays.copyOf(arrayToCopy, arrayToCopy.length);
			}
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
