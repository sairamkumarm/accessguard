package dev.accessguard.key_service.data;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
public class TenantEntity {

    @Id
    UUID tenantID;

    @Column(nullable = false)
    String tenantName;

    @Column(nullable = false, length = 512)
    String apiKeyHash;

    @Column(nullable = false, length = 4096)
    String publicKey;

    @Column(nullable = false, length = 4096)
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
    @Override
    public String toString() {
        return "TenantEntity {\n" +
                "  tenantID       = " + tenantID + "\n" +
                "  tenantName     = '" + tenantName + "'\n" +
                "  tenantName.len = " + (tenantName != null ? tenantName.length() : 0) + "\n" +
                "  apiKeyHash     = '" + apiKeyHash + "'\n" +
                "  apiKeyHash.len = " + (apiKeyHash != null ? apiKeyHash.length() : 0) + "\n" +
                "  publicKey      = '" + publicKey + "'\n" +
                "  publicKey.len  = " + (publicKey != null ? publicKey.length() : 0) + "\n" +
                "  encPrivateKey  = '" + encPrivateKey + "'\n" +
                "  encPrivateKey.len = " + (encPrivateKey != null ? encPrivateKey.length() : 0) + "\n" +
                "  iv             = '" + iv + "'\n" +
                "  iv.len         = " + (iv != null ? iv.length() : 0) + "\n" +
                "  lastUpdatedAt  = " + lastUpdatedAt + "\n" +
                "}";
    }

}
