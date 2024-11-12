package nl.dcsolutions.authserver.repository;

import nl.dcsolutions.authserver.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisteredClientEntityRepository extends JpaRepository<Client, String> {
    Optional<Client> findByClientId(String clientId);
}
