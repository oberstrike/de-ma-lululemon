package com.mediaserver.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
public class Category {
    String id;
    String name;
    String description;
    String megaPath;
    Integer sortOrder;
}
