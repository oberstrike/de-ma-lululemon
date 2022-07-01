package de.ma.pricetracker.impl.shop

import de.ma.pricetracker.api.shop.AddProductToShopUseCase
import de.ma.tracker.domain.product.ProductGateway
import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductShow
import java.util.*

class AddProductToShopUseCaseImpl(
    private val productGateway: ProductGateway
) : AddProductToShopUseCase {

    override fun execute(productCreate: ProductCreate, shopId: UUID): ProductShow {
        return productGateway.createProduct(productCreate, shopId)
    }
}