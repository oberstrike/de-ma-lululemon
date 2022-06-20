package de.ma.app.data.product

import de.ma.tracker.domain.product.message.ProductCreate

data class ProductCreateDTO(
    override var color: String,
    override var pId: String?,
    override var name: String,
    override var size: String
) : ProductCreate