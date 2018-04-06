package edu.temple.nfc;

import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class RSA {

    private static final String DEFAULT_ALGORITHM = "RSA";
    private static final String DEFAULT_PROVIDER = "SHA1PRNG";
    private static Cipher encryptCipher, decryptCipher;

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(DEFAULT_ALGORITHM);
        SecureRandom random = SecureRandom.getInstance(DEFAULT_PROVIDER);
        keyGen.initialize(2048, random);
        return keyGen.generateKeyPair();
    }

    public static PublicKey convertPublicKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Log.i(Constants.LOG_TAG, "Attempting to convert public key string:\n" + keyString);
        byte[] codedKeyArray = decodeString(keyString);
        KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_ALGORITHM);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(codedKeyArray);
        PublicKey key = keyFactory.generatePublic(publicKeySpec);
        return key;
    }

    public static PrivateKey convertPrivateKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Log.i(Constants.LOG_TAG, "Attempting to convert private key string:\n" + keyString);
        byte[] codedKeyArray = decodeString(keyString);
        KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_ALGORITHM);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(codedKeyArray);
        return keyFactory.generatePrivate(privateKeySpec);
    }

    public static byte[] decodeString(String message) {
        return Base64.decode(message, KeysContract.DEFAULT_ENCODING);
    }

    public static String encodeByteArray(byte[] message) {
        return Base64.encodeToString(message, KeysContract.DEFAULT_ENCODING);
    }

    public static byte[] encrypt(String keyString, String message) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        PrivateKey key = convertPrivateKey(keyString);
        return encrypt(key, message);
    }

    private static byte[] encrypt(PrivateKey key, String message) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        encryptCipher = Cipher.getInstance(DEFAULT_ALGORITHM);
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        return encryptCipher.doFinal(message.getBytes());
    }

    public static String decrypt(String keyString, byte[] message) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        PublicKey key = convertPublicKey(keyString);
        return decrypt(key, message);
    }

    private static String decrypt(PublicKey key, byte[] message) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        decryptCipher = Cipher.getInstance(DEFAULT_ALGORITHM);
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
        return new String(decryptCipher.doFinal(message));
    }

    // PEM Manager

    private static final String BEGIN_STRING = "-----BEGIN PUBLIC KEY-----\n";
    private static final String END_STRING = "\n-----END PUBLIC KEY-----";

    public static String convertStringToPEM(String pkString) {
        return (BEGIN_STRING + pkString.trim() + END_STRING);
    }

    public static String convertPEMToKeyString(String pem) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return pem.replace(BEGIN_STRING, "").replace(END_STRING, "");
    }

    public static boolean isPEM(String pem) {
        return (pem.startsWith(BEGIN_STRING) && pem.endsWith(END_STRING));
    }
}