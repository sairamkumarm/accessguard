package dev.accessguard.role_service.controllers;

import dev.accessguard.role_service.models.RoleRequestDTO;
import dev.accessguard.role_service.models.RoleResponseDTO;
import dev.accessguard.role_service.services.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRole(@RequestHeader("X-TENANT-NAME") String tenantName,
                                        @RequestBody RoleRequestDTO dto) {
        try {
            RoleResponseDTO role = roleService.createRole(tenantName, dto);
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles(@RequestHeader("X-TENANT-NAME") String tenantName) {
        return ResponseEntity.ok(roleService.getAllRolesForTenant(tenantName));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateRole(@RequestHeader("X-TENANT-NAME") String tenantName,
                                        @RequestBody RoleRequestDTO dto) {
        try {
            RoleResponseDTO updated = roleService.updateRole(tenantName, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRole(@RequestHeader("X-TENANT-NAME") String tenantName,
                                        @RequestBody RoleRequestDTO dto) {
        try {
            roleService.deleteRole(tenantName, dto.getRoleName());
            return ResponseEntity.ok("Role deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}