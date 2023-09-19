package antifraud.dto;

import antifraud.entity.User;
import antifraud.entity.enums.UserRoles;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserDTO {

    public static UserDTO convertUser2DTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .role(user.getAuthority())
                .build();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long	id;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @NotEmpty
    private String	name;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @NotEmpty
    private String	username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty
    private String	password;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String role;

    public User convertDto2User(String authority) {
        return User.builder()
                .name(getName())
                .username(getUsername())
                .password(getPassword())
                .authority(authority)
                .accountNonLocked(isNonLockedByDefault(authority))
                .build();
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        if (this.password == null || this.password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password empty");
        } else {
            this.password = passwordEncoder.encode(this.getPassword());
        }
    }

    private boolean isNonLockedByDefault(String authority) {
        return authority.equals(UserRoles.ADMINISTRATOR.name());
    }
}
