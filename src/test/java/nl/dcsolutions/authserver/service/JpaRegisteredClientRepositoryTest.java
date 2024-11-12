package nl.dcsolutions.authserver.service;

import nl.dcsolutions.authserver.domain.Client;
import nl.dcsolutions.authserver.domain.RedirectUri;
import nl.dcsolutions.authserver.repository.RegisteredClientEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaRegisteredClientRepositoryTest {
    @Mock
    private RegisteredClientEntityRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JpaRegisteredClientRepository clientRepository;

    private final String clientId = "test-client-id";
    private final String clientSecret = "test-client-secret";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientRepository = new JpaRegisteredClientRepository(clientId, clientSecret, repository, passwordEncoder);
    }

    @Test
    void testInitializeClientRegistrar_WhenClientNotExists() {
        // Given
        when(repository.findByClientId("client-registrar")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-secret");

        // Then
        verify(repository, times(1)).save(any(Client.class));
    }

    @Test
    void testInitializeClientRegistrar_WhenClientExists() {
        // Given
        Client existingClient = new Client();
        when(repository.findByClientId("client-registrar")).thenReturn(Optional.of(existingClient));

        // Then
        verify(repository, times(1)).save(any(Client.class));
    }

    @Test
    void testSave() {
        // Given
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://example.com")
                .build();

        when(passwordEncoder.encode(clientSecret)).thenReturn("encoded-secret");

        // When
        clientRepository.save(registeredClient);

        // Then
        verify(repository, times(2)).save(any(Client.class));
    }

    @Test
    void testFindById() {
        // Given
        Client clientEntity = new Client();
        clientEntity.setId("test-id");
        clientEntity.setClientId("test-client-id");
        nl.dcsolutions.authserver.domain.AuthorizationGrantType grantType = new nl.dcsolutions.authserver.domain.AuthorizationGrantType();
        grantType.setGrantType("authorization_code");
        clientEntity.getAuthorizationGrantTypes().add(grantType);
        RedirectUri redirectUri = new RedirectUri();
        redirectUri.setUri("http://example.com");
        clientEntity.getRedirectUris().add(redirectUri);
        when(repository.findById("test-id")).thenReturn(Optional.of(clientEntity));

        // When
        RegisteredClient result = clientRepository.findById("test-id");

        // Then
        assertNotNull(result);
        assertEquals("test-id", result.getId());
    }

    @Test
    void testFindByClientId() {
        // Given
        Client clientEntity = new Client();
        clientEntity.setClientId(clientId);
        clientEntity.setId("test-id");
        nl.dcsolutions.authserver.domain.AuthorizationGrantType grantType = new nl.dcsolutions.authserver.domain.AuthorizationGrantType();
        grantType.setGrantType("authorization_code");
        clientEntity.getAuthorizationGrantTypes().add(grantType);
        RedirectUri redirectUri = new RedirectUri();
        redirectUri.setUri("http://example.com");
        clientEntity.getRedirectUris().add(redirectUri);
        when(repository.findByClientId(clientId)).thenReturn(Optional.of(clientEntity));

        // When
        RegisteredClient result = clientRepository.findByClientId(clientId);

        // Then
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
    }
}