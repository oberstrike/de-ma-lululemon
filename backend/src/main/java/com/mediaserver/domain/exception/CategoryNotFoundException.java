package com.mediaserver.domain.exception;

/**
 * Exception thrown when a category is not found.
 * Domain exception - no framework dependencies.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String id) {
        super("Category not found with id: " + id);
    }

    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
