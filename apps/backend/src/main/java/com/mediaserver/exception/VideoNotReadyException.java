package com.mediaserver.exception;

public class VideoNotReadyException extends RuntimeException {
    public VideoNotReadyException(String message) {
        super(message);
    }
}
