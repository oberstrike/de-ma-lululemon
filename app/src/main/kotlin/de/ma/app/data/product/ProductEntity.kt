package de.ma.app.data.product

import de.ma.app.data.base.IEntity
import de.ma.app.data.base.IEntityImpl
import de.ma.app.data.state.StateEntity
import de.ma.tracker.domain.product.Product
import de.ma.tracker.domain.product.message.ProductShow
import javax.persistence.*

@Entity(name = "product")
class ProductEntity : Product, IEntity by IEntityImpl() {

    override var pId: String? = null

    override var color: String = ""

    override var size: String = ""

    override var name: String = ""

    @get:Version
    override var version: Long = 0

    @get:OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    override var states: MutableSet<StateEntity> = mutableSetOf()

    fun addStates(states: Collection<StateEntity>) {
        states.forEach {
            it.product = this
        }
        this.states.addAll(states)
    }

    fun addState(state: StateEntity) {
        state.product = this
        this.states.add(state)
    }

    fun removeStates() {
        this.states.clear()
    }

}

fun Product.toShow(states: List<StateEntity>?): ProductShow {
    return ProductShowImpl(
        this.id!!,
        this.pId,
        this.color,
        this.size,
        this.name,
        this.version,
        states?.map { it.id ?: throw IllegalStateException("State has no id") }
    )
}