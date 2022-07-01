package de.ma.tracker.domain.shop

import java.util.*

interface ShopGateway {
    fun getShops(): List<Shop>

    fun getById(id: UUID): Shop?
}