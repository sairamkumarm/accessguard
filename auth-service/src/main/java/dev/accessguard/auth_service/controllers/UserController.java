package dev.accessguard.auth_service.controllers;

import dev.accessguard.auth_service.models.UserCreationDTO;
import dev.accessguard.auth_service.models.UserResponseDTO;
import dev.accessguard.auth_service.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<?> registerUser(@RequestHeader("X-TENANT-NAME") String tenantName, @RequestBody UserCreationDTO userCreationDTO){
        try {
            return ResponseEntity.ok(userService.registerUser(userCreationDTO, tenantName));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


}
