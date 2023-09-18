package antifraud.security;

import antifraud.entity.User;
import antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAdapterService implements UserDetailsService {

    private UserService userService;

    @Autowired
    public UserAdapterService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUser(username)
                .orElseThrow( () -> new UsernameNotFoundException("User " + username + " not found!"));
        return new UserAdapter(user);
    }
}
