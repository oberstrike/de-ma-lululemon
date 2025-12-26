package com.mediaserver.infrastructure.config;

import com.mediaserver.application.port.out.CurrentUserProvider;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.infrastructure.security.KeycloakCurrentUserProvider;
import com.mediaserver.infrastructure.security.MockAuthenticationFilter;
import com.mediaserver.infrastructure.security.MockCurrentUserProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "media.auth.provider", havingValue = "mock", matchIfMissing = true)
    public CurrentUserProvider mockCurrentUserProvider() {
        return new MockCurrentUserProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "media.auth.provider", havingValue = "keycloak")
    public CurrentUserProvider keycloakCurrentUserProvider() {
        return new KeycloakCurrentUserProvider();
    }

    @Bean
    @ConditionalOnProperty(name = "media.auth.provider", havingValue = "mock", matchIfMissing = true)
    public MockAuthenticationFilter mockAuthenticationFilter(MediaProperties properties) {
        return new MockAuthenticationFilter(properties);
    }
}
