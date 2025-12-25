package com.mediaserver.infrastructure.persistence.mapper;

import com.mediaserver.domain.model.Category;
import com.mediaserver.infrastructure.persistence.entity.CategoryJpaEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Category domain entity and CategoryJpaEntity. This mapper
 * is part of the persistence adapter layer.
 */
@Mapper(componentModel = "spring")
public interface CategoryPersistenceMapper {

    /**
     * Maps JPA entity to domain entity. Movies collection is ignored as it's not part of the domain
     * model.
     */
    @Mapping(target = "id", source = "id")
    Category toDomain(CategoryJpaEntity entity);

    /** Maps domain entity to JPA entity. Movies collection is ignored and managed by JPA. */
    @Mapping(target = "movies", ignore = true)
    CategoryJpaEntity toEntity(Category domain);

    /** Maps list of JPA entities to list of domain entities. */
    List<Category> toDomainList(List<CategoryJpaEntity> entities);

    /** Maps list of domain entities to list of JPA entities. */
    List<CategoryJpaEntity> toEntityList(List<Category> domains);
}
