package dev.accessguard.auth_service.controllers;

import dev.accessguard.auth_service.models.UserCreationDTO;
import dev.accessguard.auth_service.models.UserLoginDTO;
import dev.accessguard.auth_service.models.UserResponseDTO;
import dev.accessguard.auth_service.models.UserUpdateDTO;
import dev.accessguard.auth_service.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("getuser/{userName}")
    public ResponseEntity<?> getUser(
            @PathVariable String userName,
            @RequestHeader("X-TENANT-NAME") String tenantName
    ) {
        try {
            UserResponseDTO user = userService.getUserByUsernameAndTenant(tenantName, userName);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUser(
            @RequestBody UserUpdateDTO dto,
            @RequestHeader("X-TENANT-NAME") String tenantName
    ) {
        try {
            UserResponseDTO updatedUser = userService.updateUser(tenantName, dto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("delete/{userName}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String userName,
            @RequestHeader("X-TENANT-NAME") String tenantName
    ) {
        try {
            userService.deleteUser(tenantName, userName);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestHeader("X-TENANT-NAME") String tenantName, @RequestBody UserLoginDTO userLoginDTO){
        try {
            Map<String, Object> res = userService.loginUser(tenantName, userLoginDTO);
            return ResponseEntity.ok(res);
        } catch (Exception e){
            if (e.getMessage().startsWith("Invalid Credentials")){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        }
    }

}
