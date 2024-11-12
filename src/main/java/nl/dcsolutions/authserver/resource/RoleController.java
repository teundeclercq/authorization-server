package nl.dcsolutions.authserver.resource;

import nl.dcsolutions.authserver.domain.Role;
import nl.dcsolutions.authserver.resource.util.ExceptionUtil;
import nl.dcsolutions.authserver.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.saveRole(role));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<List<Role>> getAllRoles(@PathVariable String clientId) {
        return ResponseEntity.ok(roleService.getRolesForClient(clientId));
    }

    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getRole(@PathVariable Long roleId) {
        return ExceptionUtil.wrapOrNotFound(roleService.findRoleById(roleId));
    }
}
