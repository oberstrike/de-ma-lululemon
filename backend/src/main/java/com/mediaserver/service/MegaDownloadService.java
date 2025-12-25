package com.mediaserver.service;

import com.mediaserver.config.MediaProperties;
import com.mediaserver.infrastructure.rest.dto.DownloadProgressDto;
import com.mediaserver.entity.DownloadStatus;
import com.mediaserver.entity.DownloadTask;
import com.mediaserver.entity.Movie;
import com.mediaserver.entity.MovieStatus;
import com.mediaserver.event.DownloadProgressEvent;
import com.mediaserver.exception.DownloadException;
import com.mediaserver.repository.DownloadTaskRepository;
import com.mediaserver.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class MegaDownloadService {

    private final MediaProperties properties;
    private final MovieRepository movieRepository;
    private final DownloadTaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    public CompletableFuture<Path> downloadMovie(Movie movie) {
        log.info("Starting download for movie: {}", movie.getTitle());
        DownloadTask task = createOrUpdateTask(movie, DownloadStatus.IN_PROGRESS);

        try {
            Path targetPath = prepareTargetPath(movie);

            if (isMegaUrl(movie.getMegaUrl())) {
                downloadFromMega(movie.getMegaUrl(), targetPath, progress -> {
                    updateProgress(task, progress);
                    publishProgress(movie, progress);
                });
            } else {
                downloadViaHttp(movie.getMegaUrl(), targetPath, progress -> {
                    updateProgress(task, progress);
                    publishProgress(movie, progress);
                });
            }

            movie.setLocalPath(targetPath.toString());
            movie.setFileSize(Files.size(targetPath));
            movie.setContentType(detectContentType(targetPath));
            movie.setStatus(MovieStatus.READY);
            movieRepository.save(movie);

            task.setStatus(DownloadStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            task.setProgress(100);
            taskRepository.save(task);

            publishProgress(movie, new DownloadProgress(100, movie.getFileSize(), movie.getFileSize()));
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

    private void downloadFromMega(String megaUrl, Path targetPath, Consumer<DownloadProgress> progressCallback)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("mega-get", megaUrl, targetPath.toString());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
        if (exitCode != 0) throw new DownloadException("mega-get failed with exit code: " + exitCode);
    }

    private void downloadViaHttp(String url, Path targetPath, Consumer<DownloadProgress> progressCallback)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1);

        try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(targetPath)) {
            byte[] buffer = new byte[8192];
            long downloaded = 0;
            int read;
            long lastUpdate = 0;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                downloaded += read;
                if (totalBytes > 0 && (downloaded - lastUpdate) > 1048576) {
                    progressCallback.accept(new DownloadProgress((int) ((downloaded * 100) / totalBytes), downloaded, totalBytes));
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

    private DownloadTask createOrUpdateTask(Movie movie, DownloadStatus status) {
        DownloadTask task = taskRepository.findByMovieId(movie.getId())
                .orElse(DownloadTask.builder().movie(movie).build());
        task.setStatus(status);
        task.setStartedAt(LocalDateTime.now());
        task.setProgress(0);
        task.setBytesDownloaded(0L);
        return taskRepository.save(task);
    }

    private void updateProgress(DownloadTask task, DownloadProgress progress) {
        task.setProgress(progress.percent());
        task.setBytesDownloaded(progress.bytesDownloaded());
        task.setTotalBytes(progress.totalBytes());
        taskRepository.save(task);
    }

    private void publishProgress(Movie movie, DownloadProgress progress) {
        DownloadProgressDto dto = DownloadProgressDto.builder()
                .movieId(movie.getId()).movieTitle(movie.getTitle())
                .status(DownloadStatus.IN_PROGRESS)
                .bytesDownloaded(progress.bytesDownloaded())
                .totalBytes(progress.totalBytes())
                .progress(progress.percent()).build();
        eventPublisher.publishEvent(new DownloadProgressEvent(this, dto));
    }

    private void handleDownloadError(Movie movie, DownloadTask task, Exception e) {
        movie.setStatus(MovieStatus.ERROR);
        movieRepository.save(movie);
        task.setStatus(DownloadStatus.FAILED);
        task.setErrorMessage(e.getMessage());
        taskRepository.save(task);

        DownloadProgressDto dto = DownloadProgressDto.builder()
                .movieId(movie.getId()).movieTitle(movie.getTitle())
                .status(DownloadStatus.FAILED).errorMessage(e.getMessage()).build();
        eventPublisher.publishEvent(new DownloadProgressEvent(this, dto));
    }

    private DownloadProgress parseProgress(String line) {
        try {
            if (line.contains("%")) {
                for (String part : line.split("\\s+")) {
                    if (part.endsWith("%")) return new DownloadProgress(Integer.parseInt(part.replace("%", "")), 0, 0);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public record DownloadProgress(int percent, long bytesDownloaded, long totalBytes) {}
}
