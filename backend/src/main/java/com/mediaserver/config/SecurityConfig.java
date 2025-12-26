package com.mediaserver.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.mediaserver.infrastructure.security.MockAuthenticationFilter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final MediaProperties properties;
    private final Optional<MockAuthenticationFilter> mockAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf");

        http.csrf(
                        csrf ->
                                csrf.csrfTokenRepository(
                                                CookieCsrfTokenRepository.withHttpOnlyFalse())
                                        .csrfTokenRequestHandler(csrfHandler)
                                        .ignoringRequestMatchers(
                                                "/api/stream/**", "/api/thumbnails/**", "/ws/**"))
                .cors(withDefaults())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/api/stream/**")
                                        .permitAll()
                                        .requestMatchers("/api/thumbnails/**")
                                        .permitAll()
                                        .requestMatchers("/ws/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .httpBasic(withDefaults());

        mockAuthenticationFilter.ifPresent(
                filter -> http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var adminConfig = properties.getAdmin();

        if (adminConfig.getPassword() == null || adminConfig.getPassword().isBlank()) {
            throw new IllegalStateException(
                    "Admin password must be configured via 'media.admin.password' property or"
                            + " MEDIA_ADMIN_PASSWORD environment variable. Do not use default"
                            + " credentials in production.");
        }

        var user =
                User.builder()
                        .username(adminConfig.getUsername())
                        .password(passwordEncoder.encode(adminConfig.getPassword()))
                        .roles("ADMIN")
                        .build();

        log.info("Configured admin user: {}", adminConfig.getUsername());
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
