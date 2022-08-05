package de.ma.pricetracker.impl.shop.data

import java.util.*

data class ShopShowImpl(
    override val id: UUID,
    override val name: String
) : ShopShow
