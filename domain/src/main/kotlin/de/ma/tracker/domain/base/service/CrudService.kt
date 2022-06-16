package de.ma.tracker.domain.base.service

import de.ma.tracker.domain.base.IBaseEntity
import de.ma.tracker.domain.base.exception.NotFoundException
import de.ma.tracker.domain.base.repository.CrudRepository

class CrudService<T : IBaseEntity>(
    private val crudRepository: CrudRepository<T>
) {

    fun create(entity: T) = crudRepository.create(entity)

    fun update(entity: T) = crudRepository.update(entity)

    fun deleteById(id: Long) = crudRepository.deleteById(id)

    fun findById(id: Long) =
        crudRepository.findById(id) ?: throw NotFoundException("Entity with id $id not found")

}