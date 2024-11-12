package nl.dcsolutions.authserver.domain;

import jakarta.persistence.*;

@Entity
public class AuthorizationGrantType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String grantType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_client_id")
    private Client registeredClient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public Client getRegisteredClient() {
        return registeredClient;
    }

    public void setRegisteredClient(Client registeredClient) {
        this.registeredClient = registeredClient;
    }
}
