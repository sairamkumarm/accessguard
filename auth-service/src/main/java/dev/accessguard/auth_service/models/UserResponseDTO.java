package dev.accessguard.auth_service.models;

import lombok.Data;

import java.util.Set;

@Data
public class UserResponseDTO {
    private String userName;
    private String userEmail;
    private String userPhone;
    private Set<String> userRoles;
}
