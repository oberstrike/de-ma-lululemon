package com.mediaserver.infrastructure.security;

import com.mediaserver.application.model.CurrentUser;
import com.mediaserver.application.port.out.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MockCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser getCurrentUser() {
        var authentication = getAuthentication();
        var principal = authentication.getPrincipal();
        if (principal instanceof CurrentUser currentUser) {
            return currentUser;
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
}
