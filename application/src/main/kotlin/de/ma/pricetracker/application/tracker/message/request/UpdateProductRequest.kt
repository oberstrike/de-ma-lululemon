package de.ma.pricetracker.application.tracker.message.request

import de.ma.tracker.domain.product.vo.ShopType

interface UpdateProductRequest {
    val pId: String
    val color: String
    val size: String
    val name: String
    val shop: ShopType
}