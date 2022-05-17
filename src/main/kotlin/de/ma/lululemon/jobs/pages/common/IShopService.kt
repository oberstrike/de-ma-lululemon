package de.ma.lululemon.jobs.pages.common

import de.ma.lululemon.api.domain.monitor.product.ProductCreateDTO
import de.ma.lululemon.api.domain.monitor.product.ProductDTO
import de.ma.lululemon.api.domain.monitor.product.State
import de.ma.lululemon.jobs.pages.ShopType

interface IShopService {

    fun createUrl(productCreate: ProductCreateDTO): String

    fun getCurrentStateOfProduct(product: ProductDTO): State?

    fun isShop(shopType: ShopType): Boolean

}