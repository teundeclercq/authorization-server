package nl.dcsolutions.authserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.dcsolutions.authserver.domain.*;
import nl.dcsolutions.authserver.repository.RegisteredClientEntityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final RegisteredClientEntityRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String client;
    private final String secret;
    public JpaRegisteredClientRepository(@Value("${REGISTRAR_CLIENT}") String client, @Value("${REGISTRAR_SECRET}") String secret, RegisteredClientEntityRepository repository, PasswordEncoder passwordEncoder) {
        this.client = client;
        this.secret = secret;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        initializeClientRegistrar();
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Client entity = new Client();
        entity.setId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientIdIssuedAt(Instant.now());
        entity.setClientSecret(passwordEncoder.encode(registeredClient.getClientSecret()));
        entity.setClientSecretExpiresAt(LocalDateTime.now().plusYears(1).toInstant(ZoneOffset.UTC));
        entity.setClientName(registeredClient.getClientName());

        // Convert each ClientAuthenticationMethod to ClientAuthenticationMethodEntity
        if (registeredClient.getClientAuthenticationMethods() != null) {
            registeredClient.getClientAuthenticationMethods().forEach(method -> {
                ClientAuthenticationMethod methodEntity = new ClientAuthenticationMethod();
                methodEntity.setMethod(method.getValue());
                methodEntity.setRegisteredClient(entity);
                entity.getClientAuthenticationMethods().add(methodEntity);
            });
        }

        // Convert each AuthorizationGrantType to AuthorizationGrantTypeEntity
        registeredClient.getAuthorizationGrantTypes().forEach(grantType -> {
            AuthorizationGrantType grantTypeEntity = new AuthorizationGrantType();
            grantTypeEntity.setGrantType(grantType.getValue());
            grantTypeEntity.setRegisteredClient(entity);
            entity.getAuthorizationGrantTypes().add(grantTypeEntity);
        });

        // Convert redirect URIs
        registeredClient.getRedirectUris().forEach(uri -> {
            RedirectUri uriEntity = new RedirectUri();
            uriEntity.setUri(uri);
            uriEntity.setRegisteredClient(entity);
            entity.getRedirectUris().add(uriEntity);
        });

        // Convert scopes
        registeredClient.getScopes().forEach(scope -> {
            Scope scopeEntity = new Scope();
            scopeEntity.setScope(scope);
            scopeEntity.setRegisteredClient(entity);
            entity.getScopes().add(scopeEntity);
        });
        // Map other fields as needed
        repository.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return repository.findById(id).map(this::convertToRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return repository.findByClientId(clientId).map(this::convertToRegisteredClient).orElse(null);
    }


    private void initializeClientRegistrar() {
        if (repository.findByClientId("client-registrar").isEmpty()) {
            RegisteredClient clientRegistrar = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(this.client)
                    .clientName("client registrar")
                    .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientSecret(this.secret)  // Encode the secret
                    .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .redirectUri("http://localhost:8080/registered")
                    .scope("client:register")  // Scope to allow registration of other clients
                    .build();

            save(clientRegistrar);
        }
    }

    private RegisteredClient convertToRegisteredClient(Client entity) {
        return RegisteredClient.withId(entity.getId())
                .clientId(entity.getClientId())
                .clientIdIssuedAt(entity.getClientIdIssuedAt())
                .clientSecret(entity.getClientSecret())
                .clientSecretExpiresAt(entity.getClientSecretExpiresAt())
                .clientName(entity.getClientName())
                .clientAuthenticationMethods(authMethods ->
                        entity.getClientAuthenticationMethods().stream()
                                .map(authMethodEntity -> new org.springframework.security.oauth2.core.ClientAuthenticationMethod(authMethodEntity.getMethod()))
                                .forEach(authMethods::add)
                )
                .authorizationGrantTypes(grantTypes ->
                        entity.getAuthorizationGrantTypes().stream()
                                .map(grantTypeEntity -> new org.springframework.security.oauth2.core.AuthorizationGrantType(grantTypeEntity.getGrantType()))
                                .forEach(grantTypes::add)
                )
                .redirectUris(redirectUris ->
                        entity.getRedirectUris().stream()
                                .map(RedirectUri::getUri)
                                .forEach(redirectUris::add)
                )
                .scopes(scopes ->
                        entity.getScopes().stream()
                                .map(Scope::getScope)
                                .forEach(scopes::add)
                )
//                .clientSettings(ClientSettings.withSettings(enitty).build())
//                .tokenSettings(TokenSettings.withSettings(parseJson(entity.getTokenSettings(), TokenSettings.class)).build())
                .build();
    }
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        }
    }
}