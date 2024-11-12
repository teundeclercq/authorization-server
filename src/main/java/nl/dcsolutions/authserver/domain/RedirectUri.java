package nl.dcsolutions.authserver.domain;

import jakarta.persistence.*;

@Entity
public class RedirectUri {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_client_id")
    private Client registeredClient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Client getRegisteredClient() {
        return registeredClient;
    }

    public void setRegisteredClient(Client registeredClient) {
        this.registeredClient = registeredClient;
    }
}
