package com.mediaserver.domain.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
public class User {
    String id;
    String username;
    String email;
    String displayName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
