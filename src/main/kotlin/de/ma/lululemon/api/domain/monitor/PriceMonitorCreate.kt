package de.ma.lululemon.api.domain.monitor


data class PriceMonitorCreate(
    val product: ProductCreate,
    val shopName: String
)

