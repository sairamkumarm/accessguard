package dev.accessguard.tenant_service.models;

import lombok.Data;

@Data
public class TenantResponseDTO {
    String tenantName;
    String publicKey;
}
