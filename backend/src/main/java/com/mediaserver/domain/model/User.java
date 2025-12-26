package com.mediaserver.domain.model;

import com.mediaserver.domain.exception.UserValidationException;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
public class User {
    String id;
    String username;
    String externalId;

    @Builder
    public User(String id, String username, String externalId) {
        if (username == null || username.isBlank()) {
            throw new UserValidationException("Username is required");
        }
        this.id = id;
        this.username = username;
        this.externalId = externalId;
    }
}
