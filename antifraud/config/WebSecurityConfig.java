package antifraud.config;

import antifraud.controller.routing.*;
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

import javax.lang.model.element.AnnotationMirror;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    private static final String MERCHANT_AUTH = UserRoles.MERCHANT.name();
    private static final String ADMINISTRATOR_AUTH = UserRoles.ADMINISTRATOR.name();
    private static final String SUPPORT_AUTH = UserRoles.SUPPORT.name();

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
                        .requestMatchers(HttpMethod.DELETE, User.PATH + "/*")
                        .hasAuthority(ADMINISTRATOR_AUTH)
                        .requestMatchers(HttpMethod.PUT, Access.PATH)
                        .hasAuthority(ADMINISTRATOR_AUTH)
                        .requestMatchers(HttpMethod.PUT, Role.PATH)
                        .hasAuthority(ADMINISTRATOR_AUTH)
                        .requestMatchers(HttpMethod.GET, List.PATH)
                        .hasAnyAuthority(ADMINISTRATOR_AUTH, SUPPORT_AUTH)
                        .requestMatchers(HttpMethod.PUT, Transaction.PATH)
                        .hasAuthority(SUPPORT_AUTH)
                        .requestMatchers(StolenCard.PATH, StolenCard.PATH + "/*")
                        .hasAuthority(SUPPORT_AUTH)
                        .requestMatchers(SuspiciousIP.PATH, SuspiciousIP.PATH + "/*")
                        .hasAuthority(SUPPORT_AUTH)
                        .requestMatchers(HttpMethod.GET, History.PATH, History.PATH + "/*")
                        .hasAuthority(SUPPORT_AUTH)
//                                .permitAll()
                        .requestMatchers(HttpMethod.POST, Transaction.PATH)
                        .hasAuthority(MERCHANT_AUTH)
                        .requestMatchers(HttpMethod.POST, User.PATH)
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
