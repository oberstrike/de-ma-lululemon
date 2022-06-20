package de.ma.tracker.domain.state

import de.ma.tracker.domain.product.Product

interface StateCreate {
    val price: Long
    val product: Product?
}