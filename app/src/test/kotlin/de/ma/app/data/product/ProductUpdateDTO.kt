package de.ma.app.data.product

import de.ma.tracker.domain.product.message.ProductUpdate

data class ProductUpdateDTO(
    override var color: String,
    override var name: String,
    override var size: String,
    override var pId: String?,
    override var version: Long
): ProductUpdate {

}