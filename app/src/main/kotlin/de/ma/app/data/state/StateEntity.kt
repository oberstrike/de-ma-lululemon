package de.ma.app.data.state

import de.ma.app.data.base.IEntity
import de.ma.app.data.base.IEntityImpl
import de.ma.app.data.product.ProductEntity
import de.ma.tracker.domain.product.Product
import de.ma.tracker.domain.state.State
import java.util.*
import javax.persistence.*


@Entity(name = "state")
class StateEntity : State, IEntity by IEntityImpl() {

    @get:ManyToOne(fetch = FetchType.LAZY, optional = false)
    override var product: ProductEntity? = null

    @get:Column(name = "price")
    override var price: Long = 0
}
