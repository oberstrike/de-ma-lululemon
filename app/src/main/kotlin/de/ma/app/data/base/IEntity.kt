package de.ma.app.data.base

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.GeneratedValue
import javax.persistence.Id

interface IEntity {

    @get:Id
    @get:GeneratedValue(generator = "UUID")
    @get:GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    var id: UUID?

}

class IEntityImpl(
    override var id: UUID? = null
) : IEntity