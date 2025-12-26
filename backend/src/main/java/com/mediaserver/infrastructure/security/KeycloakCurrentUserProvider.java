package com.mediaserver.infrastructure.security;

import com.mediaserver.application.model.CurrentUser;
import com.mediaserver.application.port.out.CurrentUserProvider;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class KeycloakCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser getCurrentUser() {
        var authentication = getAuthentication();
        var principal = authentication.getPrincipal();
        if (principal instanceof Map<?, ?> attributes) {
            var userId = value(attributes, "sub", authentication.getName());
            var username = value(attributes, "preferred_username", authentication.getName());
            return new CurrentUser(userId, username);
        }
        var name = authentication.getName();
        return new CurrentUser(name, name);
    }

    private Authentication getAuthentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user available");
        }
        return authentication;
    }

    private String value(Map<?, ?> attributes, String key, String fallback) {
        var value = attributes.get(key);
        if (value == null) {
            return fallback;
        }
        var text = value.toString();
        if (text.isBlank()) {
            return fallback;
        }
        return text;
    }
}
