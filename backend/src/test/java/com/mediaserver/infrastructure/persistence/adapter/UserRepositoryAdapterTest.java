package com.mediaserver.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mediaserver.domain.model.User;
import com.mediaserver.infrastructure.persistence.entity.UserJpaEntity;
import com.mediaserver.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock private UserJpaRepository userJpaRepository;

    @Mock private UserPersistenceMapper userPersistenceMapper;

    @InjectMocks private UserRepositoryAdapter userRepositoryAdapter;

    private User user;
    private UserJpaEntity entity;

    @BeforeEach
    void setUp() {
        user =
                User.builder()
                        .id("user-1")
                        .username("jdoe")
                        .email("jdoe@example.com")
                        .displayName("John Doe")
                        .build();

        entity =
                UserJpaEntity.builder()
                        .id("user-1")
                        .username("jdoe")
                        .email("jdoe@example.com")
                        .displayName("John Doe")
                        .build();
    }

    @Test
    void findById_shouldReturnDomainUser_whenFound() {
        when(userJpaRepository.findById("user-1")).thenReturn(Optional.of(entity));
        when(userPersistenceMapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = userRepositoryAdapter.findById("user-1");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("jdoe");
        verify(userJpaRepository).findById("user-1");
        verify(userPersistenceMapper).toDomain(entity);
    }

    @Test
    void findById_shouldReturnEmpty_whenMissing() {
        when(userJpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<User> result = userRepositoryAdapter.findById("missing");

        assertThat(result).isEmpty();
        verify(userJpaRepository).findById("missing");
        verify(userPersistenceMapper, never()).toDomain(any());
    }

    @Test
    void findByUsername_shouldReturnDomainUser_whenFound() {
        when(userJpaRepository.findByUsername("jdoe")).thenReturn(Optional.of(entity));
        when(userPersistenceMapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = userRepositoryAdapter.findByUsername("jdoe");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("jdoe@example.com");
        verify(userJpaRepository).findByUsername("jdoe");
        verify(userPersistenceMapper).toDomain(entity);
    }

    @Test
    void findByEmail_shouldReturnDomainUser_whenFound() {
        when(userJpaRepository.findByEmail("jdoe@example.com")).thenReturn(Optional.of(entity));
        when(userPersistenceMapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = userRepositoryAdapter.findByEmail("jdoe@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("jdoe");
        verify(userJpaRepository).findByEmail("jdoe@example.com");
        verify(userPersistenceMapper).toDomain(entity);
    }

    @Test
    void findAll_shouldMapEntities() {
        when(userJpaRepository.findAll()).thenReturn(List.of(entity));
        when(userPersistenceMapper.toDomainList(List.of(entity))).thenReturn(List.of(user));

        List<User> result = userRepositoryAdapter.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("user-1");
        verify(userJpaRepository).findAll();
        verify(userPersistenceMapper).toDomainList(List.of(entity));
    }

    @Test
    void save_shouldPersistEntity() {
        when(userPersistenceMapper.toEntity(user)).thenReturn(entity);
        when(userJpaRepository.save(entity)).thenReturn(entity);
        when(userPersistenceMapper.toDomain(entity)).thenReturn(user);

        User result = userRepositoryAdapter.save(user);

        assertThat(result.getEmail()).isEqualTo("jdoe@example.com");
        verify(userPersistenceMapper).toEntity(user);
        verify(userJpaRepository).save(entity);
        verify(userPersistenceMapper).toDomain(entity);
    }

    @Test
    void delete_shouldRemoveEntity() {
        doNothing().when(userJpaRepository).deleteById("user-1");

        userRepositoryAdapter.delete("user-1");

        verify(userJpaRepository).deleteById("user-1");
    }
}
