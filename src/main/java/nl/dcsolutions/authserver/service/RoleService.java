package nl.dcsolutions.authserver.service;

import nl.dcsolutions.authserver.domain.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role saveRole(Role role);
    Optional<Role> findRoleById(Long roleId);
    List<Role> getRolesForClient(String clientId);
    void deleteRole(Long roleId);
}
