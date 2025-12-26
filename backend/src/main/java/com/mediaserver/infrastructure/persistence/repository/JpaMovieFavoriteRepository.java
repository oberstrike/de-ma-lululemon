package com.mediaserver.infrastructure.persistence.repository;

import com.mediaserver.infrastructure.persistence.entity.MovieFavoriteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMovieFavoriteRepository extends JpaRepository<MovieFavoriteJpaEntity, String> {
    boolean existsByMovie_IdAndUserId(String movieId, String userId);

    void deleteByMovie_IdAndUserId(String movieId, String userId);
}
