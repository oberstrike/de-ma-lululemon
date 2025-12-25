package com.mediaserver.domain.exception;

/**
 * Exception thrown when attempting to access a video that is not ready.
 * Domain exception - no framework dependencies.
 */
public class VideoNotReadyException extends RuntimeException {

    public VideoNotReadyException(String message) {
        super(message);
    }

    public VideoNotReadyException(String message, Throwable cause) {
        super(message, cause);
    }
}
