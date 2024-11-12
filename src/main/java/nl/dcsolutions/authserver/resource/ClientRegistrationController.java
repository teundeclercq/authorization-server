package nl.dcsolutions.authserver.resource;

import nl.dcsolutions.authserver.service.JpaRegisteredClientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2/register")
public class ClientRegistrationController {

    private final JpaRegisteredClientRepository registeredClientRepository;

    public ClientRegistrationController(JpaRegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @PostMapping
    public ResponseEntity<?> registerClient(@RequestBody RegisteredClient registeredClient) {
        registeredClientRepository.save(registeredClient);
        return ResponseEntity.ok("Client registered successfully");
    }
}
