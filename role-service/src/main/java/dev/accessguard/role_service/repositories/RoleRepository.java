package dev.accessguard.role_service.repositories;

import dev.accessguard.role_service.models.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    public Optional<RoleEntity> findByRoleName(String roleName);
}
