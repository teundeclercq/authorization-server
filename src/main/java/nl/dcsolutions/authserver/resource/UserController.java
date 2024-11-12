package nl.dcsolutions.authserver.resource;

import nl.dcsolutions.authserver.domain.User;
import nl.dcsolutions.authserver.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(user));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<List<User>> getAllUsers(@PathVariable String clientId) {
        return ResponseEntity.ok(userService.getUsersForClient(clientId));
    }

    @PostMapping("/{userId}/roles")
    public void addRoleToUser(@PathVariable Long userId, @RequestBody Long roleId) {
        userService.addRoleToUser(userId, roleId);
    }

    @DeleteMapping
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

}
