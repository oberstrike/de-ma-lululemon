package com.mediaserver.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateUserCommand {
    String username;
    String email;
    String displayName;
}
