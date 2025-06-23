package dev.accessguard.tenant_service.services;

import annotations.TrackUsage;
import dev.accessguard.tenant_service.Repositories.TenantRepository;
import dev.accessguard.tenant_service.models.TenantCreatedDTO;
import dev.accessguard.tenant_service.models.TenantNameDTO;
import dev.accessguard.tenant_service.models.TenantResponseDTO;
import dev.accessguard.tenant_service.models.TenantEntity;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {
    private final ApiKeyGenerator apiKeyGenerator;
    private final TenantRepository tenantRepository;
    private final RSAKeyGenerator rsaKeyGenerator;

    public TenantService(ApiKeyGenerator apiKeyGenerator, TenantRepository tenantRepository, RSAKeyGenerator rsaKeyGenerator) {
        this.apiKeyGenerator = apiKeyGenerator;
        this.tenantRepository = tenantRepository;
        this.rsaKeyGenerator = rsaKeyGenerator;
    }


    public boolean nameTaken(String tenantName) {
        return tenantRepository.findByTenantName(tenantName).isPresent();
    }

    @TrackUsage(eventType = "TENANT-REGISTERED")
    public TenantCreatedDTO createTenant(String tenantName){
//        System.out.println(tenantName);

        try {
            TenantEntity tenant = new TenantEntity();
            TenantCreatedDTO res = new TenantCreatedDTO();

            UUID uuid = UUID.randomUUID();

            Map<String, String> APIKeys = apiKeyGenerator.generateAPIKey(uuid);
            Map<String, String> RSAKeys = rsaKeyGenerator.generateKeyPair();

            tenant.setTenantID(uuid);
            tenant.setTenantName(tenantName);
            tenant.setApiKeyHash(APIKeys.get("hashedAPIKey"));
            tenant.setPublicKey(RSAKeys.get("publicKey"));
            tenant.setEncPrivateKey(RSAKeys.get("encPrivateKey"));
            tenant.setIv(RSAKeys.get("iv"));

            System.out.println(tenant.toString());

            tenantRepository.save(tenant);

            res.setTenantName(tenantName);
            res.setRawAPIKey(APIKeys.get("rawAPIKey"));
            res.setPublicKey(RSAKeys.get("publicKey"));
            return res;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    @TrackUsage(eventType = "TENANT-QUERIED")
    public Optional<TenantResponseDTO> getTenant(String tenantName) {
        Optional<TenantEntity> tenant = tenantRepository.findByTenantName(tenantName);
        return tenant.map(this::tenantEntityToTenantDTO);
    }

    private TenantResponseDTO tenantEntityToTenantDTO(TenantEntity tenant) {
        TenantResponseDTO res = new TenantResponseDTO();
        res.setPublicKey(tenant.getPublicKey());
        res.setTenantName(tenant.getTenantName());
        return res;
    }

    @TrackUsage(eventType = "TENANT-DELETED")
    @Transactional
    public String deleteTenant(String tenantName) {
        try {
            tenantRepository.deleteByTenantName(tenantName);
            return tenantName + " deleted successfully";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @TrackUsage(eventType = "TENANT-API-KEY-ROTATED")
    public Optional<TenantCreatedDTO> recreateAPIKey(String tenantName) {
        Optional<TenantEntity> tenant = tenantRepository.findByTenantName(tenantName);
        TenantCreatedDTO tenantCreatedDTO = new TenantCreatedDTO();
        if (tenant.isPresent()) {
            Map<String, String> keys = apiKeyGenerator.generateAPIKey(tenant.get().getTenantID());
            tenantCreatedDTO.setTenantName(tenantName);
            tenantCreatedDTO.setRawAPIKey(keys.get("rawAPIKey"));
            tenantCreatedDTO.setPublicKey(tenant.get().getPublicKey());
            tenant.get().setApiKeyHash(keys.get("hashedAPIKey"));
            tenantRepository.save(tenant.get());
            return Optional.of(tenantCreatedDTO);
        } else {
            return Optional.empty();
        }
    }
}
