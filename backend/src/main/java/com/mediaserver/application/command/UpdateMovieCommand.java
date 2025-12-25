package com.mediaserver.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateMovieCommand {
    String id;
    String title;
    String description;
    Integer year;
    String duration;
    String megaUrl;
    String thumbnailUrl;
    String categoryId;
}
