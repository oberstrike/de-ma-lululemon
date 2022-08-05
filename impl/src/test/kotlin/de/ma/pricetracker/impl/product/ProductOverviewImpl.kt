package de.ma.pricetracker.impl.product

import de.ma.tracker.domain.product.message.ProductOverview
import java.util.*

data class ProductOverviewImpl(
    override val id: UUID,
    override val name: String,
    override val color: String,
    override val shopId: UUID,
    override val size: String,
    override val version: Long,
    override val pId: String?
) : ProductOverview
