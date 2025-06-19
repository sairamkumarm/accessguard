package dev.accessguard.role_service.models;

import lombok.Data;

@Data
public class RoleRequestDTO {
    private String roleName;
    private String roleDescription;
}
