package de.ma.pricetracker.api.shop

import de.ma.tracker.domain.shop.Shop
import de.ma.tracker.domain.shop.ShopService

interface GetShopsUseCase {
    fun execute(): List<Shop>
}