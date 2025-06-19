package dev.accessguard.auth_service.models;

import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateDTO {
    private String userName;
    private String userEmail;
    private String userPhone;
    private String rawUserPassword;
    private Set<String> userRoles;
}