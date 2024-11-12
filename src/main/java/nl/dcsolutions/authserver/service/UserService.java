package nl.dcsolutions.authserver.service;

import nl.dcsolutions.authserver.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    Optional<User> findUserById(Long userId);
    List<User> getUsersForClient(String clientId);
    void addRoleToUser(Long userId, Long roleId);
    void deleteUser(Long userId);
}
