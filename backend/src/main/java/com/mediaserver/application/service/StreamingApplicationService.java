package com.mediaserver.application.service;

import com.mediaserver.application.port.in.StreamVideoUseCase;
import com.mediaserver.application.port.out.MoviePort;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.exception.VideoNotReadyException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BoundedInputStream;
import org.springframework.stereotype.Service;

/**
 * Application service implementing video streaming use cases. This service handles HTTP range
 * requests for video streaming.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingApplicationService implements StreamVideoUseCase {

    private final MediaProperties properties;
    private final MoviePort moviePort;

    @Override
    public StreamingResponse streamVideo(String movieId, String rangeHeader) {
        var movie =
                moviePort.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));

        if (!movie.isCached()) {
            throw new VideoNotReadyException("Video is not yet downloaded");
        }

        var videoPath = Path.of(movie.getLocalPath());
        var fileSizeValue = movie.getFileSize();
        if (fileSizeValue == null || fileSizeValue <= 0) {
            throw new VideoNotReadyException("Video file size is not available");
        }
        long fileSize = fileSizeValue;
        var contentType = movie.getContentType() != null ? movie.getContentType() : "video/mp4";
        var range = parseRange(rangeHeader, fileSize);

        return StreamingResponse.builder()
                .inputStreamSupplier(() -> createRangeInputStream(videoPath, range))
                .contentType(contentType)
                .contentLength(range.length())
                .fileSize(fileSize)
                .rangeStart(range.start())
                .rangeEnd(range.end())
                .isPartial(rangeHeader != null)
                .build();
    }

    private HttpRange parseRange(String rangeHeader, long fileSize) {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            return new HttpRange(0, fileSize - 1, fileSize);
        }

        try {
            var rangeSpec = rangeHeader.substring(6);
            var ranges = rangeSpec.split("-", 2);

            if (ranges.length == 0 || ranges[0].isEmpty()) {
                log.warn("Invalid range header format: {}", rangeHeader);
                return new HttpRange(0, fileSize - 1, fileSize);
            }

            var start = Long.parseLong(ranges[0].trim());
            var end =
                    (ranges.length > 1 && !ranges[1].isEmpty())
                            ? Long.parseLong(ranges[1].trim())
                            : fileSize - 1;

            if (start < 0 || start >= fileSize || end < start) {
                log.warn(
                        "Invalid range values: start={}, end={}, fileSize={}",
                        start,
                        end,
                        fileSize);
                return new HttpRange(0, fileSize - 1, fileSize);
            }

            var chunkSize = properties.getStreaming().getChunkSize();
            if (end - start + 1 > chunkSize) {
                end = start + chunkSize - 1;
            }
            end = Math.min(end, fileSize - 1);

            return new HttpRange(start, end, end - start + 1);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse range header '{}': {}", rangeHeader, e.getMessage());
            return new HttpRange(0, fileSize - 1, fileSize);
        }
    }

    @SuppressWarnings("deprecation")
    private InputStream createRangeInputStream(Path path, HttpRange range) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(path.toFile(), "r");
            file.seek(range.start());
            InputStream channelStream = Channels.newInputStream(file.getChannel());
            InputStream boundedStream = new BoundedInputStream(channelStream, range.length());

            final RandomAccessFile fileRef = file;

            return new InputStream() {
                private boolean closed = false;

                @Override
                public int read() throws IOException {
                    return boundedStream.read();
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return boundedStream.read(b, off, len);
                }

                @Override
                public int available() throws IOException {
                    return boundedStream.available();
                }

                @Override
                public long skip(long n) throws IOException {
                    return boundedStream.skip(n);
                }

                @Override
                public void close() throws IOException {
                    if (closed) return;
                    closed = true;
                    try {
                        boundedStream.close();
                    } finally {
                        fileRef.close();
                    }
                }
            };
        } catch (IOException e) {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException closeEx) {
                    log.warn(
                            "Failed to close RandomAccessFile after error: {}",
                            closeEx.getMessage());
                }
            }
            throw new RuntimeException("Failed to create range input stream for: " + path, e);
        }
    }

    private record HttpRange(long start, long end, long length) {}
}
