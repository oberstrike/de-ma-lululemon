package com.mediaserver.infrastructure.security;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mediaserver.application.port.out.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(properties = {"media.auth.provider=mock"})
@AutoConfigureMockMvc
@Import(MockAuthenticationFilterTest.TestController.class)
class MockAuthenticationFilterTest {

    @Autowired private MockMvc mockMvc;

    @RestController
    static class TestController {
        private final CurrentUserProvider currentUserProvider;

        TestController(CurrentUserProvider currentUserProvider) {
            this.currentUserProvider = currentUserProvider;
        }

        @GetMapping
        @RequestMapping("/test/current-user")
        public CurrentUserResponse currentUser() {
            var currentUser = currentUserProvider.getCurrentUser();
            return new CurrentUserResponse(currentUser.userId(), currentUser.username());
        }
    }

    record CurrentUserResponse(String userId, String username) {}

    @Test
    void usesHeadersForMockUser() throws Exception {
        mockMvc.perform(
                        get("/test/current-user")
                                .header("X-Mock-UserId", "user-123")
                                .header("X-Mock-Username", "mock-user")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("user-123")))
                .andExpect(jsonPath("$.username", is("mock-user")));
    }

    @Test
    void fallsBackToConfiguredMockUser() throws Exception {
        mockMvc.perform(get("/test/current-user").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("mock-user")))
                .andExpect(jsonPath("$.username", is("mock")));
    }
}
