package de.ma.pricetracker.application.tracker.message.dto

import de.ma.tracker.domain.product.vo.ShopType

data class ProductDTO(
    val pId: String?,
    val color: String,
    val size: String,
    val name: String,
    val shop: ShopType
)