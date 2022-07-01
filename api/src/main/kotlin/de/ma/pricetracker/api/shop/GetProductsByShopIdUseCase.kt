package de.ma.pricetracker.api.shop

import de.ma.tracker.domain.product.Product
import java.util.*

interface GetProductsByShopIdUseCase {
    fun execute(shopId: UUID): List<Product>
}