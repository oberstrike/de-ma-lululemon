package de.ma.app.data.product

import de.ma.tracker.domain.product.Product

import de.ma.app.data.base.IEntity
import de.ma.app.data.base.IEntityImpl
import de.ma.app.data.state.StateEntity
import de.ma.tracker.domain.product.message.ProductOverview
import de.ma.tracker.domain.product.message.ProductShow
import org.hibernate.Hibernate
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity(name = "product")
class ProductEntity : Product, IEntity by IEntityImpl() {

    override var pId: String? = null

    override var color: String = ""

    override var size: String = ""

    override var name: String = ""

    @get:Version
    override var version: Long = 0

    override var id: UUID? = null

    @get:CreationTimestamp
    @get:Column(name = "created_at")
    override var createdAt: LocalDateTime = LocalDateTime.now()

    @get:UpdateTimestamp
    @get:Column(name = "updated_at")
    override var updatedAt: LocalDateTime? = null

    @Embedded
    override val trackState: TrackStateVO = TrackStateVO(false, UUID.randomUUID())

    @get:OneToMany(
        mappedBy = "product",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY
    )
    override var states: MutableSet<StateEntity> = mutableSetOf()

    fun addStates(states: Collection<StateEntity>) {
        this.states.addAll(states)
    }

    fun addState(state: StateEntity) {
        this.states.add(state)
    }

    fun removeStates() {
        this.states.clear()
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        if (other is ProductEntity) {
            return id == other.id
        }
        return false
    }

    override fun hashCode(): Int = id.hashCode()

}

fun Product.toShow(states: List<StateEntity>): ProductShow {
    return ProductShowImpl(
        this.id!!,
        this.pId,
        this.color,
        this.size,
        this.name,
        this.version,
        states.map { it.id ?: throw IllegalStateException("State has no id") }
    )
}

fun Product.toOverview(): ProductOverview {
    return ProductOverviewImpl(
        this.id!!,
        this.name,
        this.color,
        this.trackState.shopId,
        this.size,
        this.pId,
        this.version
    )
}