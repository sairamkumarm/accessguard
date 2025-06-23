package dev.accessguard.auth_service.services;

import annotations.TrackUsage;
import dev.accessguard.auth_service.Repositories.RoleRepository;
import dev.accessguard.auth_service.Repositories.TenantRepository;
import dev.accessguard.auth_service.Repositories.UserRepository;
import dev.accessguard.auth_service.models.*;
import models.UsageEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {


    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public UserService( UserRepository userRepository, TenantRepository tenantRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JWTService jwtService) {

        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @TrackUsage(eventType = "USER_REGISTERED")
    public UserResponseDTO registerUser(UserCreationDTO userCreationDTO, String tenantName){
        UserEntity user = new UserEntity();
        user.setUserName(userCreationDTO.getUserName());
        user.setUserEmail(userCreationDTO.getUserEmail());
        user.setUserPasswordHash(passwordEncoder.encode(userCreationDTO.getRawUserPassword()));
        user.setUserPhone(userCreationDTO.getUserPhone());
        Set<RoleEntity> roleEntitySet = new HashSet<>();
        for(String role: userCreationDTO.getUserRoles()){
            Optional<RoleEntity> roleEntity = roleRepository.findByRoleNameAndTenantEntity_TenantName(role,tenantName);
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

    @TrackUsage(eventType = "USER_QUERIED")
    public UserResponseDTO getUserByUsernameAndTenant(String tenantName, String userName) {
        UserEntity user = userRepository.findByUserNameAndTenantEntity_TenantName(userName,tenantName)
                .orElseThrow(() -> new RuntimeException("User not found: " + userName));

        if (!user.getTenantEntity().getTenantName().equals(tenantName)) {
            throw new RuntimeException("User does not belong to tenant: " + tenantName);
        }

        return userEntityToResponseDTO(user);
    }

    @TrackUsage(eventType = "USER_UPDATED")
    public UserResponseDTO updateUser(String tenantName, UserUpdateDTO updateDTO) {
        UserEntity user = userRepository.findByUserNameAndTenantEntity_TenantName(updateDTO.getUserName(),tenantName)
                .orElseThrow(() -> new RuntimeException("User not found: " + updateDTO.getUserName()));

        if (!user.getTenantEntity().getTenantName().equals(tenantName)) {
            throw new RuntimeException("User does not belong to tenant: " + tenantName);
        }

        if (updateDTO.getUserEmail() != null)
            user.setUserEmail(updateDTO.getUserEmail());
        if (updateDTO.getUserPhone() != null)
            user.setUserPhone(updateDTO.getUserPhone());
        if (updateDTO.getRawUserPassword() != null) {
            user.setUserPasswordHash(passwordEncoder.encode(updateDTO.getRawUserPassword()));
            user.setUserPasswordLastUpdatedAt(java.time.LocalDateTime.now());
        }
        if (updateDTO.getUserRoles() != null) {
            Set<RoleEntity> newRoles = updateDTO.getUserRoles()
                    .stream()
                    .map(role -> roleRepository.findByRoleName(role)
                            .orElseThrow(() -> new RuntimeException("Invalid role: " + role)))
                    .collect(Collectors.toSet());
            user.setUserRoles(newRoles);
        }

        userRepository.save(user);
        return userEntityToResponseDTO(user);
    }

    @TrackUsage(eventType = "USER_DELETED")
    public void deleteUser(String tenantName, String userName) {

        UserEntity user = userRepository.findByUserNameAndTenantEntity_TenantName(userName,tenantName)
                .orElseThrow(() -> new RuntimeException("User not found: " + userName));

        if (!user.getTenantEntity().getTenantName().equals(tenantName)) {
            throw new RuntimeException("User does not belong to tenant: " + tenantName);
        }

        userRepository.delete(user);
    }

    @TrackUsage(eventType = "USER_LOGIN")
    public Map<String, Object> loginUser(String tenantName, UserLoginDTO userLoginDTO){
        UserEntity user = userRepository.findByUserNameAndTenantEntity_TenantName(userLoginDTO.getUserName(),tenantName)
                .orElseThrow(() -> new RuntimeException("User not found: " + userLoginDTO.getUserName()));

        if (!user.getTenantEntity().getTenantName().equals(tenantName)) {
            throw new RuntimeException("User does not belong to tenant: " + tenantName);
        }
        if (!passwordEncoder.matches(userLoginDTO.getRawPassword(), user.getUserPasswordHash())){
            throw new RuntimeException("Invalid Credentials");
        }
        Long exp = (long) (60 * 60 * 6);
        String jwt = jwtService.generateJWT(userLoginDTO.getUserName(),user.rolesToStringSet(),tenantName,exp);
        Map<String, Object> res = new HashMap<>();
        res.put("username",userLoginDTO.getUserName());
        res.put("jwt",jwt);
        return res;
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
