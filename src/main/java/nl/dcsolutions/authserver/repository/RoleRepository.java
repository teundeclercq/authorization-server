package nl.dcsolutions.authserver.repository;

import nl.dcsolutions.authserver.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
    List<Role> findRolesByClientId(String clientId);
}
