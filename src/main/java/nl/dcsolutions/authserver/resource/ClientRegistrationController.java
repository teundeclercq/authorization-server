package nl.dcsolutions.authserver.resource;

import nl.dcsolutions.authserver.dto.ClientRegistrationDTO;
import nl.dcsolutions.authserver.dto.ClientSettingsDTO;
import nl.dcsolutions.authserver.dto.TokenSettingsDTO;
import nl.dcsolutions.authserver.service.JpaRegisteredClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/oauth2/register")
public class ClientRegistrationController {

    private final JpaRegisteredClientRepository registeredClientRepository;

    public ClientRegistrationController(JpaRegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @PostMapping
    public ResponseEntity<?> registerClient(@RequestBody ClientRegistrationDTO dto) {
        // Map ClientSettingsDTO to ClientSettings
        ClientSettings clientSettings = mapToClientSettings(dto.getClientSettings());

        // Map TokenSettingsDTO to TokenSettings
        TokenSettings tokenSettings = mapToTokenSettings(dto.getTokenSettings());

        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(dto.getClientId())
                .clientSecret(dto.getClientSecret())
                .clientName(dto.getClientName())
                .authorizationGrantTypes(grantTypes -> dto.getAuthorizationGrantTypes()
                        .forEach(grantType -> grantTypes.add(new AuthorizationGrantType(grantType))))
                .redirectUris(redirectUris -> redirectUris.addAll(dto.getRedirectUris()))
                .scopes(scopes -> scopes.addAll(dto.getScopes()))
                .clientSettings(clientSettings)
                .tokenSettings(tokenSettings)
                .build();

        registeredClientRepository.save(registeredClient);
        return ResponseEntity.ok("Client registered successfully");
    }

    private ClientSettings mapToClientSettings(ClientSettingsDTO clientSettingsDTO) {
        var clientSettings = ClientSettings.builder();

        if (clientSettingsDTO == null) {
            return clientSettings.build();
        }

        if (clientSettingsDTO.getRequireProofKey() != null) {
            clientSettings.requireAuthorizationConsent(true);
        }

        if (clientSettingsDTO.getRequireProofKey() != null) {
            clientSettings.requireAuthorizationConsent(true);
        }
        return clientSettings.build();
    }

    private TokenSettings mapToTokenSettings(TokenSettingsDTO tokenSettingsDTO) {
        var tokensettings = TokenSettings.builder();
        if (tokenSettingsDTO == null) {
            return tokensettings.build();
        }
        if (tokenSettingsDTO.getReuseRefreshTokens() != null) {
            tokensettings.reuseRefreshTokens(tokenSettingsDTO.getReuseRefreshTokens());
        }
        if (tokenSettingsDTO.getRefreshTokenTimeToLive() != null) {
            tokensettings.refreshTokenTimeToLive(Duration.parse(tokenSettingsDTO.getRefreshTokenTimeToLive()));
        }
        if (tokenSettingsDTO.getAccessTokenTimeToLive() != null) {
            tokensettings.accessTokenTimeToLive(Duration.parse(tokenSettingsDTO.getAccessTokenTimeToLive()));
        }

        if (tokenSettingsDTO.getIdTokenSignatureAlgorithm() != null) {
            tokensettings.idTokenSignatureAlgorithm(SignatureAlgorithm.from(tokenSettingsDTO.getIdTokenSignatureAlgorithm()));
        }
        return tokensettings.build();
    }
}
