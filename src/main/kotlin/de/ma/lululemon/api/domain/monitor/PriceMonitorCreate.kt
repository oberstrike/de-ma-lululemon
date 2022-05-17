package de.ma.lululemon.api.domain.monitor

import de.ma.lululemon.api.domain.monitor.product.ProductCreateDTO
import de.ma.lululemon.jobs.pages.ShopType


data class PriceMonitorCreate(
    val productCreate: ProductCreateDTO,
    val shopType: ShopType
)

