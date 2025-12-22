package com.mediaserver.repository;

import com.mediaserver.entity.Category;
import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MovieRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .name("Action")
                .description("Action movies")
                .sortOrder(1)
                .build();
        entityManager.persist(testCategory);
    }

    @Test
    void findByStatus_shouldReturnMoviesWithGivenStatus() {
        Movie pending = createMovie("Pending Movie", MovieStatus.PENDING, null);
        Movie ready = createMovie("Ready Movie", MovieStatus.READY, "/path/to/video.mp4");

        entityManager.persist(pending);
        entityManager.persist(ready);
        entityManager.flush();

        List<Movie> pendingMovies = movieRepository.findByStatus(MovieStatus.PENDING);
        List<Movie> readyMovies = movieRepository.findByStatus(MovieStatus.READY);

        assertThat(pendingMovies).hasSize(1);
        assertThat(pendingMovies.get(0).getTitle()).isEqualTo("Pending Movie");
        assertThat(readyMovies).hasSize(1);
        assertThat(readyMovies.get(0).getTitle()).isEqualTo("Ready Movie");
    }

    @Test
    void findByCategoryId_shouldReturnMoviesInCategory() {
        Movie movie1 = createMovie("Movie 1", MovieStatus.READY, "/path/1.mp4");
        movie1.setCategory(testCategory);
        Movie movie2 = createMovie("Movie 2", MovieStatus.READY, "/path/2.mp4");
        movie2.setCategory(testCategory);
        Movie movieNoCategory = createMovie("Movie 3", MovieStatus.READY, "/path/3.mp4");

        entityManager.persist(movie1);
        entityManager.persist(movie2);
        entityManager.persist(movieNoCategory);
        entityManager.flush();

        List<Movie> categoryMovies = movieRepository.findByCategoryId(testCategory.getId());

        assertThat(categoryMovies).hasSize(2);
        assertThat(categoryMovies).extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("Movie 1", "Movie 2");
    }

    @Test
    void search_shouldFindMoviesByTitle() {
        Movie movie1 = createMovie("The Matrix", MovieStatus.READY, "/path/1.mp4");
        Movie movie2 = createMovie("Matrix Reloaded", MovieStatus.READY, "/path/2.mp4");
        Movie movie3 = createMovie("Inception", MovieStatus.READY, "/path/3.mp4");

        entityManager.persist(movie1);
        entityManager.persist(movie2);
        entityManager.persist(movie3);
        entityManager.flush();

        List<Movie> results = movieRepository.search("matrix");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("The Matrix", "Matrix Reloaded");
    }

    @Test
    void search_shouldBeCaseInsensitive() {
        Movie movie = createMovie("UPPERCASE TITLE", MovieStatus.READY, "/path/1.mp4");
        entityManager.persist(movie);
        entityManager.flush();

        List<Movie> results = movieRepository.search("uppercase");

        assertThat(results).hasSize(1);
    }

    @Test
    void findReadyMovies_shouldReturnOnlyReadyMovies() {
        Movie ready1 = createMovie("Ready 1", MovieStatus.READY, "/path/1.mp4");
        Movie ready2 = createMovie("Ready 2", MovieStatus.READY, "/path/2.mp4");
        Movie pending = createMovie("Pending", MovieStatus.PENDING, null);
        Movie downloading = createMovie("Downloading", MovieStatus.DOWNLOADING, null);

        entityManager.persist(ready1);
        entityManager.persist(ready2);
        entityManager.persist(pending);
        entityManager.persist(downloading);
        entityManager.flush();

        List<Movie> readyMovies = movieRepository.findReadyMovies();

        assertThat(readyMovies).hasSize(2);
        assertThat(readyMovies).allMatch(m -> m.getStatus() == MovieStatus.READY);
    }

    @Test
    void getTotalCacheSize_shouldSumFileSizes() {
        Movie movie1 = createMovie("Movie 1", MovieStatus.READY, "/path/1.mp4");
        movie1.setFileSize(1024L * 1024 * 500); // 500MB
        Movie movie2 = createMovie("Movie 2", MovieStatus.READY, "/path/2.mp4");
        movie2.setFileSize(1024L * 1024 * 300); // 300MB
        Movie movieNoPath = createMovie("Movie 3", MovieStatus.PENDING, null);
        movieNoPath.setFileSize(null);

        entityManager.persist(movie1);
        entityManager.persist(movie2);
        entityManager.persist(movieNoPath);
        entityManager.flush();

        Long totalSize = movieRepository.getTotalCacheSize();

        assertThat(totalSize).isEqualTo(1024L * 1024 * 800); // 800MB
    }

    @Test
    void countByLocalPathIsNotNull_shouldCountCachedMovies() {
        Movie cached1 = createMovie("Cached 1", MovieStatus.READY, "/path/1.mp4");
        Movie cached2 = createMovie("Cached 2", MovieStatus.READY, "/path/2.mp4");
        Movie notCached = createMovie("Not Cached", MovieStatus.PENDING, null);

        entityManager.persist(cached1);
        entityManager.persist(cached2);
        entityManager.persist(notCached);
        entityManager.flush();

        long count = movieRepository.countByLocalPathIsNotNull();

        assertThat(count).isEqualTo(2);
    }

    private Movie createMovie(String title, MovieStatus status, String localPath) {
        return Movie.builder()
                .title(title)
                .megaUrl("https://mega.nz/file/test")
                .status(status)
                .localPath(localPath)
                .build();
    }
}
