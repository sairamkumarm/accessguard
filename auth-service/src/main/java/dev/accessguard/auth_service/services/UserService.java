package dev.accessguard.auth_service.services;

import dev.accessguard.auth_service.Repositories.RoleRepository;
import dev.accessguard.auth_service.Repositories.TenantRepository;
import dev.accessguard.auth_service.Repositories.UserRepository;
import dev.accessguard.auth_service.models.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TenantRepository tenantRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO registerUser(UserCreationDTO userCreationDTO, String tenantName){
        UserEntity user = new UserEntity();
        user.setUserName(userCreationDTO.getUserName());
        user.setUserEmail(userCreationDTO.getUserEmail());
        user.setUserPasswordHash(passwordEncoder.encode(userCreationDTO.getRawUserPassword()));
        user.setUserPhone(userCreationDTO.getUserPhone());
        Set<RoleEntity> roleEntitySet = new HashSet<>();
        for(String role: userCreationDTO.getUserRoles()){
            Optional<RoleEntity> roleEntity = roleRepository.findByRoleName(role);
            if (roleEntity.isPresent()) {
                roleEntitySet.add(roleEntity.get());
                continue;
            }
            throw new RuntimeException("Attempted to assign invalid Role: " + role);
        }
        user.setUserRoles(roleEntitySet);
        TenantEntity tenant = tenantRepository.findByTenantName(tenantName).orElseThrow(() -> new RuntimeException("Attempted to assign user to invalid tenant: " + tenantName));
        user.setTenantEntity(tenant);
        userRepository.save(user);
        return userEntityToResponseDTO(user);
    }

    private UserResponseDTO userEntityToResponseDTO(UserEntity user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserName(user.getUserName());
        dto.setUserEmail(user.getUserEmail());
        dto.setUserPhone(user.getUserPhone());

        Set<String> roles = user.getUserRoles()
                .stream()
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());

        dto.setUserRoles(roles);
        return dto;
    }

}
