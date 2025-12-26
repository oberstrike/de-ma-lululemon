package com.mediaserver.application.usecase.user;

import com.mediaserver.application.command.UpdateUserCommand;
import com.mediaserver.domain.model.User;

public interface UpdateUserUseCase {
    User updateUser(UpdateUserCommand command);
}
