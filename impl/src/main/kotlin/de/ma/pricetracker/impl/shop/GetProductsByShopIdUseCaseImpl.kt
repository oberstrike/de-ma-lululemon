package de.ma.pricetracker.impl.shop

import de.ma.pricetracker.api.shop.GetProductsByShopIdUseCase
import de.ma.tracker.domain.product.Product
import de.ma.tracker.domain.product.ProductGateway
import java.util.*

class GetProductsByShopIdUseCaseImpl(
    private val productGateway: ProductGateway
): GetProductsByShopIdUseCase {

    override fun execute(shopId: UUID): List<Product> {
        return productGateway.getProductsByShopId(shopId)
    }
}