package de.ma.app.data.product

import de.ma.tracker.domain.product.message.ProductCreate

fun ProductCreate.toEntity(): ProductEntity {
    val productEntity = ProductEntity()

    productEntity.name = name
    productEntity.color = color
    productEntity.pId = pId!!
    productEntity.size = size

    return productEntity
}