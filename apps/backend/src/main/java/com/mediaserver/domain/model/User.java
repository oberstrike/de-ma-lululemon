package com.mediaserver.domain.model;

import com.mediaserver.domain.exception.UserValidationException;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;

@Value
@With
public class User {
    String id;
    String username;
    String externalId;
    String email;
    String displayName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @Builder
    public User(
            String id,
            String username,
            String externalId,
            String email,
            String displayName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        if (username == null || username.isBlank()) {
            throw new UserValidationException("Username is required");
        }
        this.id = id;
        this.username = username;
        this.externalId = externalId;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
