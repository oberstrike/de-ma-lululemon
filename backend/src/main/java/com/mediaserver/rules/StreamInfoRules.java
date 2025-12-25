package com.mediaserver.rules;

import com.mediaserver.entity.Movie;
import org.springframework.stereotype.Component;

@Component
public class StreamInfoRules {
    public long fileSize(Movie movie) {
        return movie.getFileSize() == null ? 0 : movie.getFileSize();
    }

    public String contentType(Movie movie) {
        return movie.getContentType() == null ? "video/mp4" : movie.getContentType();
    }

    public String streamUrl(Movie movie) {
        return "/api/stream/" + movie.getId();
    }

    public boolean supportsRangeRequests(Movie movie) {
        return true;
    }
}
