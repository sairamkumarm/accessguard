package dev.accessguard.auth_service.Repositories;

import dev.accessguard.auth_service.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    public Optional<UserEntity> findByUserName(String userName);
    Optional<UserEntity> findByUserNameAndTenantEntity_TenantName(String userName, String tenantName);
}
