package de.ma.pricetracker.impl.shop

import de.ma.pricetracker.api.product.AddStateToProductUseCase
import de.ma.pricetracker.api.product.TrackPrizeUseCase
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.shop.ShopServiceGateway

class TrackPrizeUseCaseImpl(
    private val shopServiceGateway: ShopServiceGateway,
    private val addStateToProductUseCase: AddStateToProductUseCase,
    private val productGateway: ProductGateway
) : TrackPrizeUseCase {

    override fun execute() {
        val products = productGateway.getTrackedProducts()

        for (product in products) {
            val shopService = shopServiceGateway.getShopServiceById(product.shopId)
            val stateCreate = shopService.track(product)
            addStateToProductUseCase(product.id, stateCreate)
        }

    }
}