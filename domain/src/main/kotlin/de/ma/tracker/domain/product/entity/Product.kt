package de.ma.tracker.domain.product.entity

import de.ma.tracker.domain.base.IBaseEntity
import de.ma.tracker.domain.product.vo.ShopType


interface Product : IBaseEntity {

    var pId: String?

    var color: String

    var size: String

    var name: String

    var shop: ShopType

    var version: Long

    var states: MutableSet<State>

    fun addState(state: State)

    fun removeStates()

}