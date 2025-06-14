package dev.accessguard.tenant_service.Repositories;

import dev.accessguard.tenant_service.models.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {
    public Optional<TenantEntity> findByTenantName(String tenantName);
}