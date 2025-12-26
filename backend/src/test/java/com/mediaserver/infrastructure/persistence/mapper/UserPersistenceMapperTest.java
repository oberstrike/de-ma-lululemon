package com.mediaserver.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mediaserver.domain.model.User;
import com.mediaserver.infrastructure.persistence.entity.UserJpaEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserPersistenceMapperTest {

    private final UserPersistenceMapper userPersistenceMapper =
            Mappers.getMapper(UserPersistenceMapper.class);

    @Test
    void toDomain_shouldMapAllFields() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        UserJpaEntity entity =
                UserJpaEntity.builder()
                        .id("user-1")
                        .username("jdoe")
                        .email("jdoe@example.com")
                        .displayName("John Doe")
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();

        User result = userPersistenceMapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo("user-1");
        assertThat(result.getUsername()).isEqualTo("jdoe");
        assertThat(result.getEmail()).isEqualTo("jdoe@example.com");
        assertThat(result.getDisplayName()).isEqualTo("John Doe");
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toEntity_shouldMapAllFields() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime updatedAt = LocalDateTime.now().minusHours(1);
        User user =
                User.builder()
                        .id("user-2")
                        .username("asmith")
                        .email("asmith@example.com")
                        .displayName("Alice Smith")
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();

        UserJpaEntity result = userPersistenceMapper.toEntity(user);

        assertThat(result.getId()).isEqualTo("user-2");
        assertThat(result.getUsername()).isEqualTo("asmith");
        assertThat(result.getEmail()).isEqualTo("asmith@example.com");
        assertThat(result.getDisplayName()).isEqualTo("Alice Smith");
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toDomain_shouldHandleNullOptionalFields() {
        UserJpaEntity entity =
                UserJpaEntity.builder()
                        .id("user-3")
                        .username("bwayne")
                        .email("bwayne@example.com")
                        .build();

        User result = userPersistenceMapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo("user-3");
        assertThat(result.getDisplayName()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        User user =
                User.builder()
                        .username("ckent")
                        .email("ckent@example.com")
                        .build();

        UserJpaEntity result = userPersistenceMapper.toEntity(user);

        assertThat(result.getUsername()).isEqualTo("ckent");
        assertThat(result.getEmail()).isEqualTo("ckent@example.com");
        assertThat(result.getDisplayName()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    void bidirectionalMapping_shouldPreserveValues() {
        User user =
                User.builder()
                        .id("user-4")
                        .username("pparker")
                        .email("pparker@example.com")
                        .displayName("Peter Parker")
                        .build();

        UserJpaEntity entity = userPersistenceMapper.toEntity(user);
        User result = userPersistenceMapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getDisplayName()).isEqualTo(user.getDisplayName());
    }
}
