package nl.dcsolutions.authserver.domain;

import jakarta.persistence.*;

@Entity
public class ClientAuthenticationMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_client_id")
    private Client registeredClient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Client getRegisteredClient() {
        return registeredClient;
    }

    public void setRegisteredClient(Client registeredClient) {
        this.registeredClient = registeredClient;
    }
}
