package com.mediaserver.application.service;

import com.mediaserver.application.command.CreateUserCommand;
import com.mediaserver.application.command.UpdateUserCommand;
import com.mediaserver.application.usecase.user.CreateUserUseCase;
import com.mediaserver.application.usecase.user.DeleteUserUseCase;
import com.mediaserver.application.usecase.user.GetAllUsersUseCase;
import com.mediaserver.application.usecase.user.GetUserUseCase;
import com.mediaserver.application.usecase.user.UpdateUserUseCase;
import com.mediaserver.domain.model.User;
import com.mediaserver.domain.repository.UserRepository;
import com.mediaserver.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService
        implements GetUserUseCase,
                GetAllUsersUseCase,
                CreateUserUseCase,
                UpdateUserUseCase,
                DeleteUserUseCase {

    private final UserRepository userRepository;

    @Override
    public User getUser(String id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(CreateUserCommand command) {
        LocalDateTime now = LocalDateTime.now();
        User user =
                User.builder()
                        .username(command.getUsername())
                        .email(command.getEmail())
                        .displayName(command.getDisplayName())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
        return userRepository.save(user);
    }

    @Override
    public User updateUser(UpdateUserCommand command) {
        User user = userRepository.findById(command.getId()).orElseThrow(() -> new UserNotFoundException(command.getId()));

        User updatedUser =
                user.withUsername(command.getUsername())
                        .withEmail(command.getEmail())
                        .withDisplayName(command.getDisplayName())
                        .withUpdatedAt(LocalDateTime.now());

        return userRepository.save(updatedUser);
    }

    @Override
    public void deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user.getId());
    }
}
