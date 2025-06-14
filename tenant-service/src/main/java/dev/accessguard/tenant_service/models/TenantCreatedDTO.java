package dev.accessguard.tenant_service.models;

import lombok.Data;

@Data
public class TenantCreatedDTO {
    String tenantName;
    String rawAPIKey;
    String publicKey;
}
