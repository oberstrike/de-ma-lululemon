package com.mediaserver.infrastructure.persistence.adapter;

import com.mediaserver.domain.model.User;
import com.mediaserver.domain.repository.UserRepository;
import com.mediaserver.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.mediaserver.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper mapper;

    @Override
    public Optional<User> findById(String id) {
        return userJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByExternalId(String externalId) {
        return userJpaRepository.findByExternalId(externalId).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return mapper.toDomainList(userJpaRepository.findAll());
    }

    @Override
    public User save(User user) {
        var entity = mapper.toEntity(user);
        entity.setId(user.getId());
        var saved = userJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(String id) {
        userJpaRepository.deleteById(id);
    }
}
