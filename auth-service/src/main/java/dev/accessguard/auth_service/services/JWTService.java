package dev.accessguard.auth_service.services;

import crypto.Encryption;
import dev.accessguard.auth_service.Repositories.TenantRepository;
import dev.accessguard.auth_service.models.TenantEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@Service
public class JWTService {
    private final SecretKey masterKey;
    private final TenantRepository tenantRepository;

    public JWTService(SecretKey masterKey, TenantRepository tenantRepository) {
        this.masterKey = masterKey;
        this.tenantRepository = tenantRepository;
    }

    private PrivateKey parseRSAPrivateKeyFromPem(String pem) {
        try {
            String cleanPem = pem
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(cleanPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Invalid RSA Private Key format", e);
        }
    }

    public String generateJWT(String userName, Set<String> userRoles, String tenantName, Long Expiration){
        try {
            TenantEntity tenant = tenantRepository.findByTenantName(tenantName).orElseThrow(()-> new RuntimeException("TenantName invalid: "+tenantName));
            String privateKeyRaw = tenant.getEncPrivateKey();
            String ivBase64 = tenant.getIv();
            GCMParameterSpec iv = new GCMParameterSpec(128,Base64.getDecoder().decode(ivBase64));
            String pem = Encryption.decrypt(privateKeyRaw,masterKey,iv);
            PrivateKey privateKey = parseRSAPrivateKeyFromPem(pem);
            Map<String, Object> roles = new HashMap<>();
            roles.put("roles",userRoles);
            return Jwts.builder()
                    .setSubject(userName)
                    .addClaims(roles)
                    .setIssuer(tenantName)
                    .setIssuedAt(new Date())
                    .setHeaderParam("kid",tenantName+"-key")
                    .setExpiration(new Date(System.currentTimeMillis() + Expiration))
                    .signWith(privateKey,SignatureAlgorithm.RS256).compact();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
