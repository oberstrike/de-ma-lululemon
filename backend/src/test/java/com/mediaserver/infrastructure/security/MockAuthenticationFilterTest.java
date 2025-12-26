package com.mediaserver.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.mediaserver.application.model.CurrentUser;
import com.mediaserver.config.MediaProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class MockAuthenticationFilterTest {

    private MockAuthenticationFilter filter;
    private MediaProperties properties;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        properties = new MediaProperties();
        var auth = new MediaProperties.Auth();
        var mock = new MediaProperties.Auth.Mock();
        mock.setUserId("default-user");
        mock.setUsername("default-name");
        auth.setMock(mock);
        properties.setAuth(auth);

        filter = new MockAuthenticationFilter(properties);
    }

    @Test
    void usesHeadersForMockUser() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        request.addHeader("X-Mock-UserId", "user-123");
        request.addHeader("X-Mock-Username", "mock-user");

        var response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(CurrentUser.class);

        var currentUser = (CurrentUser) authentication.getPrincipal();
        assertThat(currentUser.userId()).isEqualTo("user-123");
        assertThat(currentUser.username()).isEqualTo("mock-user");
    }

    @Test
    void fallsBackToConfiguredMockUser() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();

        var currentUser = (CurrentUser) authentication.getPrincipal();
        assertThat(currentUser.userId()).isEqualTo("default-user");
        assertThat(currentUser.username()).isEqualTo("default-name");
    }

    @Test
    void doesNotOverrideExistingAuthentication() throws ServletException, IOException {
        var existingUser = new CurrentUser("existing-id", "existing-name");
        var existingAuth = new UsernamePasswordAuthenticationToken(existingUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        var request = new MockHttpServletRequest();
        request.addHeader("X-Mock-UserId", "new-user");
        var response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var currentUser = (CurrentUser) authentication.getPrincipal();
        assertThat(currentUser.userId()).isEqualTo("existing-id");
    }
}
