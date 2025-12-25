package com.mediaserver.config;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MediaProperties properties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = properties.getCors().getAllowedOrigins();
        boolean allowAll = allowedOrigins != null && Arrays.asList(allowedOrigins).contains("*");
        var registration = registry.addMapping("/api/**");

        if (allowAll) {
            registration.allowedOriginPatterns(allowedOrigins).allowCredentials(false);
        } else if (allowedOrigins != null && allowedOrigins.length > 0) {
            registration.allowedOrigins(allowedOrigins).allowCredentials(true);
        } else {
            registration.allowedOrigins().allowCredentials(true);
        }

        registration
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
