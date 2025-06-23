package dev.accessguard.key_service.controllers;

import dev.accessguard.key_service.data.TenantEntity;
import dev.accessguard.key_service.data.TenantRepository;
import dev.accessguard.key_service.services.JWKSService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(".well-known")
public class JWKSController {

    private final TenantRepository tenantRepository;
    private final JWKSService jwksService;

    public JWKSController(TenantRepository tenantRepository, JWKSService jwksService) {
        this.tenantRepository = tenantRepository;
        this.jwksService = jwksService;
    }

    @GetMapping("/jwks.json")
    public ResponseEntity<?> getJWKS(@RequestHeader("X-TENANT-NAME") String tenantName) {
        Optional<TenantEntity> tenantOpt = tenantRepository.findByTenantName(tenantName);

        if (tenantOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid tenant : "+tenantName);
        }

        try {
            Map<String, Object> jwk = jwksService.getTenantJWKS(tenantOpt.get());
            return ResponseEntity.ok(Map.of("keys", List.of(jwk)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to build JWKS");
        }
    }
}
