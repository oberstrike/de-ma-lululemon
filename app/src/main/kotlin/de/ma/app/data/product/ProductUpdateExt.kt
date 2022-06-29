package de.ma.app.data.product

import de.ma.tracker.domain.product.message.ProductUpdate
import java.util.*


fun ProductUpdate.toEntity(id: UUID): ProductEntity {
    val productEntity = ProductEntity()
    productEntity.id = id
    productEntity.pId = pId!!
    productEntity.name = name
    productEntity.color = color
    productEntity.size = size
    productEntity.version = version
    return productEntity
}
