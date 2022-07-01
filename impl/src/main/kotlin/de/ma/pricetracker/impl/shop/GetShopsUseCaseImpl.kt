package de.ma.pricetracker.impl.shop

import de.ma.pricetracker.api.shop.GetShopsUseCase
import de.ma.tracker.domain.shop.Shop
import de.ma.tracker.domain.shop.ShopGateway

class GetShopsUseCaseImpl(
    private val shopGateway: ShopGateway
) : GetShopsUseCase {

    override fun execute(): List<Shop> {
        return shopGateway.getShops()
    }
}