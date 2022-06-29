package de.ma.app.infrastructure.rest.product.data

import de.ma.tracker.domain.product.message.ProductCreate

data class ProductCreateDTO(
    override var name: String,
    override var color: String,
    override var size: String,
    override var pId: String?
): ProductCreate
