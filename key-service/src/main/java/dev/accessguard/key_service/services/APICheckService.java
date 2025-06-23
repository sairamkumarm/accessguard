package dev.accessguard.key_service.services;

import dev.accessguard.key_service.data.TenantRepository;
import dev.accessguard.key_service.data.TenantEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class APICheckService {

    private final TenantRepository tenantRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public APICheckService(TenantRepository repository
    ) {
        this.tenantRepository = repository;
    }

    public boolean checkAPIKeyExistence(String tenantName, String rawAPIKey) {
        Optional<TenantEntity> tenant = tenantRepository.findByTenantName(tenantName);
        System.out.println(tenant.isPresent() && passwordEncoder.matches(rawAPIKey, tenant.get().getApiKeyHash()));
        return tenant.isPresent() && passwordEncoder.matches(rawAPIKey, tenant.get().getApiKeyHash());
    }
}
