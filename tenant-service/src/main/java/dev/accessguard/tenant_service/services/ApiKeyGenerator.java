package dev.accessguard.tenant_service.services;

import org.bouncycastle.util.encoders.UrlBase64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class ApiKeyGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getEncoder().withoutPadding();

    private final PasswordEncoder passwordEncoder;

    public ApiKeyGenerator(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, String> generateAPIKey(UUID tenantID){
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        Map<String, String> res = new HashMap<>();

        byte[] tenantBytes = tenantID.toString().getBytes();
        byte[] combined = new byte[randomBytes.length + tenantBytes.length];
        System.arraycopy(randomBytes,0,combined,0,randomBytes.length);
        System.arraycopy(tenantBytes,0,combined,randomBytes.length,tenantBytes.length);
        String raw_key = "agk_v1_" + encoder.encodeToString(combined);
        res.put("key", raw_key);
        res.put("hash", passwordEncoder.encode(raw_key));
        return res;
    }
}
