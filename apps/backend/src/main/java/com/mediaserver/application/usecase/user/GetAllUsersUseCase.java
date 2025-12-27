package com.mediaserver.application.usecase.user;

import com.mediaserver.domain.model.User;

import java.util.List;

public interface GetAllUsersUseCase {
    List<User> getAllUsers();
}
