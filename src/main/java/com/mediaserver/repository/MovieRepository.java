package com.mediaserver.repository;

import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {

    List<Movie> findByStatus(MovieStatus status);

    List<Movie> findByCategoryId(String categoryId);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Movie> search(@Param("query") String query);

    @Query("SELECT m FROM Movie m WHERE m.status = 'READY' ORDER BY m.createdAt DESC")
    List<Movie> findReadyMovies();

    @Query("SELECT COALESCE(SUM(m.fileSize), 0) FROM Movie m WHERE m.localPath IS NOT NULL")
    Long getTotalCacheSize();

    long countByLocalPathIsNotNull();
}
