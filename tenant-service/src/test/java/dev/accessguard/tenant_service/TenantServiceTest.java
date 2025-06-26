package dev.accessguard.tenant_service;

import dev.accessguard.tenant_service.Repositories.TenantRepository;
import dev.accessguard.tenant_service.models.TenantCreatedDTO;
import dev.accessguard.tenant_service.models.TenantEntity;
import dev.accessguard.tenant_service.models.TenantResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantServiceTest {

    private TenantRepository tenantRepository;
    private ApiKeyGenerator apiKeyGenerator;
    private RSAKeyGenerator rsaKeyGenerator;
    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        tenantRepository = mock(TenantRepository.class);
        apiKeyGenerator = mock(ApiKeyGenerator.class);
        rsaKeyGenerator = mock(RSAKeyGenerator.class);
        tenantService = new TenantService(apiKeyGenerator, tenantRepository, rsaKeyGenerator);
    }

    @Test
    void testNameTaken_whenTenantExists_returnsTrue() {
        when(tenantRepository.findByTenantName("acme")).thenReturn(Optional.of(new TenantEntity()));
        assertTrue(tenantService.nameTaken("acme"));
    }

    @Test
    void testNameTaken_whenTenantNotExists_returnsFalse() {
        when(tenantRepository.findByTenantName("acme")).thenReturn(Optional.empty());
        assertFalse(tenantService.nameTaken("acme"));
    }

    @Test
    void testCreateTenant_successfulCreation_returnsTenantCreatedDTO() throws Exception {
        Map<String, String> fakeAPIKeys = Map.of(
                "rawAPIKey", "raw-123",
                "hashedAPIKey", "hashed-123"
        );

        Map<String, String> fakeRSAKeys = Map.of(
                "publicKey", "pub-key",
                "encPrivateKey", "enc-priv-key",
                "iv", "iv-val"
        );

        when(apiKeyGenerator.generateAPIKey(any(UUID.class))).thenReturn(fakeAPIKeys);
        when(rsaKeyGenerator.generateKeyPair()).thenReturn(fakeRSAKeys);

        TenantCreatedDTO result = tenantService.createTenant("acme");

        assertEquals("acme", result.getTenantName());
        assertEquals("raw-123", result.getRawAPIKey());
        assertEquals("pub-key", result.getPublicKey());
        verify(tenantRepository).save(any(TenantEntity.class));
    }

    @Test
    void testGetTenant_whenExists_returnsDTO() {
        TenantEntity mockTenant = new TenantEntity();
        mockTenant.setTenantName("acme");
        mockTenant.setPublicKey("pub-key");

        when(tenantRepository.findByTenantName("acme")).thenReturn(Optional.of(mockTenant));

        Optional<TenantResponseDTO> result = tenantService.getTenant("acme");

        assertTrue(result.isPresent());
        assertEquals("acme", result.get().getTenantName());
        assertEquals("pub-key", result.get().getPublicKey());
    }

    @Test
    void testGetTenant_whenNotFound_returnsEmpty() {
        when(tenantRepository.findByTenantName("ghost")).thenReturn(Optional.empty());

        Optional<TenantResponseDTO> result = tenantService.getTenant("ghost");
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteTenant_returnsSuccessMessage() {
        doNothing().when(tenantRepository).deleteByTenantName("acme");

        String response = tenantService.deleteTenant("acme");
        assertEquals("acme deleted successfully", response);
    }

    @Test
    void testRecreateAPIKey_whenTenantExists_returnsUpdatedDTO() {
        UUID fakeUUID = UUID.randomUUID();

        TenantEntity mockTenant = new TenantEntity();
        mockTenant.setTenantID(fakeUUID);
        mockTenant.setTenantName("acme");
        mockTenant.setPublicKey("pub-key");

        Map<String, String> fakeAPIKeys = Map.of(
                "rawAPIKey", "raw-999",
                "hashedAPIKey", "hashed-999"
        );

        when(tenantRepository.findByTenantName("acme")).thenReturn(Optional.of(mockTenant));
        when(apiKeyGenerator.generateAPIKey(fakeUUID)).thenReturn(fakeAPIKeys);

        Optional<TenantCreatedDTO> result = tenantService.recreateAPIKey("acme");

        assertTrue(result.isPresent());
        assertEquals("acme", result.get().getTenantName());
        assertEquals("raw-999", result.get().getRawAPIKey());
        assertEquals("pub-key", result.get().getPublicKey());
        verify(tenantRepository).save(mockTenant);
    }

    @Test
    void testRecreateAPIKey_whenTenantMissing_returnsEmpty() {
        when(tenantRepository.findByTenantName("ghost")).thenReturn(Optional.empty());
        Optional<TenantCreatedDTO> result = tenantService.recreateAPIKey("ghost");
        assertFalse(result.isPresent());
    }
}
