package com.mediaserver.infrastructure.security;

import com.mediaserver.application.model.CurrentUser;
import com.mediaserver.application.port.out.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {
    @Override
    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return new CurrentUser(authentication.getName(), authentication.getName());
    }
}
