package de.ma.infrastructure.entity

import de.ma.tracker.domain.product.entity.Product

fun Product.toEntity(): ProductEntity {
    val productEntity = ProductEntity()
    productEntity.id = id
    productEntity.name = name
    productEntity.states = states
    productEntity.color = color
    productEntity.pId = pId
    productEntity.shop = shop
    productEntity.size = size
    productEntity.version = version
    return productEntity
}