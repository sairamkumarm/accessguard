package dev.accessguard.role_service.services;

import annotations.TrackUsage;
import dev.accessguard.role_service.models.RoleRequestDTO;
import dev.accessguard.role_service.models.RoleResponseDTO;
import dev.accessguard.role_service.repositories.RoleRepository;
import dev.accessguard.role_service.models.RoleEntity;
import dev.accessguard.role_service.models.TenantEntity;
import dev.accessguard.role_service.repositories.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import logging.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;

    public RoleService(RoleRepository roleRepository, TenantRepository tenantRepository) {
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
    }

    @TrackUsage(eventType = "ROLE-CREATED")
    public RoleResponseDTO createRole(String tenantName, RoleRequestDTO dto) {
        TenantEntity tenant = tenantRepository.findByTenantName(tenantName)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        RoleEntity role = new RoleEntity();
        role.setRoleName(dto.getRoleName());
        role.setRoleDescription(dto.getRoleDescription());
        role.setTenantEntity(tenant);
        RoleEntity saved = roleRepository.save(role);
        return toDTO(saved);
    }

    @TrackUsage(eventType = "ROLES-QUERIED")
    public List<RoleResponseDTO> getAllRolesForTenant(String tenantName) {
        return roleRepository.findAll().stream()
                .filter(role -> role.getTenantEntity().getTenantName().equals(tenantName))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @TrackUsage(eventType = "ROLES-UPDATED")
    @Transactional
    public RoleResponseDTO updateRole(String tenantName, RoleRequestDTO dto) {
        RoleEntity role = roleRepository.findByRoleName(dto.getRoleName())
                .filter(r -> r.getTenantEntity().getTenantName().equals(tenantName))
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        role.setRoleName(dto.getRoleName());
        role.setRoleDescription(dto.getRoleDescription());
        return toDTO(role);
    }
    @TrackUsage(eventType = "ROLES-DELETED")
    public void deleteRole(String tenantName, String roleName) {
        RoleEntity role = roleRepository.findByRoleName(roleName)
                .filter(r -> r.getTenantEntity().getTenantName().equals(tenantName))
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        roleRepository.delete(role);
    }

    private RoleResponseDTO toDTO(RoleEntity role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setRoleName(role.getRoleName());
        dto.setRoleDescription(role.getRoleDescription());
        return dto;
    }
}