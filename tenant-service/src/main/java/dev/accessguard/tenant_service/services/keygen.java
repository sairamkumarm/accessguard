package dev.accessguard.tenant_service.services;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class keygen {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        SecretKey key = generateKey(256);
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    }

    public static SecretKey generateKey(int bitLength) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(bitLength);
        return keyGen.generateKey();
    }
}
