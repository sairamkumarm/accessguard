package dev.accessguard.auth_service.models;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String userName;
    private String rawPassword;
}
