package com.mediaserver.infrastructure.persistence.repository;

import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.infrastructure.persistence.entity.MovieJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for MovieJpaEntity. This is an infrastructure component that provides
 * persistence operations.
 */
public interface JpaMovieRepository extends JpaRepository<MovieJpaEntity, String> {

    List<MovieJpaEntity> findByStatus(MovieStatus status);

    List<MovieJpaEntity> findByCategoryId(String categoryId);

    Optional<MovieJpaEntity> findByMegaPath(String megaPath);

    boolean existsByMegaPath(String megaPath);

    @Query(
            "SELECT m FROM MovieJpaEntity m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query,"
                    + " '%'))")
    List<MovieJpaEntity> search(@Param("query") String query);

    @Query("SELECT m FROM MovieJpaEntity m WHERE m.status = :status ORDER BY m.createdAt DESC")
    List<MovieJpaEntity> findByStatusOrderByCreatedAtDesc(@Param("status") MovieStatus status);

    default List<MovieJpaEntity> findReadyMovies() {
        return findByStatusOrderByCreatedAtDesc(MovieStatus.READY);
    }

    @Query(
            "SELECT COALESCE(SUM(m.fileSize), 0) FROM MovieJpaEntity m WHERE m.localPath IS NOT"
                    + " NULL")
    Long getTotalCacheSize();

    @Query("SELECT m FROM MovieJpaEntity m WHERE m.localPath IS NOT NULL ORDER BY m.updatedAt DESC")
    List<MovieJpaEntity> findCachedMovies();

    long countByLocalPathIsNotNull();

    @Query(
            "SELECT m FROM MovieJpaEntity m JOIN MovieFavoriteJpaEntity f ON f.movie = m WHERE f.userId = :userId")
    List<MovieJpaEntity> findFavoritesByUserId(@Param("userId") String userId);

    @Query(
            "SELECT m FROM MovieJpaEntity m WHERE m.localPath IS NOT NULL AND NOT EXISTS (SELECT 1 FROM MovieFavoriteJpaEntity f WHERE f.movie = m)"
                    + " ORDER BY m.updatedAt DESC")
    List<MovieJpaEntity> findCachedNonFavoriteMovies();
}
