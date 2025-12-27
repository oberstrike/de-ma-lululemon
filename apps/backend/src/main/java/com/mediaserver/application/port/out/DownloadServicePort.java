package com.mediaserver.application.port.out;

import com.mediaserver.domain.model.Movie;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Output port for download operations. This port will be implemented by the infrastructure layer.
 */
public interface DownloadServicePort {

    /**
     * Downloads a movie from its source URL.
     *
     * @param movie the movie to download
     * @return future containing the downloaded file path
     */
    CompletableFuture<Path> downloadMovie(Movie movie);
}
