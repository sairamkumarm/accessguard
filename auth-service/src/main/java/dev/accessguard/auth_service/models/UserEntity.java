package dev.accessguard.auth_service.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String userPhone;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> userRoles = new HashSet<>();

    @Column(nullable = false)
    private String userPasswordHash;

    @Column(nullable = false)
    private LocalDateTime userCreatedAt;

    @Column(nullable = false)
    private LocalDateTime userPasswordLastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenantEntity;

    @PrePersist
    public void fillCreated(){
        this.userCreatedAt = LocalDateTime.now();
        this.userPasswordLastUpdatedAt = LocalDateTime.now();
    }

    public UserEntity(){}

}
