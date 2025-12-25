package com.mediaserver.domain.model;

public enum MovieStatus {
    PENDING, // Just added, not downloaded
    DOWNLOADING, // Currently downloading from Mega
    READY, // Downloaded and ready to stream
    ERROR // Download failed
}
