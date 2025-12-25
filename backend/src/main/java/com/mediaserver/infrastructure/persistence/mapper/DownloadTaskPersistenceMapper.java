package com.mediaserver.infrastructure.persistence.mapper;

import com.mediaserver.domain.model.DownloadTask;
import com.mediaserver.infrastructure.persistence.entity.DownloadTaskJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between DownloadTask domain entity and DownloadTaskJpaEntity.
 * This mapper is part of the persistence adapter layer.
 */
@Mapper(componentModel = "spring")
public interface DownloadTaskPersistenceMapper {

    /** Maps JPA entity to domain entity. Extracts movieId from the movie relationship. */
    @Mapping(target = "movieId", source = "movie.id")
    DownloadTask toDomain(DownloadTaskJpaEntity entity);

    /**
     * Maps domain entity to JPA entity. Movie relationship must be set separately in the adapter.
     */
    @Mapping(target = "movie", ignore = true)
    DownloadTaskJpaEntity toEntity(DownloadTask domain);

    /** Maps list of JPA entities to list of domain entities. */
    List<DownloadTask> toDomainList(List<DownloadTaskJpaEntity> entities);

    /** Maps list of domain entities to list of JPA entities. */
    List<DownloadTaskJpaEntity> toEntityList(List<DownloadTask> domains);
}
