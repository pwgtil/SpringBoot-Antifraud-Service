package antifraud.dto;

import antifraud.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
                .authority(user.getAuthority())
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

    @JsonIgnore
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String authority;

    public User convertDto2User(String authority) {
        return User.builder()
                .name(getName())
                .username(getUsername())
                .password(getPassword())
                .authority(authority)
                .accountNonLocked(true)
                .build();
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        if (this.password == null || this.password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password empty");
        } else {
            this.password = passwordEncoder.encode(this.getPassword());
        }
    }
}
