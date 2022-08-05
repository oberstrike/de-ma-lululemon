package de.ma.app.data.product

import de.ma.tracker.domain.product.message.ProductOverview
import java.util.*

data class ProductOverviewImpl(
    override val id: UUID,
    override val name: String,
    override val color: String,
    override val shopId: UUID,
    override val size: String,
    override val pId: String?,
    override val version: Long
): ProductOverview {}