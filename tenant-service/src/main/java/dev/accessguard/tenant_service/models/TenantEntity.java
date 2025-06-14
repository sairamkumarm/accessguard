package dev.accessguard.tenant_service.models;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class TenantEntity {

    @Id
    UUID tenantID;

    @Column(nullable = false)
    String tenantName;

    @Column(nullable = false)
    String apiKeyHash;

    @Column(nullable = false)
    String publicKey;

    @Column(nullable = false)
    String encPrivateKey;

    @Column(nullable = false)
    String iv;

    @Column(nullable = false, updatable = false)
    LocalDateTime lastUpdatedAt;

    public TenantEntity(){}

    @PreUpdate
    @PrePersist
    protected void onCreate(){
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
