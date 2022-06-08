package de.ma.tracker.domain.product.repository

import de.ma.tracker.domain.product.entity.Product

interface ProductRepository {

    fun create(product: Product)

}