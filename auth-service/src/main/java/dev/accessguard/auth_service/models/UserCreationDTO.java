package dev.accessguard.auth_service.models;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserCreationDTO {
    private String userName;
    private String userEmail;
    private String userPhone;
    private Set<String> userRoles;
    private String rawUserPassword;
}
