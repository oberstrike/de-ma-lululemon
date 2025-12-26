package com.mediaserver.application.usecase.user;

import com.mediaserver.application.command.CreateUserCommand;
import com.mediaserver.domain.model.User;

public interface CreateUserUseCase {
    User createUser(CreateUserCommand command);
}
