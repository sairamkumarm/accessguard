package dev.accessguard.key_service.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {
    Optional<TenantEntity> findByTenantName(String tenantName);
    void deleteByTenantName(String tenantName);
}