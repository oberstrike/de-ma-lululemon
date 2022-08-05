package de.ma.tracker.domain.shop

import de.ma.tracker.domain.product.message.ProductOverview
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.state.message.StateCreate
import java.util.*

interface ShopService {
    val shop: Shop
    fun track(productShow: ProductOverview): StateCreate
}