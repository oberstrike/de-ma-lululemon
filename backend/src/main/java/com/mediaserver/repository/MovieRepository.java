package com.mediaserver.repository;

import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {

    List<Movie> findByStatus(MovieStatus status);

    List<Movie> findByCategoryId(String categoryId);

    Optional<Movie> findByMegaPath(String megaPath);

    boolean existsByMegaPath(String megaPath);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Movie> search(@Param("query") String query);

    @Query("SELECT m FROM Movie m WHERE m.status = :status ORDER BY m.createdAt DESC")
    List<Movie> findByStatusOrderByCreatedAtDesc(@Param("status") MovieStatus status);

    default List<Movie> findReadyMovies() {
        return findByStatusOrderByCreatedAtDesc(MovieStatus.READY);
    }

    @Query("SELECT COALESCE(SUM(m.fileSize), 0) FROM Movie m WHERE m.localPath IS NOT NULL")
    Long getTotalCacheSize();

    @Query("SELECT m FROM Movie m WHERE m.localPath IS NOT NULL ORDER BY m.updatedAt DESC")
    List<Movie> findCachedMovies();

    long countByLocalPathIsNotNull();
}
