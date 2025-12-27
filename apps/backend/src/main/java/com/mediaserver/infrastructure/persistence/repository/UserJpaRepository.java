package com.mediaserver.infrastructure.persistence.repository;

import com.mediaserver.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, String> {
    Optional<UserJpaEntity> findByUsername(String username);

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByExternalId(String externalId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
