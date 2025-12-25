package com.mediaserver.application.port.out;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Output port for file storage operations.
 * This port will be implemented by the infrastructure layer.
 */
public interface FileStoragePort {

    /**
     * Deletes a file if it exists.
     * @param path the file path
     * @return true if the file was deleted, false if it didn't exist
     * @throws IOException if deletion fails
     */
    boolean deleteIfExists(Path path) throws IOException;

    /**
     * Deletes a file.
     * @param path the file path
     * @throws IOException if deletion fails
     */
    void delete(Path path) throws IOException;

    /**
     * Checks if a file exists.
     * @param path the file path
     * @return true if the file exists
     */
    boolean exists(Path path);

    /**
     * Gets the size of a file.
     * @param path the file path
     * @return file size in bytes
     * @throws IOException if reading fails
     */
    long size(Path path) throws IOException;

    /**
     * Opens an input stream for reading a file.
     * @param path the file path
     * @return input stream
     * @throws IOException if opening fails
     */
    InputStream openInputStream(Path path) throws IOException;
}
