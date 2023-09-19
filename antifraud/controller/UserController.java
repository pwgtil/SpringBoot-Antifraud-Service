package antifraud.controller;

import antifraud.dto.UserDTO;
import antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(antifraud.controller.routing.User.PATH)
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO) {
        userDTO.encodePassword(passwordEncoder);
        userDTO = userService.registerUser(userDTO);
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    @GetMapping(antifraud.controller.routing.List.PATH)
    public ResponseEntity<List<UserDTO>> getAuthorizedUsers() {
        List<UserDTO> usersList = userService.getAuthorizedUsers();
        return new ResponseEntity<>(usersList, HttpStatus.OK);
    }

    @DeleteMapping(antifraud.controller.routing.User.PATH + "/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return new ResponseEntity<>(Map.of("username", username, "status", "Deleted successfully!"), HttpStatus.OK);
    }

    @PutMapping(antifraud.controller.routing.Role.PATH)
    public ResponseEntity<UserDTO> changeUserAuthority(@RequestBody ChangeAuthority authority) {
        UserDTO userDTO = userService.changeUserAuthorization(authority.username, authority.role);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PutMapping(antifraud.controller.routing.Access.PATH)
    public ResponseEntity<Map<String, String>> changeUserAccess(@RequestBody ChangeAccess access) {
        userService.changeUserAccess(access.username, access.operation);
        return new ResponseEntity<>(Map.of("status", "User " + access.username + " " + access.operation.toLowerCase() + "ed!"), HttpStatus.OK);
    }

    record ChangeAuthority(String username, String role) {
    }

    record ChangeAccess(String username, String operation) {
    }
}
