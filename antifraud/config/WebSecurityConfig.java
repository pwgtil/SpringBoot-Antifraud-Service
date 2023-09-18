package antifraud.config;

import antifraud.controller.routing.Transaction;
import antifraud.entity.enums.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    private static final String MERCHANT_ROLE = UserRoles.MERCHANT.name();
    private static final String ADMINISTRATOR_ROLE = UserRoles.ADMINISTRATOR.name();
    private static final String SUPPORT_ROLE = UserRoles.SUPPORT.name();

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    public WebSecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic()
//                .httpBasic(Customizer.withDefaults())
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .csrf().ignoringRequestMatchers(toH2Console()).disable()
                .headers(headers -> headers.frameOptions().disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, Transaction.PATH)
                        .permitAll()
                        .requestMatchers(toH2Console())
                        .permitAll()
                        .requestMatchers("/actuator/shutdown")
                        .permitAll()
                        .requestMatchers("/error")
                        .permitAll()
                        .anyRequest().denyAll()
                )
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
//                .exceptionHandling()
//                .accessDeniedHandler(new ExceptionHandlerController())
//                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
