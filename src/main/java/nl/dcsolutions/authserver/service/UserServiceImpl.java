package nl.dcsolutions.authserver.service;

import nl.dcsolutions.authserver.domain.Client;
import nl.dcsolutions.authserver.domain.Role;
import nl.dcsolutions.authserver.domain.User;
import nl.dcsolutions.authserver.repository.RegisteredClientEntityRepository;
import nl.dcsolutions.authserver.repository.RoleRepository;
import nl.dcsolutions.authserver.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RegisteredClientEntityRepository registeredClientEntityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, RegisteredClientEntityRepository registeredClientEntityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.registeredClientEntityRepository = registeredClientEntityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User saveUser(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        registeredClientEntityRepository.findByClientId(jwt.getSubject()).ifPresent(user::setClient);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public List<User> getUsersForClient(String clientId) {
        Jwt authentication = (Jwt) SecurityContextHolder.getContext().getAuthentication();
        if (isClient(authentication)) {
            throw new AccessDeniedException("Unauthorized to access users");
        }
        return userRepository.findUsersByClientId(clientId);
    }

    private boolean isClient(Jwt jwt) {
        String clientId = jwt.getSubject();
        Optional<Client> client = registeredClientEntityRepository.findByClientId(clientId);
        return client.isPresent();
    }

    @Override
    public void addRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
