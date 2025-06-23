package dev.accessguard.auth_service.Repositories;

import dev.accessguard.auth_service.models.RoleEntity;
import dev.accessguard.auth_service.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    public Optional<RoleEntity> findByRoleName(String roleName);
    Optional<RoleEntity> findByRoleNameAndTenantEntity_TenantName(String userName, String tenantName);

}
