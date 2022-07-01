package de.ma.pricetracker.api.shop

import de.ma.tracker.domain.product.message.ProductCreate
import de.ma.tracker.domain.product.message.ProductShow
import java.util.*

interface AddProductToShopUseCase {
    fun execute(productCreate: ProductCreate, shopId: UUID): ProductShow
}