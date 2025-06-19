package dev.accessguard.tenant_service.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
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

        byte[] tenantBytes = uuidToBytes(tenantID);
        for (int i=0; i<tenantBytes.length; i++){
            randomBytes[i] ^= tenantBytes[i];
        }

        String raw_key = "agk_v1_" + encoder.encodeToString(randomBytes);
        res.put("rawAPIKey", raw_key);
        res.put("hashedAPIKey", passwordEncoder.encode(raw_key));
        return res;
    }
    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
}
