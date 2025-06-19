package dev.accessguard.auth_service.Repositories;

import dev.accessguard.auth_service.models.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {
    Optional<TenantEntity> findByTenantName(String tenantName);
    void deleteByTenantName(String tenantName);
}