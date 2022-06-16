package de.ma.infrastructure.entity

import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.entity.State
import de.ma.tracker.domain.product.vo.ShopType
import javax.persistence.*

@Entity(name = "folder")
class ProductEntity : Product {

    override var id: Long? = null

    @get:Column(name = "color")
    override var color: String = ""

    @get:Column(name = "name")
    override var name: String = ""

    @get:Enumerated(EnumType.STRING)
    override var shop: ShopType = ShopType.LULULEMON

    @get:Column(name = "size")
    override var size: String = ""

    @get:OneToMany(
        mappedBy = "folder",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    override var states: MutableSet<State> = mutableSetOf()

    @get:Column(name = "pId")
    override var pId: String? = null

    @get:Version
    override var version: Long = 0

    override fun addState(state: State) {
        val myId = this.id

        state.product = ProductEntity().apply {
            this.id = myId
        }

        states.add(state)
    }


    override fun removeStates() {
        states.clear()
    }
}