package com.mediaserver.application.usecase.user;

import com.mediaserver.domain.model.User;

public interface GetUserUseCase {
    User getUser(String id);
}
