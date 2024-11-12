package nl.dcsolutions.authserver.dto;

import java.util.List;
import java.util.Set;

public class ClientRegistrationDTO {
    private String clientId;
    private String clientSecret;
    private String clientName;
    private List<String> authorizationGrantTypes;
    private List<String> redirectUris;
    private Set<String> scopes;
    private ClientSettingsDTO clientSettings;
    private TokenSettingsDTO tokenSettings;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<String> getAuthorizationGrantTypes() {
        return authorizationGrantTypes;
    }

    public void setAuthorizationGrantTypes(List<String> authorizationGrantTypes) {
        this.authorizationGrantTypes = authorizationGrantTypes;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public ClientSettingsDTO getClientSettings() {
        return clientSettings;
    }

    public void setClientSettings(ClientSettingsDTO clientSettings) {
        this.clientSettings = clientSettings;
    }

    public TokenSettingsDTO getTokenSettings() {
        return tokenSettings;
    }

    public void setTokenSettings(TokenSettingsDTO tokenSettings) {
        this.tokenSettings = tokenSettings;
    }
}
