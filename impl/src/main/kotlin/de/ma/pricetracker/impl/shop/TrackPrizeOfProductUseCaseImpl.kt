package de.ma.pricetracker.impl.shop

import de.ma.pricetracker.api.product.AddStateToProductUseCase
import de.ma.pricetracker.api.shop.TrackPrizeOfProductUseCase
import de.ma.tracker.domain.product.message.ProductShow
import de.ma.tracker.domain.shop.ShopGateway
import de.ma.tracker.domain.shop.ShopServiceGateway

class TrackPrizeOfProductUseCaseImpl(
    private val shopGateway: ShopGateway,
    private val shopServiceGateway: ShopServiceGateway,
    private val addStateToProductUseCase: AddStateToProductUseCase
) : TrackPrizeOfProductUseCase {

    override fun execute(productShow: ProductShow) {
        val productId = productShow.id
        val shop = shopGateway.getById(productId)
            ?: throw IllegalArgumentException("No Shop with id $productId was found.")
        val shopService = shopServiceGateway.getShopServiceByName(shop.name)
        val stateCreate = shopService.track(productShow)
        addStateToProductUseCase.execute(productId, stateCreate)
    }
}