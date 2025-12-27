package com.mediaserver.infrastructure.persistence.repository;

import com.mediaserver.infrastructure.persistence.entity.MovieFavoriteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface JpaMovieFavoriteRepository extends JpaRepository<MovieFavoriteJpaEntity, String> {
    boolean existsByMovie_IdAndUserId(String movieId, String userId);

    @Modifying
    @Transactional
    void deleteByMovie_IdAndUserId(String movieId, String userId);
}
