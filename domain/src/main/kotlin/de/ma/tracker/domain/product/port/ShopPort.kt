package de.ma.tracker.domain.product.port

import de.ma.tracker.domain.product.entity.Product
import de.ma.tracker.domain.product.entity.State

interface ShopPort {
    fun getState(product: Product): State
}