package com.mediaserver.application.service;

import com.mediaserver.application.port.in.StreamVideoUseCase;
import com.mediaserver.application.port.out.MoviePort;
import com.mediaserver.config.MediaProperties;
import com.mediaserver.domain.model.Movie;
import com.mediaserver.exception.MovieNotFoundException;
import com.mediaserver.exception.VideoNotReadyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.BoundedInputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Path;

/**
 * Application service implementing video streaming use cases.
 * This service handles HTTP range requests for video streaming.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingApplicationService implements StreamVideoUseCase {

    private final MediaProperties properties;
    private final MoviePort moviePort;

    @Override
    public StreamingResponse streamVideo(String movieId, String rangeHeader) {
        Movie movie = moviePort.findById(movieId)
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

    @SuppressWarnings("deprecation")
    private InputStream createRangeInputStream(Path path, HttpRange range) {
        try {
            RandomAccessFile file = new RandomAccessFile(path.toFile(), "r");
            file.seek(range.start());
            InputStream channelStream = Channels.newInputStream(file.getChannel());
            InputStream boundedStream = new BoundedInputStream(channelStream, range.length());

            // Wrap to ensure RandomAccessFile is closed when stream is closed
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return boundedStream.read();
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return boundedStream.read(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    try {
                        boundedStream.close();
                    } finally {
                        file.close();
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Failed to create range input stream", e);
        }
    }

    private record HttpRange(long start, long end, long length) {}
}
