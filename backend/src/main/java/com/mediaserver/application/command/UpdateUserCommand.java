package com.mediaserver.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateUserCommand {
    String id;
    String username;
    String email;
    String displayName;
}
