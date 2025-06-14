package dev.accessguard.tenant_service.services;

import crypto.Encryption;
import org.bouncycastle.jcajce.provider.asymmetric.RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RSAKeyGenerator {

    private final SecretKey masterKey;

    public RSAKeyGenerator(SecretKey masterKey){
        this.masterKey = masterKey;
    }

    public Map<String, String> generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        KeyPair keyPair= keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        GCMParameterSpec iv = Encryption.generateIv();

        String rawPrivateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String encryptedPrivateKey = Encryption.encrypt(rawPrivateKey,masterKey, iv);
        String ivBase64 = Base64.getEncoder().encodeToString(iv.getIV());
        Map<String, String> keys = new HashMap<>();
        keys.put("publicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        keys.put("encPrivateKey", encryptedPrivateKey);
        keys.put("iv",ivBase64);
        return keys;
    }
}
