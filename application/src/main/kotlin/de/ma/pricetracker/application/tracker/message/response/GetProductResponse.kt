package de.ma.pricetracker.application.tracker.message.response

import de.ma.tracker.domain.product.entity.Product

class GetProductResponse(product: Product) {
    val id = product.id
    val name = product.name
    val price = product.color
    val url = product.size
}