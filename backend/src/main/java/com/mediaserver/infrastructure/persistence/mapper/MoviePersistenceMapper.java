package com.mediaserver.infrastructure.persistence.mapper;

import com.mediaserver.domain.model.Movie;
import com.mediaserver.infrastructure.persistence.entity.MovieJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for converting between Movie domain entity and MovieJpaEntity.
 * This mapper is part of the persistence adapter layer.
 */
@Mapper(componentModel = "spring")
public interface MoviePersistenceMapper {

    /**
     * Maps JPA entity to domain entity.
     * Extracts categoryId from the category relationship.
     */
    @Mapping(target = "categoryId", source = "category.id")
    Movie toDomain(MovieJpaEntity entity);

    /**
     * Maps domain entity to JPA entity.
     * Category relationship must be set separately in the adapter.
     */
    @Mapping(target = "category", ignore = true)
    MovieJpaEntity toEntity(Movie domain);

    /**
     * Maps list of JPA entities to list of domain entities.
     */
    List<Movie> toDomainList(List<MovieJpaEntity> entities);

    /**
     * Maps list of domain entities to list of JPA entities.
     */
    List<MovieJpaEntity> toEntityList(List<Movie> domains);
}
