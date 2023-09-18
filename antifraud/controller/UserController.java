package antifraud.controller;

import antifraud.controller.routing.User;
import antifraud.dto.UserDTO;
import antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

//    @PostMapping(User.PATH)
//    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO request) {
//        UserDTO userDTO = UserDTO.builder()
//                .username(request.getUsername())
//                .name(request.getName())
//                .password(passwordEncoder.encode(request.getPassword()))
//    }

}
