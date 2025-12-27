package com.mediaserver.infrastructure.security;

import com.mediaserver.application.model.CurrentUser;
import com.mediaserver.config.MediaProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class MockAuthenticationFilter extends OncePerRequestFilter {

    private final MediaProperties properties;

    public MockAuthenticationFilter(MediaProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null) {
            var mock = properties.getAuth().getMock();
            var userId = resolveHeader(request, "X-Mock-UserId", mock.getUserId());
            var username = resolveHeader(request, "X-Mock-Username", mock.getUsername());
            var currentUser = new CurrentUser(userId, username);
            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            currentUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            context.setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveHeader(HttpServletRequest request, String header, String fallback) {
        var value = request.getHeader(header);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
