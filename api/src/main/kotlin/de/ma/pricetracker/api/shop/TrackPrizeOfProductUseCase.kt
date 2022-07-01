package de.ma.pricetracker.api.shop

import de.ma.tracker.domain.product.message.ProductShow

interface TrackPrizeOfProductUseCase {

    fun execute(productShow: ProductShow)

}