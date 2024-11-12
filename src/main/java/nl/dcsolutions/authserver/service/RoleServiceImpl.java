package nl.dcsolutions.authserver.service;

import nl.dcsolutions.authserver.domain.Client;
import nl.dcsolutions.authserver.domain.Role;
import nl.dcsolutions.authserver.repository.RegisteredClientEntityRepository;
import nl.dcsolutions.authserver.repository.RoleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private final RegisteredClientEntityRepository registeredClientEntityRepository;

    public RoleServiceImpl(RoleRepository roleRepository, RegisteredClientEntityRepository registeredClientEntityRepository) {
        this.roleRepository = roleRepository;
        this.registeredClientEntityRepository = registeredClientEntityRepository;
    }

    @Override
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Optional<Role> findRoleById(Long roleId) {
        Jwt authentication = (Jwt) SecurityContextHolder.getContext().getAuthentication();

        Optional<Role> role = roleRepository.findById(roleId);
        if (role.isPresent() && role.get().getClient().getClientId().equals(authentication.getSubject())) {
            return role;
        }

        return Optional.empty();
    }

    @Override
    public List<Role> getRolesForClient(String clientId) {
        Jwt authentication = (Jwt) SecurityContextHolder.getContext().getAuthentication();
        if (isClient(authentication, clientId)) {
            throw new AccessDeniedException("Unauthorized to access roles");
        }
        return roleRepository.findRolesByClientId(clientId);
    }

    private boolean isClient(Jwt jwt, String clientId) {
        String subject = jwt.getSubject();
        Optional<Client> client = registeredClientEntityRepository.findByClientId(subject);
        return client.isPresent() && clientId.equals(subject);
    }

    @Override
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }
}
