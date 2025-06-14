package dev.accessguard.tenant_service.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class CryptoConfig {

    @Value("${AWS_MASTER_KEY}")
    private String base64Key;

    @Bean
    public SecretKey masterKey() {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey,0,decodedKey.length,"AES");
    }
}
