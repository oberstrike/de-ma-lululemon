package com.mediaserver.infrastructure.persistence.mapper;

import com.mediaserver.domain.model.User;
import com.mediaserver.infrastructure.persistence.entity.UserJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    User toDomain(UserJpaEntity entity);

    UserJpaEntity toEntity(User domain);

    List<User> toDomainList(List<UserJpaEntity> entities);

    List<UserJpaEntity> toEntityList(List<User> domains);
}
