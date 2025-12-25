package com.mediaserver.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateCategoryCommand {
    String name;
    String description;
    Integer sortOrder;
}
