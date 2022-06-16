package de.ma.tracker.domain.base.repository

import de.ma.tracker.domain.base.IBaseEntity

interface CrudRepository<T : IBaseEntity> {

    fun create(entity: T): T

    fun deleteById(id: Long)

    fun findById(id: Long): T?

    fun update(entity: T)
}