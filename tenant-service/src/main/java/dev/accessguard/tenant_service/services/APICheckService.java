package dev.accessguard.tenant_service.services;

import dev.accessguard.tenant_service.Repositories.TenantRepository;
import dev.accessguard.tenant_service.models.TenantEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class APICheckService {

    private final TenantRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public APICheckService(TenantRepository repository
    ) {
        this.repository = repository;
    }

    public boolean checkAPIKeyExistence(String tenantName, String rawAPIKey) {
        Optional<TenantEntity> tenant = repository.findByTenantName(tenantName);
        System.out.println(tenant.isPresent() && passwordEncoder.matches(rawAPIKey, tenant.get().getApiKeyHash()));
        return tenant.isPresent() && passwordEncoder.matches(rawAPIKey, tenant.get().getApiKeyHash());
    }
}
