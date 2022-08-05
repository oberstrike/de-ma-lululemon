package de.ma.tracker.domain.shop

import java.util.*

interface ShopServiceGateway {
    fun getShopServiceById(id: UUID): ShopService

    fun getAllShops(): List<ShopService>
}