package de.ma.app.data.shop

import de.ma.tracker.domain.shop.Shop
import java.util.*

data class ShopImpl(
    override val id: UUID,
    override val name: String
) : Shop