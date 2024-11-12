package nl.dcsolutions.authserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.dcsolutions.authserver.domain.*;
import nl.dcsolutions.authserver.repository.RegisteredClientEntityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final RegisteredClientEntityRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String client;
    private final String secret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JpaRegisteredClientRepository(@Value("${REGISTRAR_CLIENT}") String client, @Value("${REGISTRAR_SECRET}") String secret, RegisteredClientEntityRepository repository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.client = client;
        this.secret = secret;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        ClassLoader classLoader = JpaRegisteredClientRepository.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        this.objectMapper.registerModules(securityModules);
        this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
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

        // Store ClientSettings and TokenSettings as JSON
        if (registeredClient.getClientSettings() != null) {
            entity.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
        }
        if (registeredClient.getTokenSettings() != null) {
            entity.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));
        }

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


    void initializeClientRegistrar() {
        if (repository.findByClientId("client-registrar").isEmpty()) {
            RegisteredClient clientRegistrar = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(this.client)
                    .clientName("client registrar")
                    .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientSecret(this.secret)  // Encode the secret
                    .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .redirectUri("http://localhost:8080/registered")
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofDays(1)).build())
                    .scope("client:register")  // Scope to allow registration of other clients
                    .build();

            save(clientRegistrar);
        }
    }

    private RegisteredClient convertToRegisteredClient(Client entity) {
        RegisteredClient.Builder builder = RegisteredClient.withId(entity.getId())
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
                );

        Map<String, Object> clientSettingsMap = parseMap(entity.getClientSettings());
        builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());

        Map<String, Object> tokenSettingsMap = parseMap(entity.getTokenSettings());
        builder.tokenSettings(TokenSettings.withSettings(tokenSettingsMap).build());

        return builder.build();
    }

    private Map<String, Object> parseMap(String data) {
        try {
            return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private String writeMap(Map<String, Object> data) {
        try {
            return this.objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}