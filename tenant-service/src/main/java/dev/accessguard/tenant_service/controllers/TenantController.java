package dev.accessguard.tenant_service.controllers;

import dev.accessguard.tenant_service.models.TenantCreatedDTO;
import dev.accessguard.tenant_service.models.TenantNameDTO;
import dev.accessguard.tenant_service.models.TenantResponseDTO;
import dev.accessguard.tenant_service.services.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/v1/")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService){
        this.tenantService = tenantService;
    }

    @PostMapping("/register/{tenantName}")
    public ResponseEntity<?> createTenant(@PathVariable String tenantName){
        try {
            if(!tenantService.nameTaken(tenantName)){
                return ResponseEntity.ok(tenantService.createTenant(tenantName));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username Taken");
            }
        } catch (Exception e) {
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/get-tenant/{tenantName}")
    public ResponseEntity<TenantResponseDTO> getTenant(@PathVariable String tenantName){
        Optional<TenantResponseDTO> tenantResponseDTO = tenantService.getTenant(tenantName);
        return tenantResponseDTO.map(dto-> ResponseEntity.ok(dto)).orElseGet(()->ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/delete/{tenantName}")
    public ResponseEntity<String> deleteTenant(@PathVariable String tenantName) {
        try {
            String message = tenantService.deleteTenant(tenantName);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/regenerate-key/{tenantName}")
    public ResponseEntity<TenantCreatedDTO> regenerateApiKey(@PathVariable String tenantName) {
        Optional<TenantCreatedDTO> recreated = tenantService.recreateAPIKey(tenantName);
        return recreated
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
