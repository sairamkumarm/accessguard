package dev.accessguard.tenant_service.services;

import dev.accessguard.tenant_service.Repositories.TenantRepository;
import dev.accessguard.tenant_service.models.TenantCreatedDTO;
import dev.accessguard.tenant_service.models.TenantEntity;
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
    public TenantService(ApiKeyGenerator apiKeyGenerator, TenantRepository tenantRepository, RSAKeyGenerator rsaKeyGenerator){
        this.apiKeyGenerator = apiKeyGenerator;
        this.tenantRepository = tenantRepository;
        this.rsaKeyGenerator = rsaKeyGenerator;
    }


    public boolean nameTaken(String tenantName){
        return tenantRepository.findByTenantName(tenantName).isPresent();
    }

    public TenantCreatedDTO createTenant(String tenantName) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        TenantEntity tenant = new TenantEntity();
        TenantCreatedDTO res = new TenantCreatedDTO();

        tenant.setTenantID(UUID.randomUUID());
        Map<String, String> APIKeys = apiKeyGenerator.generateAPIKey(tenant.getTenantID());

        tenant.setTenantName(tenantName);

        tenant.setApiKeyHash(APIKeys.get("hash"));

        Map<String, String> RSAKeys = rsaKeyGenerator.generateKeyPair();
        tenant.setPublicKey(RSAKeys.get("publicKey"));
        tenant.setEncPrivateKey(RSAKeys.get("encPrivateKey"));
        tenant.setIv(RSAKeys.get("iv"));


        res.setTenantName(tenantName);
        res.setRawAPIKey(APIKeys.get("key"));
        res.setPublicKey(RSAKeys.get("publicKey"));
        return res;
    }
}
