package com.mediaserver.service;

import com.mediaserver.config.MediaProperties;
import com.mediaserver.domain.model.DownloadStatus;
import com.mediaserver.domain.model.DownloadTask;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.domain.model.MovieStatus;
import com.mediaserver.domain.repository.DownloadTaskRepository;
import com.mediaserver.domain.repository.MovieRepository;
import com.mediaserver.event.DownloadProgressEvent;
import com.mediaserver.exception.DownloadException;
import com.mediaserver.infrastructure.rest.dto.DownloadProgressDto;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MegaDownloadService {

    private final MediaProperties properties;
    private final MovieRepository movieRepository;
    private final DownloadTaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Lock for task creation to prevent race conditions
    private final ReentrantLock taskCreationLock = new ReentrantLock();

    // Reusable HTTP client for better resource management
    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

    @Async
    public CompletableFuture<Path> downloadMovie(Movie movie) {
        log.info("Starting download for movie: {}", movie.getTitle());
        DownloadTask task = createOrUpdateTask(movie, DownloadStatus.IN_PROGRESS);

        try {
            Path targetPath = prepareTargetPath(movie);

            if (isMegaUrl(movie.getMegaUrl())) {
                downloadFromMega(
                        movie.getMegaUrl(),
                        targetPath,
                        progress -> {
                            updateProgress(task, progress);
                            publishProgress(movie, progress);
                        });
            } else {
                downloadViaHttp(
                        movie.getMegaUrl(),
                        targetPath,
                        progress -> {
                            updateProgress(task, progress);
                            publishProgress(movie, progress);
                        });
            }

            Movie updatedMovie =
                    movie.withLocalPath(targetPath.toString())
                            .withFileSize(Files.size(targetPath))
                            .withContentType(detectContentType(targetPath))
                            .withStatus(MovieStatus.READY);
            movieRepository.save(updatedMovie);

            DownloadTask updatedTask =
                    task.withStatus(DownloadStatus.COMPLETED)
                            .withCompletedAt(LocalDateTime.now())
                            .withProgress(100);
            taskRepository.save(updatedTask);

            publishProgress(
                    movie, new DownloadProgress(100, movie.getFileSize(), movie.getFileSize()));
            log.info("Download completed for movie: {}", movie.getTitle());
            return CompletableFuture.completedFuture(targetPath);

        } catch (Exception e) {
            log.error("Download failed for movie: {}", movie.getTitle(), e);
            handleDownloadError(movie, task, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private boolean isMegaUrl(String url) {
        return url != null && url.contains("mega.nz");
    }

    private void downloadFromMega(
            String megaUrl, Path targetPath, Consumer<DownloadProgress> progressCallback)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("mega-get", megaUrl, targetPath.toString());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("mega-get: {}", line);
                DownloadProgress progress = parseProgress(line);
                if (progress != null) progressCallback.accept(progress);
            }
        }

        int timeoutMinutes = properties.getDownload().getProcessTimeoutMinutes();
        boolean completed = process.waitFor(timeoutMinutes, TimeUnit.MINUTES);
        if (!completed) {
            process.destroyForcibly();
            throw new DownloadException("mega-get timed out after " + timeoutMinutes + " minutes");
        }
        int exitCode = process.exitValue();
        if (exitCode != 0)
            throw new DownloadException("mega-get failed with exit code: " + exitCode);
    }

    private void downloadViaHttp(
            String url, Path targetPath, Consumer<DownloadProgress> progressCallback)
            throws IOException, InterruptedException {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .timeout(Duration.ofHours(2))
                        .build();
        HttpResponse<InputStream> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1);

        try (InputStream in = response.body();
                OutputStream out = Files.newOutputStream(targetPath)) {
            byte[] buffer = new byte[8192];
            long downloaded = 0;
            int read;
            long lastUpdate = 0;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloaded += read;
                if (totalBytes > 0 && (downloaded - lastUpdate) > 1048576) {
                    progressCallback.accept(
                            new DownloadProgress(
                                    (int) ((downloaded * 100) / totalBytes),
                                    downloaded,
                                    totalBytes));
                    lastUpdate = downloaded;
                }
            }
        }
    }

    private Path prepareTargetPath(Movie movie) throws IOException {
        Path storageDir = Path.of(properties.getStorage().getPath());
        Files.createDirectories(storageDir);
        String safeFileName = movie.getId() + "_" + sanitizeFileName(movie.getTitle()) + ".mp4";
        return storageDir.resolve(safeFileName);
    }

    private String sanitizeFileName(String name) {
        String sanitized = name.replaceAll("[^a-zA-Z0-9.-]", "_");
        return sanitized.substring(0, Math.min(sanitized.length(), 50));
    }

    private String detectContentType(Path path) throws IOException {
        String contentType = Files.probeContentType(path);
        return contentType != null ? contentType : "video/mp4";
    }

    @Transactional
    protected DownloadTask createOrUpdateTask(Movie movie, DownloadStatus status) {
        // Use lock to prevent race conditions when creating/updating tasks
        taskCreationLock.lock();
        try {
            DownloadTask task =
                    taskRepository
                            .findByMovieId(movie.getId())
                            .orElse(DownloadTask.builder().movieId(movie.getId()).build());
            DownloadTask updatedTask =
                    task.withStatus(status)
                            .withStartedAt(LocalDateTime.now())
                            .withProgress(0)
                            .withBytesDownloaded(0L);
            return taskRepository.save(updatedTask);
        } finally {
            taskCreationLock.unlock();
        }
    }

    private void updateProgress(DownloadTask task, DownloadProgress progress) {
        DownloadTask updatedTask =
                task.withProgress(progress.percent())
                        .withBytesDownloaded(progress.bytesDownloaded())
                        .withTotalBytes(progress.totalBytes());
        taskRepository.save(updatedTask);
    }

    private void publishProgress(Movie movie, DownloadProgress progress) {
        DownloadProgressDto dto =
                DownloadProgressDto.builder()
                        .movieId(movie.getId())
                        .movieTitle(movie.getTitle())
                        .status(DownloadStatus.IN_PROGRESS)
                        .bytesDownloaded(progress.bytesDownloaded())
                        .totalBytes(progress.totalBytes())
                        .progress(progress.percent())
                        .build();
        eventPublisher.publishEvent(new DownloadProgressEvent(this, dto));
    }

    private void handleDownloadError(Movie movie, DownloadTask task, Exception e) {
        Movie updatedMovie = movie.withStatus(MovieStatus.ERROR);
        movieRepository.save(updatedMovie);

        DownloadTask updatedTask =
                task.withStatus(DownloadStatus.FAILED).withErrorMessage(e.getMessage());
        taskRepository.save(updatedTask);

        DownloadProgressDto dto =
                DownloadProgressDto.builder()
                        .movieId(movie.getId())
                        .movieTitle(movie.getTitle())
                        .status(DownloadStatus.FAILED)
                        .errorMessage(e.getMessage())
                        .build();
        eventPublisher.publishEvent(new DownloadProgressEvent(this, dto));
    }

    private DownloadProgress parseProgress(String line) {
        try {
            if (line != null && line.contains("%")) {
                for (String part : line.split("\\s+")) {
                    if (part.endsWith("%")) {
                        String percentStr = part.replace("%", "").trim();
                        if (!percentStr.isEmpty()) {
                            return new DownloadProgress(Integer.parseInt(percentStr), 0, 0);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.trace("Could not parse progress from line: {}", line);
        }
        return null;
    }

    public record DownloadProgress(int percent, long bytesDownloaded, long totalBytes) {}
}
