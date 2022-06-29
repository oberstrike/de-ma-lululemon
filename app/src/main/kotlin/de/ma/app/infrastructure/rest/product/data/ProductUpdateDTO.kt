package de.ma.app.infrastructure.rest.product.data

import de.ma.tracker.domain.product.message.ProductUpdate

data class ProductUpdateDTO(
    override var color: String,
    override var pId: String?,
    override var name: String,
    override var size: String,
    override var version: Long
): ProductUpdate
