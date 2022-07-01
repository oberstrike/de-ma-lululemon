package de.ma.tracker.domain.shop

interface ShopServiceGateway {
    fun getShopServiceByName(name: String): ShopService
}