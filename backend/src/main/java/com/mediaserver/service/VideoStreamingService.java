package com.mediaserver.service;

import com.mediaserver.config.MediaProperties;
import com.mediaserver.entity.Movie;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.exception.VideoNotReadyException;
import com.mediaserver.repository.MovieRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BoundedInputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoStreamingService {

    private final MediaProperties properties;
    private final MovieRepository movieRepository;

    public StreamingResponse streamVideo(String movieId, String rangeHeader) throws IOException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        if (!movie.isCached()) {
            throw new VideoNotReadyException("Video is not yet downloaded");
        }

        Path videoPath = Path.of(movie.getLocalPath());
        long fileSize = movie.getFileSize();
        String contentType = movie.getContentType();
        HttpRange range = parseRange(rangeHeader, fileSize);

        return StreamingResponse.builder()
                .inputStreamSupplier(() -> createRangeInputStream(videoPath, range))
                .contentType(contentType)
                .contentLength(range.length)
                .fileSize(fileSize)
                .rangeStart(range.start)
                .rangeEnd(range.end)
                .isPartial(rangeHeader != null)
                .build();
    }

    private HttpRange parseRange(String rangeHeader, long fileSize) {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            return new HttpRange(0, fileSize - 1, fileSize);
        }

        String rangeSpec = rangeHeader.substring(6);
        String[] ranges = rangeSpec.split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileSize - 1;

        int chunkSize = properties.getStreaming().getChunkSize();
        if (end - start + 1 > chunkSize) {
            end = start + chunkSize - 1;
        }
        end = Math.min(end, fileSize - 1);

        return new HttpRange(start, end, end - start + 1);
    }

    private InputStream createRangeInputStream(Path path, HttpRange range) {
        try {
            RandomAccessFile file = new RandomAccessFile(path.toFile(), "r");
            file.seek(range.start);
            return BoundedInputStream.builder()
                    .setInputStream(Channels.newInputStream(file.getChannel()))
                    .setMaxCount(range.length)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create range input stream", e);
        }
    }

    public record HttpRange(long start, long end, long length) {}

    @Data
    @Builder
    public static class StreamingResponse {
        private Supplier<InputStream> inputStreamSupplier;
        private String contentType;
        private long contentLength;
        private long fileSize;
        private long rangeStart;
        private long rangeEnd;
        private boolean isPartial;
    }
}
