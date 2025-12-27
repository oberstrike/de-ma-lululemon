package com.mediaserver.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateCategoryCommand {
    String id;
    String name;
    String description;
    Integer sortOrder;
}
