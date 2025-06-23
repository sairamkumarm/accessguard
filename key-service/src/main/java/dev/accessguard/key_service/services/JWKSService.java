package dev.accessguard.key_service.services;

import JWKS.JWKSUtil;
import annotations.TrackUsage;
import dev.accessguard.key_service.data.TenantEntity;
import dev.accessguard.key_service.data.TenantRepository;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
public class JWKSService {

    @TrackUsage(eventType = "PUBLIC-KEY-QUERIED")
    public Map<String, Object> getTenantJWKS(TenantEntity tenant){
        try {
            byte[] keyBytes = Base64.getDecoder().decode(tenant.getPublicKey());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(keySpec);
            RSAPublicKey rsaKey = (RSAPublicKey) pk;

            return JWKSUtil.createJWK(tenant.getTenantName() + "-key", rsaKey);

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert public key for tenant: " + tenant.getTenantName(), e);
        }
    }
}
