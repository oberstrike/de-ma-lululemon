package de.ma.infrastructure.entity

import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.entity.State
import javax.persistence.*


@Entity
class StateEntity(
    override var id: Long? = null
) : State {

    @get:ManyToOne(optional = false)
    override var product: Product? = null

    @get:Column(name = "search_count", nullable = false)
    override var searchCount: Long = 0

    @get:Column(name = "version")
    @get:Version
    override var version: Long = 0

}