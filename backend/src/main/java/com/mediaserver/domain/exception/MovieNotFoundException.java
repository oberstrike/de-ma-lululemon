package com.mediaserver.domain.exception;

/**
 * Exception thrown when a movie is not found.
 * Domain exception - no framework dependencies.
 */
public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(String id) {
        super("Movie not found with id: " + id);
    }

    public MovieNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
