package com.mediaserver.application.port.in;

/**
 * Use case for initiating movie downloads.
 * Defines the input port for download operations.
 */
public interface DownloadMovieUseCase {

    /**
     * Starts downloading a movie from its Mega URL.
     * @param movieId the movie ID
     * @throws com.mediaserver.exception.MovieNotFoundException if movie not found
     * @throws IllegalStateException if movie is already downloaded
     */
    void startDownload(String movieId);
}
