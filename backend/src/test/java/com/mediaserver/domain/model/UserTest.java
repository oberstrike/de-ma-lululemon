package com.mediaserver.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mediaserver.domain.exception.UserValidationException;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void builder_shouldCreateUser() {
        User user = User.builder().id("user-1").username("jane").externalId("kc-123").build();

        assertThat(user.getId()).isEqualTo("user-1");
        assertThat(user.getUsername()).isEqualTo("jane");
        assertThat(user.getExternalId()).isEqualTo("kc-123");
    }

    @Test
    void builder_shouldAllowNullExternalId() {
        User user = User.builder().id("user-1").username("jane").build();

        assertThat(user.getExternalId()).isNull();
    }

    @Test
    void builder_shouldRejectNullUsername() {
        assertThatThrownBy(() -> User.builder().id("user-1").username(null).build())
                .isInstanceOf(UserValidationException.class)
                .hasMessage("Username is required");
    }

    @Test
    void builder_shouldRejectBlankUsername() {
        assertThatThrownBy(() -> User.builder().id("user-1").username("   ").build())
                .isInstanceOf(UserValidationException.class)
                .hasMessage("Username is required");
    }

    @Test
    void wither_shouldUpdateFields() {
        User user = User.builder().id("user-1").username("jane").externalId("kc-123").build();

        User updated = user.withUsername("sam").withExternalId("kc-456");

        assertThat(updated.getId()).isEqualTo("user-1");
        assertThat(updated.getUsername()).isEqualTo("sam");
        assertThat(updated.getExternalId()).isEqualTo("kc-456");
    }
}
