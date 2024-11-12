package nl.dcsolutions.authserver.domain;

import jakarta.persistence.*;

@Entity
public class Scope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String scope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_client_id")
    private Client registeredClient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Client getRegisteredClient() {
        return registeredClient;
    }

    public void setRegisteredClient(Client registeredClient) {
        this.registeredClient = registeredClient;
    }
}
