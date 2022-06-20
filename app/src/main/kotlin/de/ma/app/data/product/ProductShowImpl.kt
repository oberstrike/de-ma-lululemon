package de.ma.app.data.product

import com.fasterxml.jackson.annotation.JsonInclude
import de.ma.tracker.domain.product.message.ProductShow
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductShowImpl(
    override var id: UUID,
    override var pId: String?,
    override var color: String,
    override var size: String,
    override var name: String,
    override var version: Long,
    override var states: List<UUID>?
): ProductShow