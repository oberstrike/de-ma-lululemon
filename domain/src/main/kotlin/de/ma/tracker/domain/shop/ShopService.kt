package de.ma.tracker.domain.shop

import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.state.message.StateCreate

interface ShopService {
    val name: String
    fun track(productShow: ProductShow): StateCreate
}