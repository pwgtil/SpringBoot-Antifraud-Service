package antifraud.service;

import antifraud.dto.UserDTO;
import antifraud.entity.User;
import antifraud.entity.enums.UserAccess;
import antifraud.entity.enums.UserRoles;
import antifraud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    static volatile long userCount = 0;
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        userCount = userRepository.count();
    }

    public Optional<User> getUser(String username) {
        return userRepository.findUserByUsernameIgnoreCase(username);
    }

    @Transactional
    public void deleteUser(String username) {
        if (userExists(username)) {
            userRepository.deleteUserByUsernameIgnoreCase(username);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }


    public UserDTO registerUser(UserDTO userDTO) {

        // Basic validations TODO: move to separate method to validate UserDTO
        if (userExists(userDTO.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User exist!");
        }

        if (userDTO.getUsername() == null || userDTO.getName() == null
                || userDTO.getUsername().isBlank() || userDTO.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and/or username needs to be filled in!");
        }


        synchronized (UserService.class) {
            String authority;
            if (userCount < 1) {
                authority = UserRoles.ADMINISTRATOR.name();
            } else {
                authority = UserRoles.MERCHANT.name();
            }

            User user = userDTO.convertDto2User(authority);

            userRepository.save(user);
            userCount++;
            return UserDTO.convertUser2DTO(user);
        }
    }

    public List<UserDTO> getAuthorizedUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::convertUser2DTO)
                .collect(Collectors.toList());
    }

    private boolean userExists(String username) {
        return userRepository.countUsersByUsernameIgnoreCase(username) > 0;
    }

    public void changeUserAccess(String username, String operation) {
        // Basic validations
        User user = getUser(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        if (user.getAuthority().equalsIgnoreCase(UserRoles.ADMINISTRATOR.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operation no allowed on user with admin rights!");
        }

        if (UserAccess.LOCK.name().equalsIgnoreCase(operation)) {
            user.setAccountNonLocked(false);
        } else if (UserAccess.UNLOCK.name().equalsIgnoreCase(operation)) {
            user.setAccountNonLocked(true);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected value: " + operation.toUpperCase());
        }
        userRepository.save(user);
    }

    public UserDTO changeUserAuthorization(String username, String authority) {

        // Basic validations
        User user = getUser(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        if (Arrays.stream(UserRoles.values()).noneMatch(auth -> auth.name().equalsIgnoreCase(authority))
                || authority.equalsIgnoreCase(UserRoles.ADMINISTRATOR.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect role!");
        }

        if (user.getAuthority().equalsIgnoreCase(authority)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has that role!");
        }

        user.setAuthority(authority.toUpperCase());

        userRepository.save(user);

        return UserDTO.convertUser2DTO(user);
    }
}
