package dev.accessguard.auth_service.services;

import dev.accessguard.auth_service.Repositories.RoleRepository;
import dev.accessguard.auth_service.Repositories.TenantRepository;
import dev.accessguard.auth_service.Repositories.UserRepository;
import dev.accessguard.auth_service.models.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private final JWTService jwtService;

    public UserService(UserRepository userRepository, TenantRepository tenantRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JWTService jwtService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    public UserResponseDTO getUserByUsernameAndTenant(String tenantName, String userName) {
        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found: " + userName));

        if (!user.getTenantEntity().getTenantName().equals(tenantName)) {
            throw new RuntimeException("User does not belong to tenant: " + tenantName);
        }

        return userEntityToResponseDTO(user);
    }

    public UserResponseDTO updateUser(String tenantName, UserUpdateDTO updateDTO) {
        UserEntity user = userRepository.findByUserName(updateDTO.getUserName())
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

    public void deleteUser(String tenantName, String userName) {

        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found: " + userName));

        if (!user.getTenantEntity().getTenantName().equals(tenantName)) {
            throw new RuntimeException("User does not belong to tenant: " + tenantName);
        }

        userRepository.delete(user);
    }


    public String loginUser(String tenantName, UserLoginDTO userLoginDTO){
        UserEntity user = userRepository.findByUserName(userLoginDTO.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found: " + userLoginDTO.getUserName()));

        if (!user.getTenantEntity().getTenantName().equals(tenantName)) {
            throw new RuntimeException("User does not belong to tenant: " + tenantName);
        }
        if (!passwordEncoder.matches(userLoginDTO.getRawPassword(), user.getUserPasswordHash())){
            throw new RuntimeException("Invalid Credentials");
        }
        Long exp = (long) (60 * 60 * 6);
        return jwtService.generateJWT(userLoginDTO.getUserName(),user.rolesToStringSet(),tenantName,exp);
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
